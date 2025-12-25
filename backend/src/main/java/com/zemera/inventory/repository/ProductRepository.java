package com.zemera.inventory.repository;

import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class ProductRepository {

    private final PgPool client;

    public ProductRepository(PgPool client) {
        this.client = client;
    }

    public Future<Row> findByName(String name) {
        String sql = "SELECT id, name, unit, current_stock, selling_price FROM product WHERE name = $1";
        return client
            .preparedQuery(sql)
            .execute(Tuple.of(name))
            .map((RowSet<Row> rows) -> rows.size() == 0 ? null : rows.iterator().next());
    }

    public Future<Void> decreaseStock(long productId, double quantity) {
        String sql = "UPDATE product SET current_stock = current_stock - $1 " +
                     "WHERE id = $2 AND current_stock >= $1";
        return client
            .preparedQuery(sql)
            .execute(Tuple.of(quantity, productId))
            .compose(rows -> {
                if (rows.rowCount() == 0) {
                    return Future.failedFuture("INSUFFICIENT_STOCK");
                }
                return Future.succeededFuture();
            });
    }
}



