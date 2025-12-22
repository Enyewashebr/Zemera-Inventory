package com.zemera.inventory;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
// import io.vertx.ext.web.Router;
// import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private void loadConfig(Handler<AsyncResult<JsonObject>> handler) {

  ConfigStoreOptions fileStore = new ConfigStoreOptions()
    .setType("file")
    .setFormat("json")
    .setConfig(new JsonObject().put("path", "application.json"));

  ConfigRetrieverOptions options = new ConfigRetrieverOptions()
    .addStore(fileStore);

  ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

  retriever.getConfig(handler);
}


    private Promise<JsonObject> loadConfig() {
        ConfigStoreOptions envStore = new ConfigStoreOptions()
            .setType("env")
            .setConfig(new JsonObject().put("prefix", ""));

        ConfigStoreOptions fileStore = new ConfigStoreOptions()
            .setType("file")
            .setFormat("hocon")
            .setOptional(true)
            .setConfig(new JsonObject().put("path", "application.j"));

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


