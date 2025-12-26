package com.zemera.inventory.repository;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private final Pool client;

    public ProductRepository(Pool client) {
        this.client = client;
    }

    // Create a new product
    public void createProduct(JsonObject productData, Handler<AsyncResult<JsonObject>> resultHandler) {
        String sql = "INSERT INTO products " +
                "(name, category_id, subcategory, unit, buying_price, selling_price, stock, sellable, created_at) " +
                "VALUES ($1, $2, $3, $4, $5, $6, $7, $8, NOW()) " +
                "RETURNING id, name, category_id, subcategory, unit, buying_price, selling_price, stock, sellable, created_at";

        client.preparedQuery(sql).execute(
            Tuple.of(
                productData.getString("name"),
                productData.getString("categoryId"),
                productData.getString("subcategory"),
                productData.getString("unit"),
                productData.getDouble("buyingPrice"),
                productData.getDouble("sellingPrice"),
                productData.getDouble("stock"),
                productData.getBoolean("sellable")
            ),
            ar -> {
                if (ar.succeeded()) {
                    RowSet<Row> rows = ar.result();
                    Row row = rows.iterator().next();
                    JsonObject createdProduct = new JsonObject()
                        .put("id", row.getInteger("id"))
                        .put("name", row.getString("name"))
                        .put("categoryId", row.getString("category_id"))
                        .put("subcategory", row.getString("subcategory"))
                        .put("unit", row.getString("unit"))
                        .put("buyingPrice", row.getDouble("buying_price"))
                        .put("sellingPrice", row.getDouble("selling_price"))
                        .put("stock", row.getDouble("stock"))
                        .put("sellable", row.getBoolean("sellable"))
                        .put("createdAt", row.getLocalDateTime("created_at").toString()); // <-- changed
                    resultHandler.handle(io.vertx.core.Future.succeededFuture(createdProduct));
                } else {
                    resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
                }
            }
        );
    }

    // Get all products
    public void getAllProducts(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        String sql = "SELECT id, name, category_id, subcategory, unit, buying_price, selling_price, stock, sellable, created_at " +
                     "FROM products ORDER BY created_at DESC";

        client.query(sql).execute(ar -> {
            if (ar.succeeded()) {
                List<JsonObject> products = new ArrayList<>();
                for (Row row : ar.result()) {
                    JsonObject p = new JsonObject()
                        .put("id", row.getInteger("id"))
                        .put("name", row.getString("name"))
                        .put("categoryId", row.getString("category_id"))
                        .put("subcategory", row.getString("subcategory"))
                        .put("unit", row.getString("unit"))
                        .put("buyingPrice", row.getDouble("buying_price"))
                        .put("sellingPrice", row.getDouble("selling_price"))
                        .put("stock", row.getDouble("stock"))
                        .put("sellable", row.getBoolean("sellable"))
                        .put("createdAt", row.getLocalDateTime("created_at").toString()); // <-- changed
                    products.add(p);
                }
                resultHandler.handle(io.vertx.core.Future.succeededFuture(products));
            } else {
                resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
            }
        });
    }


    // Update product
  public Future<JsonObject> updateProduct(int id, JsonObject data) {
    String sql = """
        UPDATE products SET
            name = $1,
            category_id = $2,
            subcategory = $3,
            unit = $4,
            buying_price = $5,
            selling_price = $6,
            stock = $7,
            sellable = $8
        WHERE id = $9
        RETURNING id, name, category_id, subcategory, unit,
                  buying_price, selling_price, stock, sellable, created_at
    """;

    return client
        .preparedQuery(sql)
        .execute(Tuple.of(
            data.getString("name"),
            data.getString("categoryId"),
            data.getString("subcategory"),
            data.getString("unit"),
            data.getDouble("buyingPrice"),
            data.getDouble("sellingPrice"),
            data.getDouble("stock"),
            data.getBoolean("sellable"),
            id
        ))
        .map(rows -> {
            if (!rows.iterator().hasNext()) {
                throw new RuntimeException("Product not found");
            }

            Row row = rows.iterator().next();
            return new JsonObject()
                .put("id", row.getInteger("id"))
                .put("name", row.getString("name"))
                .put("categoryId", row.getString("category_id"))
                .put("subcategory", row.getString("subcategory"))
                .put("unit", row.getString("unit"))
                .put("buyingPrice", row.getDouble("buying_price"))
                .put("sellingPrice", row.getDouble("selling_price"))
                .put("stock", row.getDouble("stock"))
                .put("sellable", row.getBoolean("sellable"))
                .put("createdAt", row.getLocalDateTime("created_at").toString());
        });
}

}
