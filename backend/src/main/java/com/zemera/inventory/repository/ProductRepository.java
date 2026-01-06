package com.zemera.inventory.repository;

import com.zemera.inventory.model.Product;
import io.vertx.core.Future;
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
    public Future<Product> createProduct(Product product) {
        String sql = "INSERT INTO products " +
                "(name, category_id, subcategory, unit, buying_price, selling_price, stock, sellable, created_at) " +
                "VALUES ($1, $2, $3, $4, $5, $6, $7, $8, NOW()) " +
                "RETURNING id, name, category_id, subcategory, unit, buying_price, selling_price, stock, sellable, created_at";

        return client.preparedQuery(sql)
            .execute(Tuple.of(
                product.getName(),
                product.getCategoryId(),
                product.getSubcategory(),
                product.getUnit(),
                product.getBuyingPrice(),
                product.getSellingPrice(),
                product.getStock(),
                product.getSellable()
            ))
            .map(rows -> {
                Row row = rows.iterator().next();
                return mapRowToProduct(row);
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

    // Get all products
    public Future<List<Product>> getAllProducts() {
        String sql = "SELECT id, name, category_id, subcategory, unit, buying_price, selling_price, stock, sellable, created_at " +
                     "FROM products ORDER BY created_at DESC";

        return client.query(sql)
            .execute()
            .map(rows -> {
                List<Product> products = new ArrayList<>();
                for (Row row : rows) {
                    products.add(mapRowToProduct(row));
                }
                return products;
            });
    }

    // Decrease stock when purchase is approved
    public Future<Void> decreaseStock(int productId, int quantity) {
        String sql = "UPDATE products SET stock = stock - $1 WHERE id = $2 AND stock >= $1";

        return client.preparedQuery(sql)
            .execute(Tuple.of(quantity, productId))
            .mapEmpty();
    }

    // Check if product has sufficient stock
    public Future<Boolean> hasSufficientStock(int productId, int quantity) {
        String sql = "SELECT stock >= $1 as sufficient FROM products WHERE id = $2";

        return client.preparedQuery(sql)
            .execute(Tuple.of(quantity, productId))
            .map(rows -> {
                if (rows.size() > 0) {
                    return rows.iterator().next().getBoolean("sufficient");
                }
                return false;
            });
    }

    // Get product by ID
    public Future<Product> getProductById(int id) {
        String sql = "SELECT id, name, category_id, subcategory, unit, buying_price, selling_price, stock, sellable, created_at " +
                     "FROM products WHERE id = $1";

        return client.preparedQuery(sql)
            .execute(Tuple.of(id))
            .map(rows -> {
                if (rows.size() > 0) {
                    return mapRowToProduct(rows.iterator().next());
                }
                return null;
            });
    }

    // Helper method to map Row to Product
    private Product mapRowToProduct(Row row) {
        Product product = new Product();
        product.setId(row.getInteger("id"));
        product.setName(row.getString("name"));
        product.setCategoryId(row.getString("category_id"));
        product.setSubcategory(row.getString("subcategory"));
        product.setUnit(row.getString("unit"));
        product.setBuyingPrice(row.getDouble("buying_price"));
        product.setSellingPrice(row.getDouble("selling_price"));
        product.setStock(row.getDouble("stock"));
        product.setSellable(row.getBoolean("sellable"));
        product.setCreatedAt(row.getLocalDateTime("created_at"));
        return product;
    }

}
