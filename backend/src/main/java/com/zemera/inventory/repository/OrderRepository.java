package com.zemera.inventory.repository;

import com.zemera.inventory.model.OrderItem;
import io.vertx.core.Future;
import io.vertx.sqlclient.*;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    private final Pool client;

    public OrderRepository(Pool client) {
        this.client = client;
    }

    /* ==========================
       INSERT ORDER
    ========================== */

    public Future<Long> insertOrder(SqlConnection conn,
                                    int branchId,
                                    String waiterName,
                                    double totalAmount) {

        String sql = """
            INSERT INTO orders (branch_id, waiter_name, total_amount)
            VALUES ($1, $2, $3)
            RETURNING id
        """;

        return conn
            .preparedQuery(sql)
            .execute(Tuple.of(branchId, waiterName, totalAmount))
            .map(rs -> rs.iterator().next().getLong("id"));
    }

    /* ==========================
       INSERT ORDER ITEM
    ========================== */

    public Future<Void> insertOrderItem(SqlConnection conn,
                                        long orderId,
                                        OrderItem item) {

        String sql = """
            INSERT INTO order_items
            (order_id, product_id, product_name, quantity, unit, unit_price, line_total)
            VALUES ($1, $2, $3, $4, $5, $6, $7)
        """;

        double lineTotal = item.getQuantity() * item.getUnitPrice();

        return conn
            .preparedQuery(sql)
            .execute(Tuple.of(
                orderId,
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnit(),
                item.getUnitPrice(),
                lineTotal
            ))
            .mapEmpty();
    }

    /* ==========================
       FETCH ORDER (REPRINT)
    ========================== */

    public Future<RowSet<Row>> getOrderWithItems(long orderId) {

        String sql = """
            SELECT
                o.id AS order_id,
                o.waiter_name,
                o.total_amount,
                o.created_at,

                i.product_name,
                i.quantity,
                i.unit,
                i.unit_price,
                i.line_total
            FROM orders o
            JOIN order_items i ON i.order_id = o.id
            WHERE o.id = $1
            ORDER BY i.id
        """;

        return client
            .preparedQuery(sql)
            .execute(Tuple.of(orderId));
    }
}
