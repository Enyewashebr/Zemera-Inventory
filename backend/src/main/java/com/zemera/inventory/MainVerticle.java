package com.zemera.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zemera.inventory.config.DatabaseConfig;
import com.zemera.inventory.handler.AuthHandler;
import com.zemera.inventory.handler.ProductHandler;
import com.zemera.inventory.handler.PurchaseHandler;
import com.zemera.inventory.repository.AuthRepository;
import com.zemera.inventory.repository.ProductRepository;
import com.zemera.inventory.repository.PurchaseRepository;
import com.zemera.inventory.service.AuthService;
import com.zemera.inventory.service.ProductService;
import com.zemera.inventory.service.PurchaseService;
import com.zemera.inventory.util.JwtUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.sqlclient.Pool;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {

        // ✅ FIX: Register JavaTime support (LocalDate, LocalDateTime)
        ObjectMapper mapper = DatabindCodec.mapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Router router = Router.router(vertx);

        router.route().handler(
            CorsHandler.create("http://localhost:4200")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.DELETE)
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization")
        );

        router.route().handler(BodyHandler.create());

        router.get("/health").handler(ctx ->
            ctx.json(new io.vertx.core.json.JsonObject()
                .put("status", "UP")
                .put("service", "inventory-backend"))
        );

        Pool client = (Pool) DatabaseConfig.createClient(vertx);

        client.query("SELECT 1").execute(ar -> {
            if (ar.succeeded()) {
                System.out.println("✅ PostgreSQL connection successful!");
            } else {
                System.out.println("❌ PostgreSQL connection failed: " + ar.cause().getMessage());
            }
        });

        // Product
        ProductRepository productRepo = new ProductRepository(client);
        ProductService productService = new ProductService(productRepo);
        ProductHandler productHandler = new ProductHandler(productService);

        // Auth
        AuthRepository authRepo = new AuthRepository(client);
        JwtUtil jwtUtil = new JwtUtil();
        AuthService authService = new AuthService(authRepo, jwtUtil);
        AuthHandler authHandler = new AuthHandler(authService);

        // Purchase
        PurchaseRepository purchaseRepo = new PurchaseRepository(client);
        PurchaseService purchaseService = new PurchaseService(purchaseRepo);
        PurchaseHandler purchaseHandler = new PurchaseHandler(purchaseService);

        router.get("/api/products").handler(productHandler::getAllProducts);
        router.post("/api/products").handler(productHandler::createProduct);
        router.put("/api/products/:id").handler(productHandler::updateProduct);

        router.post("/api/auth/login").handler(authHandler::loginUser);
        router.post("/api/auth/create").handler(authHandler::registerUser);

        router.post("/api/purchase/create").handler(purchaseHandler::createPurchase);
        router.get("/api/purchase/getAll").handler(purchaseHandler::getAllPurchases);
        router.get("/api/purchase/getById/:id").handler(purchaseHandler::getPurchaseById);
        router.put("/api/purchase/update/:id").handler(purchaseHandler::updatePurchase);
        router.delete("/api/purchase/delete/:id").handler(purchaseHandler::deletePurchase);

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
