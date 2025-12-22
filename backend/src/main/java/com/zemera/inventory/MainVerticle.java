package com.zemera.inventory;

import com.zemera.inventory.handler.OrderHandler;
import com.zemera.inventory.service.OrderService;
import com.zemera.inventory.repository.OrderRepository;
import com.zemera.inventory.repository.ProductRepository;
import com.zemera.inventory.db.PgClientFactory;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgPool;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        loadConfig().future().onComplete(ar -> {
            if (ar.failed()) {
                LOGGER.error("Failed to load configuration", ar.cause());
                startPromise.fail(ar.cause());
                return;
            }

            JsonObject config = ar.result();

            // HTTP port: prefer env HTTP_PORT, then config.http.port, then 8080
            int httpPort = config.getInteger("HTTP_PORT",
                config.getJsonObject("http", new JsonObject()).getInteger("port", 8080));

            // DB config: prefer nested "db" object, otherwise fall back to flat env vars
            JsonObject dbConfig = config.getJsonObject("db", new JsonObject());
            if (dbConfig.isEmpty()) {
                dbConfig
                    .put("host", config.getString("DB_HOST", "localhost"))
                    .put("port", config.getInteger("DB_PORT", 5432))
                    .put("database", config.getString("DB_NAME", "postgres"))
                    .put("user", config.getString("DB_USER", "postgres"))
                    .put("password", config.getString("DB_PASSWORD", "postgres"));
            }

            PgPool client = PgClientFactory.createPool(vertx, dbConfig);

            ProductRepository productRepository = new ProductRepository(client);
            OrderRepository orderRepository = new OrderRepository(client);
            OrderService orderService = new OrderService(productRepository, orderRepository);
            OrderHandler orderHandler = new OrderHandler(orderService);

            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create());

            // Simple CORS for Angular localhost
            router.route().handler(ctx -> {
                ctx.response()
                    .putHeader("Access-Control-Allow-Origin", "http://localhost:4200")
                    .putHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS")
                    .putHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
                if ("OPTIONS".equals(ctx.request().method().name())) {
                    ctx.response().setStatusCode(204).end();
                } else {
                    ctx.next();
                }
            });

            // Simple health
            router.get("/api/health").handler(ctx -> ctx.json(new JsonObject().put("status", "ok")));

            // DB health: run a lightweight SELECT 1 to verify connection
            router.get("/api/db-health").handler(ctx ->
                client
                    .query("SELECT 1")
                    .execute(dbAr -> {
                        if (dbAr.succeeded()) {
                            ctx.json(new JsonObject()
                                .put("status", "ok")
                                .put("db", "connected"));
                        } else {
                            LOGGER.error("DB health check failed", dbAr.cause());
                            ctx.response()
                                .setStatusCode(500)
                                .putHeader("Content-Type", "application/json")
                                .end(new JsonObject()
                                    .put("status", "error")
                                    .put("db", "unreachable")
                                    .put("message", "Database connection failed")
                                    .encode());
                        }
                    })
            );

            // Orders
            router.post("/api/orders").handler(orderHandler::createOrder);

            vertx.createHttpServer()
                .requestHandler(router)
                .listen(httpPort)
                .onSuccess(server -> {
                    LOGGER.info("HTTP server started on port " + server.actualPort());
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
        });
    }

    private Promise<JsonObject> loadConfig() {
        ConfigStoreOptions envStore = new ConfigStoreOptions()
            .setType("env")
            .setConfig(new JsonObject().put("prefix", ""));

        ConfigStoreOptions fileStore = new ConfigStoreOptions()
            .setType("file")
            .setFormat("hocon")
            .setOptional(true)
            .setConfig(new JsonObject().put("path", "application.conf"));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
            .addStore(envStore)
            .addStore(fileStore);

        Promise<JsonObject> promise = Promise.promise();
        ConfigRetriever.create(vertx, options).getConfig(ar -> {
            if (ar.failed()) {
                promise.fail(ar.cause());
            } else {
                promise.complete(ar.result());
            }
        });
        return promise;
    }
}
