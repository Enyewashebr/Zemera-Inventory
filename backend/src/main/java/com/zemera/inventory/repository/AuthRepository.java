
package com.zemera.inventory.repository;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class AuthRepository {

    private final Pool client;

    public AuthRepository(Pool client) {
        this.client = client;
    }

    // Return Future<JsonObject> instead of using Handler
    public Future<JsonObject> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = $1";

        return client.preparedQuery(sql)
                     .execute(Tuple.of(username))
                     .map(rows -> {
                         if (!rows.iterator().hasNext()) {
                             return null;
                         }
                         Row row = rows.iterator().next();
                         return row.toJson();
                     });
    }
}
