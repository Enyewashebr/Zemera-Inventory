package com.zemera.inventory.repository;

import com.zemera.inventory.model.Order;
import com.zemera.inventory.model.OrderItem;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    private final Pool client;

    public OrderRepository(Pool client) {
        this.client = client;
    }

    // Create a new order with items
    public Future<Order> create(Order order) {
        String insertOrderSql = """
            INSERT INTO orders (branch_id, waiter_id, total_amount, created_at)
            VALUES ($1, $2, $3, NOW())
            RETURNING id, created_at
        """;

        return client
            .preparedQuery(insertOrderSql)
            .execute(Tuple.of(order.getBranchId(), order.getTotalAmount()))
            .compose(rs -> {
                Row row = rs.iterator().next();
                order.setId(row.getLong("id"));
                order.setCreatedAt(row.getLocalDateTime("created_at"));

                // Insert order items
                Future<Void> itemsFuture = Future.succeededFuture();
                for (OrderItem item : order.getItems()) {
                    String insertItemSql = """
                        INSERT INTO order_items
                        (order_id, product_id, quantity, unit_price, total_price)
                        VALUES ($1, $2, $3, $4, $5)
                    """;

                    item.calculateTotalPrice(); // auto-calc total price

                    itemsFuture = itemsFuture.compose(v ->
                        client.preparedQuery(insertItemSql)
                              .execute(Tuple.of(order.getId(), item.getProductId(),
                                                  item.getQuantity(),
                                                  item.getUnitPrice(),
                                                  item.getTotalPrice()))
                              .mapEmpty()
                    );
                }

                return itemsFuture.map(v -> order);
            });
    }

    // Get all orders
    public Future<List<Order>> getAll() {
        String sql = "SELECT id, branch_id, waiter_id, total_amount, created_at FROM orders ORDER BY created_at DESC";
        return client.query(sql)
                     .execute()
                     .map(rs -> {
                         List<Order> orders = new ArrayList<>();
                         for (Row row : rs) {
                             Order o = new Order();
                             o.setId(row.getLong("id"));
                             o.setBranchId(row.getInteger("branch_id"));
                            //  o.setWaiterId(row.getLong("waiter_id"));
                             o.setTotalAmount(row.getDouble("total_amount"));
                             o.setCreatedAt(row.getLocalDateTime("created_at"));
                             orders.add(o);
                         }
                         return orders;
                     });
    }

    // Get order by ID
    public Future<Order> getById(Long orderId) {
        String orderSql = "SELECT id, branch_id, waiter_id, total_amount, created_at FROM orders WHERE id=$1";
        String itemsSql = "SELECT product_id, quantity, unit_price, total_price FROM order_items WHERE order_id=$1";

        return client.preparedQuery(orderSql)
                     .execute(Tuple.of(orderId))
                     .compose(rs -> {
                         if (!rs.iterator().hasNext()) return Future.succeededFuture(null);
                         Row row = rs.iterator().next();
                         Order order = new Order();
                         order.setId(row.getLong("id"));
                         order.setBranchId(row.getInteger("branch_id"));
                        //  order.setWaiterId(row.getLong("waiter_id"));
                         order.setTotalAmount(row.getDouble("total_amount"));
                         order.setCreatedAt(row.getLocalDateTime("created_at"));

                         return client.preparedQuery(itemsSql)
                                      .execute(Tuple.of(orderId))
                                      .map(itemsRs -> {
                                          List<OrderItem> items = new ArrayList<>();
                                          for (Row itemRow : itemsRs) {
                                              OrderItem item = new OrderItem();
                                              item.setProductId(itemRow.getLong("product_id"));
                                              item.setQuantity(itemRow.getDouble("quantity"));
                                              item.setUnitPrice(itemRow.getDouble("unit_price"));
                                            //   item.setTotalPrice(itemRow.getDouble("total_price"));
                                              items.add(item);
                                          }
                                          order.setItems(items);
                                          return order;
                                      });
                     });
    }
}
