package com.zemera.inventory.db;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public final class PgClientFactory {

    private PgClientFactory() {
    }

    public static PgPool createPool(Vertx vertx, JsonObject dbConfig) {
        String host = dbConfig.getString("host", "localhost");
        int port = dbConfig.getInteger("port", 5432);
        String database = dbConfig.getString("database", "postgres");
        String user = dbConfig.getString("user", "postgres");
        String password = dbConfig.getString("password", "postgres");

        PgConnectOptions connectOptions = new PgConnectOptions()
            .setHost(host)
            .setPort(port)
            .setDatabase(database)
            .setUser(user)
            .setPassword(password);

        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(10);

        return PgPool.pool(vertx, connectOptions, poolOptions);
    }
}



