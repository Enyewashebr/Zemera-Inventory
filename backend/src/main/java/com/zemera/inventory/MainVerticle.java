package com.zemera.inventory;

import com.zemera.inventory.config.DatabaseConfig;
import com.zemera.inventory.handler.ProductHandler;
import com.zemera.inventory.service.ProductService;
import com.zemera.inventory.repository.ProductRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.sqlclient.Pool;
// import io.vertx.sqlclient.SqlClient;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {

        // 1️⃣ Create router
        Router router = Router.router(vertx);

        // 2️⃣ Enable CORS for Angular frontend (localhost:4200)
        router.route().handler(
            CorsHandler.create("http://localhost:4200") // allow requests only from Angular
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.DELETE)
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization")
        );

        // 3️⃣ Enable request body parsing for POST/PUT requests
        router.route().handler(BodyHandler.create());

        // 4️⃣ Health check endpoint
        router.get("/health").handler(ctx -> {
            ctx.json(new io.vertx.core.json.JsonObject()
                    .put("status", "UP")
                    .put("service", "inventory-backend"));
        });

        // 5️⃣ Simple test endpoint
        router.get("/hello").handler(ctx -> {
            ctx.response().end("Hello from Vert.x HTTP API 🚀");
        });

        // 6️⃣ Create database client
        Pool client = (Pool) DatabaseConfig.createClient(vertx);

        // 7️⃣ Test database connection
        client.query("SELECT 1").execute(ar -> {
            if (ar.succeeded()) {
                System.out.println("✅ PostgreSQL connection successful!");
            } else {
                System.out.println("❌ PostgreSQL connection failed: " + ar.cause().getMessage());
            }
        });

        // 8️⃣ Initialize repository, service, and handler for Products
        ProductRepository productRepository = new ProductRepository(client);
        ProductService productService = new ProductService(productRepository);
        ProductHandler productHandler = new ProductHandler(productService);

        // 9️⃣ Product API routes
        router.get("/api/products").handler(productHandler::getAllProducts); // GET all products
        router.post("/api/products").handler(productHandler::createProduct); // CREATE product
        router.put("/api/products/:id").handler(productHandler::updateProduct); // UPDATE product


        // 1️⃣0️⃣ Start HTTP server
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
    }
}
