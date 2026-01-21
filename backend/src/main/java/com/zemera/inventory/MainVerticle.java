package com.zemera.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zemera.inventory.config.DatabaseConfig;
import com.zemera.inventory.handler.UserAuthHandler;
import com.zemera.inventory.handler.ProductHandler;
import com.zemera.inventory.handler.BranchHandler;
import com.zemera.inventory.handler.OrderHandler;
import com.zemera.inventory.handler.PurchaseHandler;
import com.zemera.inventory.handler.ReportsHandler;
import com.zemera.inventory.handler.StockHandler;
import com.zemera.inventory.repository.BranchRepository;
import com.zemera.inventory.repository.OrderRepository;
import com.zemera.inventory.repository.AuthRepository;
import com.zemera.inventory.repository.ProductRepository;
import com.zemera.inventory.repository.PurchaseRepository;
import com.zemera.inventory.repository.ReportsRepository;
import com.zemera.inventory.repository.StockRepository;
import com.zemera.inventory.service.AuthService;
import com.zemera.inventory.service.OrderService;
import com.zemera.inventory.service.ProductService;
import com.zemera.inventory.service.PurchaseService;
import com.zemera.inventory.service.ReportsService;
import com.zemera.inventory.service.StockService;
import com.zemera.inventory.util.JwtUtil;
import io.vertx.core.AbstractVerticle;
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
import java.util.Base64;


public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {

    //   Jackson ObjectMapper setup
        ObjectMapper mapper = DatabindCodec.mapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Router router = Router.router(vertx);

        //  JWT Auth setup
        String encodedSecret = Base64.getEncoder().encodeToString("my_super_secret_key_123456".getBytes());

        JWTAuth jwtAuth = JWTAuth.create(vertx,
            new JWTAuthOptions()
                .addJwk(new JsonObject()
                .put("kty", "oct")       // symmetric key
                .put("alg", "HS256")     // must match Jwts signing
                .put("k", encodedSecret) // Base64-encoded
                )
        );

        //  CORS configuration
        router.route().handler(
            CorsHandler.create("http://localhost:4200")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.DELETE)
                .allowedMethod(HttpMethod.OPTIONS) 
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization")
                .allowedHeader("Accept")
                .allowedHeader("X-Requested-With")
        );

        // Body handler
        router.route().handler(BodyHandler.create());

        router.get("/health").handler(ctx ->
            ctx.json(new io.vertx.core.json.JsonObject()
                .put("status", "UP")
                .put("service", "inventory-backend"))
        );

            // Database client
        Pool client = (Pool) DatabaseConfig.createClient(vertx);

        // Product
        ProductRepository productRepo = new ProductRepository(client);
        ProductService productService = new ProductService(productRepo);
        ProductHandler productHandler = new ProductHandler(productService);


        // Stock
        StockRepository stockRepo = new StockRepository(client);
        StockService stockService = new StockService(stockRepo);
        StockHandler stockHandler = new StockHandler(stockService);
        
        // Auth
        JWTAuthHandler jwtAuthHandler = JWTAuthHandler.create(jwtAuth);
        AuthRepository authRepo = new AuthRepository(client);
        JwtUtil jwtUtil = new JwtUtil();
        AuthService authService = new AuthService(authRepo, jwtUtil);
        UserAuthHandler userAuthHandler = new UserAuthHandler(authService);

        // order
        OrderRepository orderRepo = new OrderRepository(client);
        OrderService orderService = new OrderService(client, orderRepo, stockRepo);
        OrderHandler orderHandler = new OrderHandler(orderService);

        // Purchase
        PurchaseRepository purchaseRepo = new PurchaseRepository(client);
        PurchaseService purchaseService = new PurchaseService(purchaseRepo, productRepo);
        PurchaseHandler purchaseHandler = new PurchaseHandler(purchaseService);

        // Branch
        BranchRepository branchRepo = new BranchRepository(client);
        BranchHandler branchHandler = new BranchHandler(branchRepo);

        // Report
        // ReportRepository reportRepo =new ReportRepository(client);
        // ReportService reportService =new ReportService(reportRepo);
        // ReportHandler reportHandler =new ReportHandler(reportService);

        ReportsRepository reportRepo = new ReportsRepository(client);
        ReportsService reportService = new ReportsService(reportRepo);
        ReportsHandler handler = new ReportsHandler(reportService);

        router.get("/api/reports/sales").handler(jwtAuthHandler).handler(handler::getSales);
        router.get("/api/reports/purchases").handler(jwtAuthHandler).handler(handler::getPurchaseReport);
        router.get("/api/reports/profit").handler(jwtAuthHandler).handler(handler::getProfit);


        // branch routes
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
        router.get("/api/purchase/getAll").handler(purchaseHandler::getAllPurchases);
        router.get("/api/purchase/getById/:id").handler(purchaseHandler::getPurchaseById);
        router.put("/api/purchase/update/:id").handler(purchaseHandler::updatePurchase);
        router.delete("/api/purchase/delete/:id").handler(purchaseHandler::deletePurchase);
        router.get("/api/purchase/my").handler(jwtAuthHandler).handler(purchaseHandler::getMyPurchases);
        router.put("/api/purchase/:id/approve").handler(jwtAuthHandler).handler(purchaseHandler::approve);
        router.put("/api/purchase/:id/decline").handler(jwtAuthHandler).handler(purchaseHandler::decline);
        router.get("/api/purchase/branch/:branchId").handler(purchaseHandler::getPurchasesByBranch);

        // My stock route
        router.get("/api/stock/my").handler(jwtAuthHandler).handler(stockHandler::getMyStock);
        router.get("/api/stock/branch/:branchId").handler(stockHandler::getStockByBranch);
        router.get("/api/stock/:productId").handler(jwtAuthHandler).handler(stockHandler::getStock);
        router.post("/api/stock/increase").handler(jwtAuthHandler).handler(stockHandler::increaseStock);

         // order routes
        router.post("/api/orders").handler(jwtAuthHandler).handler(orderHandler::createOrder);
        router.get("/api/orders/:id").handler(jwtAuthHandler).handler(orderHandler::getOrderTicket);

        // report routes
        // router.get("/api/reports/daily-sales").handler(JWTAuthHandler.create(jwtAuth)).handler(reportHandler::dailySales);
        // router.get("/api/reports/purchase").handler(JWTAuthHandler.create(jwtAuth)).handler(reportHandler::dailyPurchase);
        // router.get("/api/reports/purchase/monthly").handler(JWTAuthHandler.create(jwtAuth)).handler(reportHandler::monthlyPurchase);
        // router.get("/api/reports/profit").handler(JWTAuthHandler.create(jwtAuth)).handler(reportHandler::profit);

        // router.get("/api/reports/sales").handler(jwtAuthHandler).handler(reportHandler::sales);
        // router.get("/api/reports/purchase").handler(jwtAuthHandler).handler(reportHandler::purchase);
        // router.get("/api/reports/profit").handler(jwtAuthHandler).handler(reportHandler::profit);



        //  Start HTTP server
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080, http -> {
                if (http.succeeded()) {
                    System.out.println(" HTTP server started on port 8080");
                } else {
                    startPromise.fail(http.cause());
                }
            });
    }

    
}
