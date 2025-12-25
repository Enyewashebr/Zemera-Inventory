package com.zemera.inventory.config;


import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlClient;

public class DatabaseConfig {

    public static SqlClient createClient(Vertx vertx) {

        PgConnectOptions connectOptions = new PgConnectOptions()
            .setHost("127.0.0.1")
            .setPort(5432)
            .setDatabase("zemera_inventory")
            .setUser("zemera_user")
            .setPassword("zemera123");

        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(5);

        return PgPool.pool(vertx, connectOptions, poolOptions);
    }
}
