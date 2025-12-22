package com.zemera.inventory.repository;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

public class OrderRepository {

    private final PgPool client;

    public OrderRepository(PgPool client) {
        this.client = client;
    }

    /**
     * Creates an order and items and decreases stock in a single transaction.
     * Expects a payload:
     * {
     *   waiterName: string,
     *   items: [{ productName, quantity }]
     * }
     */
    public Future<JsonObject> createOrder(JsonObject payload) {
        String waiterName = payload.getString("waiterName");
        JsonArray items = payload.getJsonArray("items", new JsonArray());

        if (waiterName == null || waiterName.isBlank() || items.isEmpty()) {
            return Future.failedFuture("INVALID_PAYLOAD");
        }

        return client.withTransaction(conn -> doCreateOrder(conn, waiterName, items));
    }

    private Future<JsonObject> doCreateOrder(SqlConnection conn, String waiterName, JsonArray items) {
        // 1) Insert order header with temporary zero total
        String insertOrderSql = "INSERT INTO \"order\" (waiter_name, total_amount) VALUES ($1, 0) RETURNING id, order_datetime";

        return conn
            .preparedQuery(insertOrderSql)
            .execute(Tuple.of(waiterName))
            .compose(orderRows -> {
                Row orderRow = orderRows.iterator().next();
                long orderId = orderRow.getLong("id");
                java.time.OffsetDateTime createdAt = orderRow.getOffsetDateTime("order_datetime");

                // 2) For each item, insert order_item and decrease stock
                Future<Void> chain = Future.succeededFuture((Void) null);
                final double[] totalAmount = {0.0};
                JsonArray ticketItems = new JsonArray();

                for (int i = 0; i < items.size(); i++) {
                    JsonObject item = items.getJsonObject(i);
                    String productName = item.getString("productName");
                    double quantity = item.getDouble("quantity", 0.0);

                    if (productName == null || quantity <= 0) {
                        return Future.failedFuture("INVALID_ITEM");
                    }

                    final String pn = productName;
                    final double q = quantity;
                    chain = chain.compose(v ->
                        addOrderItemAndDecreaseStock(conn, orderId, pn, q)
                            .map(itemJson -> {
                                double lineTotal = itemJson.getDouble("lineTotal");
                                totalAmount[0] += lineTotal;
                                ticketItems.add(itemJson);
                                return (Void) null;
                            })
                    );
                }

                return chain.compose(v -> {
                    // 3) Update order total
                    String updateTotalSql = "UPDATE \"order\" SET total_amount = $1 WHERE id = $2";
                    return conn
                        .preparedQuery(updateTotalSql)
                        .execute(Tuple.of(totalAmount[0], orderId))
                        .map(r -> {
                            JsonObject ticket = new JsonObject()
                                .put("orderId", orderId)
                                .put("waiterName", waiterName)
                                .put("createdAt", createdAt)
                                .put("currency", "ETB")
                                .put("totalAmount", totalAmount[0])
                                .put("items", ticketItems);
                            return ticket;
                        });
                });
            });
    }

    private Future<JsonObject> addOrderItemAndDecreaseStock(SqlConnection conn, long orderId, String productName, double quantity) {
        // Find product by name
        String findSql = "SELECT id, unit, selling_price FROM product WHERE name = $1";
        return conn
            .preparedQuery(findSql)
            .execute(Tuple.of(productName))
            .compose((RowSet<Row> rows) -> {
                if (rows.size() == 0) {
                    return Future.failedFuture("PRODUCT_NOT_FOUND");
                }
                Row row = rows.iterator().next();
                long productId = row.getLong("id");
                String unit = row.getString("unit");
                double unitPrice = row.getNumeric("selling_price").doubleValue();
                double lineTotal = unitPrice * quantity;

                String insertItemSql =
                    "INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES ($1, $2, $3, $4)";
                String updateStockSql =
                    "UPDATE product SET current_stock = current_stock - $1 WHERE id = $2 AND current_stock >= $1";

                return conn
                    .preparedQuery(updateStockSql)
                    .execute(Tuple.of(quantity, productId))
                    .compose(updateRows -> {
                        if (updateRows.rowCount() == 0) {
                            return Future.failedFuture("INSUFFICIENT_STOCK");
                        }
                        return conn
                            .preparedQuery(insertItemSql)
                            .execute(Tuple.of(orderId, productId, quantity, unitPrice))
                            .map(v -> new JsonObject()
                                .put("productName", productName)
                                .put("quantity", quantity)
                                .put("unit", unit)
                                .put("unitPrice", unitPrice)
                                .put("lineTotal", lineTotal)
                            );
                    });
            });
    }
}


