package com.zemera.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zemera.inventory.config.DatabaseConfig;
import com.zemera.inventory.handler.UserAuthHandler;
import com.zemera.inventory.handler.ProductHandler;
import com.zemera.inventory.handler.BranchHandler;
import com.zemera.inventory.handler.OrderHandler;
import com.zemera.inventory.handler.PurchaseHandler;
import com.zemera.inventory.handler.StockHandler;
import com.zemera.inventory.repository.BranchRepository;
import com.zemera.inventory.repository.OrderRepository;
import com.zemera.inventory.repository.AuthRepository;
import com.zemera.inventory.repository.ProductRepository;
import com.zemera.inventory.repository.PurchaseRepository;
import com.zemera.inventory.repository.StockRepository;
import com.zemera.inventory.service.AuthService;
import com.zemera.inventory.service.OrderService;
import com.zemera.inventory.service.ProductService;
import com.zemera.inventory.service.PurchaseService;
import com.zemera.inventory.service.StockService;
import com.zemera.inventory.util.JwtUtil;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Base64;


public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {

        // ✅ FIX: Register JavaTime support (LocalDate, LocalDateTime)
        ObjectMapper mapper = DatabindCodec.mapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Router router = Router.router(vertx);

        // JWT Auth provider setup
     /* ---------------- JWT CONFIG (CRITICAL FIX) ---------------- */
 // Base64-encode the secret for JWK (must match JwtUtil)
String encodedSecret = Base64.getEncoder()
                             .encodeToString("my_super_secret_key_123456".getBytes());

JWTAuth jwtAuth = JWTAuth.create(vertx,
    new JWTAuthOptions()
        .addJwk(new JsonObject()
            .put("kty", "oct")       // symmetric key
            .put("alg", "HS256")     // must match Jwts signing
            .put("k", encodedSecret) // Base64-encoded
        )
);



JWTAuthHandler jwtAuthHandler = JWTAuthHandler.create(jwtAuth);





        // ✅ CORS configuration
        router.route().handler(
            CorsHandler.create("http://localhost:4200")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.DELETE)
                .allowedMethod(HttpMethod.OPTIONS) // <-- important
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization")
                .allowedHeader("Accept")
                .allowedHeader("X-Requested-With")
        );

        router.route().handler(BodyHandler.create());

        router.get("/health").handler(ctx ->
            ctx.json(new io.vertx.core.json.JsonObject()
                .put("status", "UP")
                .put("service", "inventory-backend"))
        );

        Pool client = (Pool) DatabaseConfig.createClient(vertx);

        // Product
        ProductRepository productRepo = new ProductRepository(client);
        ProductService productService = new ProductService(productRepo);
        ProductHandler productHandler = new ProductHandler(productService);


        // Stock
StockRepository stockRepo = new StockRepository(client);
StockService stockService = new StockService(stockRepo);
StockHandler stockHandler = new StockHandler(stockService);

// My stock route
router.get("/api/stock/my").handler(jwtAuthHandler).handler(stockHandler::getMyStock);
router.get("/api/stock/branch/:branchId").handler(stockHandler::getStockByBranch);

router.get("/api/stock/:productId")
      .handler(jwtAuthHandler)
      .handler(stockHandler::getStock);

router.post("/api/stock/increase")
      .handler(jwtAuthHandler)
      .handler(stockHandler::increaseStock);

        // Auth
       AuthRepository authRepo = new AuthRepository(client);
JwtUtil jwtUtil = new JwtUtil();
AuthService authService = new AuthService(authRepo, jwtUtil);
UserAuthHandler userAuthHandler = new UserAuthHandler(authService);

        // order
         // ===== Repositories =====
        // StockRepository stockRepo = new StockRepository(client);
        OrderRepository orderRepo = new OrderRepository(client);

        // ===== Services =====
        OrderService orderService = new OrderService(client, orderRepo, stockRepo);

        // ===== Handlers =====
        OrderHandler orderHandler = new OrderHandler(orderService);


    router.post("/api/orders")
      .handler(jwtAuthHandler)
      .handler(orderHandler::createOrder);

router.get("/api/orders/:id")
      .handler(jwtAuthHandler)
      .handler(orderHandler::getOrderTicket);

    


        // Purchase
        PurchaseRepository purchaseRepo = new PurchaseRepository(client);
        PurchaseService purchaseService = new PurchaseService(purchaseRepo, productRepo);
        PurchaseHandler purchaseHandler = new PurchaseHandler(purchaseService);

        // branch routes
        BranchRepository branchRepo = new BranchRepository(client);
        BranchHandler branchHandler = new BranchHandler(branchRepo);

        // Route to get all branches
        router.get("/api/branches").handler(branchHandler::getAllBranches);
        router.post("/api/branches").handler(branchHandler::createBranch);



        // product routes
        router.get("/api/products").handler(productHandler::getAllProducts);
        router.post("/api/products").handler(productHandler::createProduct);
        router.put("/api/products/:id").handler(productHandler::updateProduct);

        // user auth routes
        router.post("/api/auth/login").handler(userAuthHandler::loginUser);
        router.post("/api/create-user").handler(userAuthHandler::registerUser);
        router.get("/api/users").handler(userAuthHandler::getAllUsers);
        router.put("/api/users/:id").handler(userAuthHandler::updateUser);
        router.delete("/api/users/:id").handler(userAuthHandler::deleteUser);

        // purchase routes
        router.post("/api/purchase/create").handler(jwtAuthHandler).handler(purchaseHandler::createPurchase);

        // router.post("/api/purchase/create").handler(purchaseHandler::createPurchase);
        router.get("/api/purchase/getAll").handler(purchaseHandler::getAllPurchases);
        router.get("/api/purchase/getById/:id").handler(purchaseHandler::getPurchaseById);
        router.put("/api/purchase/update/:id").handler(purchaseHandler::updatePurchase);
        router.delete("/api/purchase/delete/:id").handler(purchaseHandler::deletePurchase);
        // router.get("/api/purchase/my").handler(JWTAuthHandler).handler(purchaseHandler::getMyPurchases);
        // router.get("/api/purchase/my").handler(authMiddleware).handler(purchaseHandler::getMyPurchases);
    //    router.get("/api/purchase/my").handler(jwtAuthHandler).handler(purchaseHandler::getMyPurchases);
       router.get("/api/purchase/my").handler(jwtAuthHandler).handler(purchaseHandler::getMyPurchases);

       router.put("/api/purchase/:id/approve").handler(jwtAuthHandler).handler(purchaseHandler::approve);
        router.put("/api/purchase/:id/decline").handler(jwtAuthHandler).handler(purchaseHandler::decline);






         router.get("/api/purchase/branch/:branchId").handler(purchaseHandler::getPurchasesByBranch);

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080, http -> {
                if (http.succeeded()) {
                    System.out.println("✅ HTTP server started on port 8080");

                    // Create default data after server starts
                    createDefaultData(client)
                        .onComplete(branchResult -> {
                            createDefaultProducts(client)
                                .onComplete(productResult -> {
                                    if (branchResult.succeeded() && productResult.succeeded()) {
                                        System.out.println("✅ Default data created successfully");
                                    } else {
                                        System.out.println("⚠️  Default data creation completed with some issues");
                                    }
                                    startPromise.complete();
                                });
                        });
                } else {
                    startPromise.fail(http.cause());
                }
            });
    }

    private Future<Void> createDefaultData(Pool client) {
        return client.preparedQuery("INSERT INTO branches (branch_name, phone) VALUES ($1, $2) ON CONFLICT (branch_name) DO NOTHING")
            .execute(Tuple.of("Test Branch", "123-456-7890"))
            .compose(branchResult -> {
                System.out.println("✅ Default branch created or already exists");
                return client.preparedQuery("SELECT id FROM branches WHERE branch_name = $1")
                    .execute(Tuple.of("Test Branch"));
            })
            .compose(idResult -> {
                if (idResult.size() > 0) {
                    Integer branchId = idResult.iterator().next().getInteger("id");

                    // Hash password
                    String hashedPassword = BCrypt.hashpw("password123", BCrypt.gensalt());

                    // Create default user with all required fields
                    String sql = """
                        INSERT INTO users(
                            full_name, username, password, email, phone,
                            role, branch_name, branch_id, created_at
                        )
                        VALUES ($1,$2,$3,$4,$5,$6,$7,$8,NOW())
                        ON CONFLICT (username) DO NOTHING
                    """;

                    return client.preparedQuery(sql)
                        .execute(Tuple.of(
                            "Test User",           // full_name
                            "testuser",            // username
                            hashedPassword,        // password
                            "test@example.com",    // email
                            "123-456-7890",        // phone
                            "BRANCH_MANAGER",      // role
                            "Test Branch",         // branch_name
                            branchId               // branch_id
                        ))
                        .map(userResult -> {
                            System.out.println("✅ Default user created: testuser/password123");
                            return null;
                        });
                } else {
                    return Future.failedFuture("Could not find created branch");
                }
            });
    }

    private Future<Void> createDefaultProducts(Pool client) {
        // Create some default products for testing
        String[] products = {
            "Laptop", "Mouse", "Keyboard", "Monitor", "Printer",
            "Scanner", "Webcam", "Headphones", "Speakers", "Microphone",
            "USB Drive", "External Hard Drive", "Router", "Switch", "Cable"
        };

        Future<Void> result = Future.succeededFuture();

        for (int i = 0; i < products.length; i++) {
            final String productName = products[i];
            final int productId = i + 1;
            final int index = i;

            result = result.compose(v ->
                client.preparedQuery("INSERT INTO products (id, name, unit, stock, buying_price, selling_price, sellable) VALUES ($1, $2, $3, $4, $5, $6, $7) ON CONFLICT (id) DO NOTHING")
                    .execute(Tuple.of(productId, productName, "piece", 100.0, 50.0 + index * 10, 75.0 + index * 15, true))
                    .compose(ar -> {
                        System.out.println("✅ Default product created: " + productName + " (ID: " + productId + ")");
                        return Future.succeededFuture((Void) null);
                    })
                    .recover(err -> {
                        System.out.println("⚠️  Product " + productName + " may already exist or failed: " + err.getMessage());
                        return Future.succeededFuture((Void) null);
                    })
            );
        }

        return result;
    }
}
