package com.zemera.inventory.config;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlClient;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConfig {

    private static final Dotenv dotenv = Dotenv.load(); // Load .env file
    public static SqlClient createClient(Vertx vertx) {

        PgConnectOptions connectOptions = new PgConnectOptions()
            .setHost(dotenv.get("DB_HOST"))
            .setPort(Integer.parseInt(dotenv.get("DB_PORT")))
            .setDatabase(dotenv.get("DB_NAME"))
            .setUser(dotenv.get("DB_USER"))
            .setPassword(dotenv.get("DB_PASSWORD"));

        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(Integer.parseInt(dotenv.get("DB_POOL_SIZE")));

        return PgPool.pool(vertx, connectOptions, poolOptions);
    }
}
