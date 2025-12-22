package com.zemera.inventory;

import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public final class Launcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    private Launcher() {
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle())
            .onSuccess(id -> LOGGER.info("MainVerticle deployed with id: " + id))
            .onFailure(err -> {
                LOGGER.error("Failed to deploy MainVerticle", err);
                vertx.close();
            });
    }
}


