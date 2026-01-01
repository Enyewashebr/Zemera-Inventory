package com.zemera.inventory.repository;

import com.zemera.inventory.model.Purchase;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.ArrayList;
import java.util.List;

public class PurchaseRepository {

    private final Pool client;

    public PurchaseRepository(Pool client) {
        this.client = client;
    }


    // Create a new purchase
 public Future<Purchase> create(Purchase p) {

    String sql = """
        INSERT INTO purchase
        (product_id, quantity, unit_price, purchase_date, status, approved_by)
        VALUES ($1, $2, $3, $4, $5, $6)
        RETURNING *
        """;

    return client
        .preparedQuery(sql)
        .execute(Tuple.of(
            p.getProductId(),
            p.getQuantity(),
            p.getUnitPrice(),
            p.getPurchaseDate(),
            p.getStatus(),
            p.getApprovedBy()
        ))
        .compose(rs -> {
            Long purchaseId = rs.iterator().next().getLong("id");

            // 🔁 Fetch purchase with product name
            String fetchSql = """
                SELECT
                    p.id,
                    p.product_id,
                    pr.name AS product_name,
                    p.quantity,
                    p.unit_price,
                    p.total_cost,
                    p.purchase_date,
                    p.status,
                    p.approved_by,
                    p.created_at,
                    p.updated_at
                FROM purchase p
                JOIN products pr ON pr.id = p.product_id
                WHERE p.id = $1
            """;

            return client
                .preparedQuery(fetchSql)
                .execute(Tuple.of(purchaseId))
                .map(r -> mapRowToPurchase(r.iterator().next()));
        });
}

    // double totalCost = p.getQuantity() * p.getUnitPrice();


    // Get all purchases
   public Future<List<Purchase>> getAll() {

    String sql = """
        SELECT
            p.id,
            p.product_id,
            pr.name AS product_name,
            p.quantity,
            p.unit_price,
            p.total_cost,
            p.purchase_date,
            p.status,
            p.approved_by,
            p.created_at,
            p.updated_at
        FROM purchase p
        JOIN products pr ON pr.id = p.product_id
        ORDER BY p.created_at DESC
    """;

    return client
        .query(sql)
        .execute()
        .map(rs -> {
            List<Purchase> list = new ArrayList<>();
            for (Row row : rs) {
                list.add(mapRowToPurchase(row));
            }
            return list;
        });
}

    // Get purchase by ID
   public Future<Purchase> getById(Long id) {

    String sql = """
        SELECT
            p.id,
            p.product_id,
            pr.name AS product_name,
            p.quantity,
            p.unit_price,
            p.total_cost,
            p.purchase_date,
            p.status,
            p.approved_by,
            p.created_at,
            p.updated_at
        FROM purchase p
        JOIN products pr ON pr.id = p.product_id
        WHERE p.id = $1
    """;

    return client
        .preparedQuery(sql)
        .execute(Tuple.of(id))
        .map(rs -> rs.iterator().hasNext()
            ? mapRowToPurchase(rs.iterator().next())
            : null
        );
}

    // Update a purchase
    public Future<Purchase> update(Long id, Purchase p) {

    String sql = """
        UPDATE purchase
        SET product_id = $1,
            quantity = $2,
            unit_price = $3,
            purchase_date = $4,
            status = $5,
            approved_by = $6,
            updated_at = now()
        WHERE id = $7
        RETURNING *
        """;

    return client
        .preparedQuery(sql)
        .execute(Tuple.of(
            p.getProductId(),
            p.getQuantity(),
            p.getUnitPrice(),
            p.getPurchaseDate(),
            p.getStatus(),
            p.getApprovedBy(),
            id
        ))
        .map(rs -> mapRowToPurchase(rs.iterator().next()));
}


    // Delete a purchase
    public Future<Void> delete(Long id) {
        String sql = "DELETE FROM purchase WHERE id=$1";
        return client.preparedQuery(sql)
                .execute(Tuple.of(id))
                .mapEmpty();
    }

    // Helper to map Row to Purchase object
    // private Purchase mapRowToPurchase(Row row) {
    //     return new Purchase(
    //             row.getLong("id"),
    //             row.getLong("product_id"),
    //             row.getInteger("quantity"),
    //             row.getDouble("unit_price"),
    //             row.getDouble("total_cost"),
    //             row.getLocalDate("purchase_date"),
    //             row.getString("status"),
    //             row.getLong("approved_by"),
    //             row.getLocalDateTime("created_at"),
    //             row.getLocalDateTime("updated_at")
    //     );
    // }
    private Purchase mapRowToPurchase(Row row) {

    Purchase p = new Purchase();

    p.setId(row.getLong("id"));
    p.setProductId(row.getLong("product_id"));
    p.setProductName(row.getString("product_name")); // ✅ HERE
    p.setQuantity(row.getInteger("quantity"));
    p.setUnitPrice(row.getDouble("unit_price"));
    p.setTotalCost(row.getDouble("total_cost"));
    p.setPurchaseDate(row.getLocalDate("purchase_date"));
    p.setStatus(row.getString("status"));
    p.setApprovedBy(row.getLong("approved_by"));
    p.setCreatedAt(row.getLocalDateTime("created_at"));
    p.setUpdatedAt(row.getLocalDateTime("updated_at"));

    return p;
}

}
