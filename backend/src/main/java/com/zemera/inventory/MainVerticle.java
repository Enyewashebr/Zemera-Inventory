package com.zemera.inventory;

import com.zemera.inventory.config.DatabaseConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlClient;

public class MainVerticle extends AbstractVerticle {

    @Override
     public void start(Promise<Void> startPromise) {

    Router router = Router.router(vertx);

    // Enable request body parsing (for POST/PUT)
    router.route().handler(BodyHandler.create());

    // Health check
    router.get("/health").handler(ctx -> {
      ctx.json(
        new io.vertx.core.json.JsonObject()
          .put("status", "UP")
          .put("service", "inventory-backend")
      );
    });

    // Simple test endpoint
    router.get("/hello").handler(ctx -> {
      ctx.response().end("Hello from Vert.x HTTP API 🚀");
    });

    // Start HTTP server
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8080, http -> {
        if (http.succeeded()) {
          System.out.println("✅ HTTP server started on port 8080");
          startPromise.complete();
        } else {
          startPromise.fail(http.cause());
        }
      });

        // Create the client
        SqlClient client = DatabaseConfig.createClient(vertx);

        // Test connection
        client.query("SELECT 1").execute(ar -> {
            if (ar.succeeded()) {
                System.out.println("✅ PostgreSQL connection successful!");
            } else {
                System.out.println("❌ PostgreSQL connection failed: " + ar.cause().getMessage());
            }
        });


        
    }
}
