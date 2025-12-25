package com.zemera.inventory;

import com.zemera.inventory.DB.PgClientFactory;
import com.zemera.inventory.handler.OrderHandler;
import com.zemera.inventory.repository.OrderRepository;
import com.zemera.inventory.repository.ProductRepository;
import com.zemera.inventory.service.OrderService;
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
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.pgclient.PgPool;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        loadConfig()
            .compose(config -> {
                // Create PostgreSQL connection pool
                JsonObject dbConfig = config.getJsonObject("db", new JsonObject());
                PgPool pgPool = PgClientFactory.createPool(vertx, dbConfig);

                // Create repositories
                ProductRepository productRepository = new ProductRepository(pgPool);
                OrderRepository orderRepository = new OrderRepository(pgPool);

                // Create services
                OrderService orderService = new OrderService(productRepository, orderRepository);

                // Create handlers
                OrderHandler orderHandler = new OrderHandler(orderService);

                // Create HTTP server and router
                Router router = Router.router(vertx);

                // Enable CORS for Angular frontend
                router.route().handler(CorsHandler.create()
                    .addOrigin("http://localhost:4200")
                    .allowedMethods(java.util.Set.of(
                        io.vertx.core.http.HttpMethod.GET,
                        io.vertx.core.http.HttpMethod.POST,
                        io.vertx.core.http.HttpMethod.PUT,
                        io.vertx.core.http.HttpMethod.DELETE,
                        io.vertx.core.http.HttpMethod.OPTIONS
                    ))
                    .allowedHeaders(java.util.Set.of("Content-Type", "Authorization"))
                    .allowCredentials(true));

                // Enable JSON body parsing
                router.route().handler(BodyHandler.create());

                // Health check endpoints
                router.get("/api/health").handler(ctx -> {
                    ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("status", "ok").encode());
                });

                router.get("/api/db-health").handler(ctx -> {
                    pgPool.query("SELECT 1")
                        .execute()
                        .onSuccess(rows -> {
                            ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .end(new JsonObject()
                                    .put("status", "ok")
                                    .put("db", "connected")
                                    .encode());
                        })
                        .onFailure(err -> {
                            LOGGER.error("DB health check failed", err);
                            ctx.response()
                                .setStatusCode(500)
                                .putHeader("Content-Type", "application/json")
                                .end(new JsonObject()
                                    .put("status", "error")
                                    .put("db", "unreachable")
                                    .put("message", "Database connection failed: " + err.getMessage())
                                    .encode());
                        });
                });

                // Order endpoints
                router.post("/api/orders").handler(orderHandler::createOrder);

                // Start HTTP server
                int port = config.getJsonObject("http", new JsonObject()).getInteger("port", 8080);
                return vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(port)
                    .mapEmpty();
            })
            .onSuccess(v -> {
                LOGGER.info("HTTP server started on port " + config().getJsonObject("http", new JsonObject()).getInteger("port", 8080));
                startPromise.complete();
            })
            .onFailure(err -> {
                LOGGER.error("Failed to start server", err);
                startPromise.fail(err);
            });
    }

    private io.vertx.core.Future<JsonObject> loadConfig() {
        ConfigStoreOptions envStore = new ConfigStoreOptions()
            .setType("env")
            .setConfig(new JsonObject().put("prefix", ""));

        ConfigStoreOptions fileStore = new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setOptional(true)
            .setConfig(new JsonObject().put("path", "src/main/resources/application.json"));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
            .addStore(envStore)
            .addStore(fileStore);

        return ConfigRetriever.create(vertx, options).getConfig();
            }
}
