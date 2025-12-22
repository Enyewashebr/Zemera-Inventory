package com.zemera.inventory;

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

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        loadConfig()
            .compose(cfg -> {
                int httpPort = cfg.getInteger("http.port", 8080);
                Router router = Router.router(vertx);
                router.route().handler(BodyHandler.create());
                router.get("/health").handler(ctx -> ctx.json(new JsonObject().put("status", "ok")));
                // Placeholder root endpoint to verify server is running
                router.get("/").handler(ctx -> ctx.response().end("Zemera Inventory backend up"));

                return vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(httpPort)
                    .onSuccess(server -> LOGGER.info("HTTP server started on port " + server.actualPort()));
            })
            .onSuccess(ignored -> startPromise.complete())
            .onFailure(startPromise::fail);
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


