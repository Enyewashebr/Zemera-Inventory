// package com.zemera.inventory.config;

// import io.vertx.core.Vertx;
// import io.vertx.pgclient.PgConnectOptions;
// import io.vertx.sqlclient.PoolOptions;
// import io.vertx.pgclient.PgPool;
// import io.vertx.sqlclient.SqlClient;
// import io.github.cdimascio.dotenv.Dotenv;

// public class DatabaseConfig {

//     private static String env(String key) {
//     String value = System.getenv(key);
//     if (value == null) value = Dotenv.load().get(key);
//     return value;
// }


//     private static final Dotenv dotenv = Dotenv.load(); // Load .env file
//     public static SqlClient createClient(Vertx vertx) {

//         PgConnectOptions connectOptions = new PgConnectOptions()
//             .setHost(dotenv.get("DB_HOST"))
//             .setPort(Integer.parseInt(dotenv.get("DB_PORT")))
//             .setDatabase(dotenv.get("DB_NAME"))
//             .setUser(dotenv.get("DB_USER"))
//             .setPassword(dotenv.get("DB_PASSWORD"));

//         PoolOptions poolOptions = new PoolOptions()
//             .setMaxSize(Integer.parseInt(dotenv.get("DB_POOL_SIZE")));

//         return PgPool.pool(vertx, connectOptions, poolOptions);
//     }
// }

// todayyyyyyyyyyyy
// package com.zemera.inventory.config;

// import io.vertx.core.Vertx;
// import io.vertx.pgclient.PgConnectOptions;
// import io.vertx.pgclient.PgPool;
// import io.vertx.sqlclient.PoolOptions;
// import io.vertx.sqlclient.SqlClient;
// import io.github.cdimascio.dotenv.Dotenv;

// public class DatabaseConfig {

//     private static String env(String key) {
//         String value = System.getenv(key);
//         if (value == null) {
//             value = Dotenv.load().get(key);
//         }
//         if (value == null) {
//             throw new RuntimeException("Missing environment variable: " + key);
//         }
//         return value;
//     }

//     public static SqlClient createClient(Vertx vertx) {

//         PgConnectOptions connectOptions = new PgConnectOptions()
//             .setHost(env("DB_HOST"))
//             .setPort(Integer.parseInt(env("DB_PORT")))
//             .setDatabase(env("DB_NAME"))
//             .setUser(env("DB_USER"))
//             .setPassword(env("DB_PASSWORD"));

//         PoolOptions poolOptions = new PoolOptions()
//             .setMaxSize(Integer.parseInt(env("DB_POOL_SIZE")));

//         return PgPool.pool(vertx, connectOptions, poolOptions);
//     }
// }


package com.zemera.inventory.config;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConfig {

    private static String env(String key) {
        String value = System.getenv(key);
        if (value == null) {
            value = Dotenv.load().get(key);
        }
        if (value == null) {
            throw new RuntimeException("Missing environment variable: " + key);
        }
        return value;
    }

    public static SqlClient createClient(Vertx vertx) {

        PgConnectOptions connectOptions = new PgConnectOptions()
            .setHost(env("DB_HOST"))
            .setPort(Integer.parseInt(env("DB_PORT")))
            .setDatabase(env("DB_NAME"))
            .setUser(env("DB_USER"))
            .setPassword(env("DB_PASSWORD"))
            .setSslMode(SslMode.REQUIRE)  // 🔥 REQUIRED FOR NEON
            .setSsl(true)                    // ✅ ADD THIS
            .setTrustAll(true)
            .setHostnameVerificationAlgorithm(""); 

        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(Integer.parseInt(env("DB_POOL_SIZE")));

        return PgPool.pool(vertx, connectOptions, poolOptions);
    }
}
