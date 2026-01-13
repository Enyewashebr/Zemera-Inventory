package com.zemera.inventory.repository;

import com.zemera.inventory.model.Purchase;
import com.zemera.inventory.model.Stock;
import com.zemera.inventory.model.StockView;
import io.vertx.core.Future;
import io.vertx.sqlclient.*;

import java.util.ArrayList;
import java.util.List;

public class StockRepository {

    private final Pool client;

    public StockRepository(Pool client) {
        this.client = client;
    }

    // Get stock quantity
    public Future<Double> getStock(int branchId, Long productId) {
        String sql = """
            SELECT quantity
            FROM stocks
            WHERE branch_id = $1 AND product_id = $2
        """;

        return client
            .preparedQuery(sql)
            .execute(Tuple.of(branchId, productId))
            .map(rs -> rs.iterator().hasNext()
                ? rs.iterator().next().getDouble("quantity")
                : 0.0
            );
    }

    // Increase stock (purchase / restock)
    public Future<Void> increaseStock(int branchId, int productId, double qty) {
        String sql = """
            INSERT INTO stocks (branch_id, product_id, quantity)
            VALUES ($1, $2, $3)
            ON CONFLICT (branch_id, product_id)
            DO UPDATE SET quantity = stocks.quantity + EXCLUDED.quantity,
                          last_updated = now()
        """;

        return client
            .preparedQuery(sql)
            .execute(Tuple.of(branchId, productId, qty))
            .mapEmpty();
    }

    // Decrease stock (order)
    public Future<Void> decreaseStock(int branchId, long productId, double qty) {
        String sql = """
            UPDATE stocks
            SET quantity = quantity - $1,
                last_updated = now()
            WHERE branch_id = $2
              AND product_id = $3
              AND quantity >= $1
        """;

        return client
            .preparedQuery(sql)
            .execute(Tuple.of(qty, branchId, productId))
            .compose(rs -> {
                if (rs.rowCount() == 0) {
                    return Future.failedFuture("Insufficient stock");
                }
                return Future.succeededFuture();
            });
    }
  public Future<List<StockView>> getStockViewByBranch(int branchId) {

    String sql = """
        SELECT
            s.id AS stock_id,
            s.product_id,
            s.quantity,
            s.last_updated,

            p.name AS product_name,
            p.unit,
            p.category_id AS category,
            p.subcategory AS subcategory

        FROM stocks s
        JOIN products p ON p.id = s.product_id
        WHERE s.branch_id = $1
        ORDER BY p.name
    """;

    return client
        .preparedQuery(sql)
        .execute(Tuple.of(branchId))
        .map(rs -> {
            List<StockView> list = new ArrayList<>();

            for (Row row : rs) {
                StockView v = new StockView();
                v.setStockId(row.getLong("stock_id"));
                v.setProductId(row.getLong("product_id"));
                v.setProductName(row.getString("product_name"));
                v.setCategory(row.getString("category"));
                v.setSubcategory(row.getString("subcategory"));
                v.setUnit(row.getString("unit"));
                v.setQuantity(row.getDouble("quantity"));
                v.setLastUpdated(row.getLocalDateTime("last_updated"));
                list.add(v);
            }
            return list;
        });
}


    // Get all stock for a branch
    public Future<List<Stock>> getBranchStock(int branchId) {
        String sql = """
            SELECT id, product_id, product_name, category, subcategory, branch_id, quantity, last_updated
            FROM stocks
            WHERE branch_id = $1
        """;

        return client
            .preparedQuery(sql)
            .execute(Tuple.of(branchId))
            .map(rs -> {
                List<Stock> list = new ArrayList<>();
                for (Row row : rs) {
                    Stock s = new Stock();
                    s.setId(row.getLong("id"));
                    s.setProductId(row.getInteger("product_id"));
                   s.setProductName(row.getString("product_name"));
                s.setCategory(row.getString("category"));
                s.setSubcategory(row.getString("subcategory"));
                    s.setBranchId(row.getInteger("branch_id"));
                    s.setQuantity(row.getDouble("quantity"));
                    s.setLastUpdated(row.getLocalDateTime("last_updated"));
                    list.add(s);
                }
                return list;
            });
    }

    //  public Future<List<Stock>> getStockByBranch(int branchId) {
    //     String sql = "SELECT product_id, branch_id, quantity FROM stocks WHERE branch_id = $1";
    //     return client.preparedQuery(sql)
    //                  .execute(io.vertx.sqlclient.Tuple.of(branchId))
    //                  .map(rows -> {
    //                      List<Stock> list = new ArrayList<>();
    //                      for (Row row : rows) {
    //                          Stock stock = new Stock();
    //                          stock.setProductId(row.getInteger("product_id"));
    //                          stock.setBranchId(row.getInteger("branch_id"));
    //                          stock.setQuantity(row.getDouble("quantity"));
    //                          list.add(stock);
    //                      }
    //                      return list;
    //                  });
    // }


}
