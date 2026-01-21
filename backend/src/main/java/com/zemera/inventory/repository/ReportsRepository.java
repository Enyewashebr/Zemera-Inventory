package com.zemera.inventory.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

public class ReportsRepository {

    private final SqlClient client;

    public ReportsRepository(SqlClient client) {
        this.client = client;
    }

    // ================= SALES =================
public Future<JsonArray> getSalesRows(String time, String value, Integer branchId) {

    String sql = """
        SELECT
            oi.product_name AS item,
            SUM(oi.quantity) AS qty,
            oi.unit AS unit,
            oi.unit_price AS "unitPrice",
            SUM(oi.line_total) AS "totalPrice",
            o.waiter_name AS waiter,
            MAX(o.created_at):: date AS timestamp
        FROM orders o
        JOIN order_items oi ON oi.order_id = o.id
        WHERE o.branch_id = $1
    """;

    Tuple params = Tuple.tuple();
    params.addInteger(branchId);
    

    switch (time) {
        case "daily" -> {
            sql += " AND o.created_at::date = $2";
            params.addLocalDate(LocalDate.parse(value));
        }
         case "monthly" -> {
            sql += """
                AND DATE_TRUNC('month', o.created_at)
                    = DATE_TRUNC('month', $2::date)
            """;
            params.addLocalDate(LocalDate.parse(value));
        }

        case "yearly" -> {
            sql += " AND EXTRACT(YEAR FROM o.created_at) = $2";
            params.addInteger(Integer.parseInt(value.substring(0, 4)));
        }
    }

    sql += """
        GROUP BY oi.product_name, oi.unit, oi.unit_price, o.waiter_name
        ORDER BY timestamp DESC
    """;

    return client
        .preparedQuery(sql)
        .execute(params)
        .map(rows -> {
            JsonArray array = new JsonArray();
            for (Row row : rows) {
                array.add(row.toJson());
            }
            return array;
        });
}


    // ================= PURCHASES =================

//     public Future<JsonArray> getPurchaseRows(String time, String value, Integer branchId) {

//     StringBuilder sql = new StringBuilder("""
//     SELECT
//         pr.name AS item,
//         SUM(p.quantity) AS quantity,
//         AVG(p.unit_price) AS "unitPrice",
//         SUM(p.total_cost) AS "totalCost"
//     FROM purchase p
//     JOIN products pr ON pr.id = p.product_id
//     WHERE 1=1
// """);

// List<Object> params = new ArrayList<>();

// sql.append(" AND p.status = 'APPROVED'");
// sql.append(" AND p.branch_id = ?");
// params.add(branchId);

// switch (time) {
//     case "daily" -> {
//         sql.append(" AND p.purchase_date = ?");
//         params.add(LocalDate.parse(value));
//     }
//     case "monthly" -> {
//         sql.append("""
//             AND DATE_TRUNC('month', p.purchase_date)
//                 = DATE_TRUNC('month', ?::date)
//         """);
//         params.add(LocalDate.parse(value));
//     }
//     case "yearly" -> {
//         sql.append(" AND EXTRACT(YEAR FROM p.purchase_date) = ?");
//         params.add(Integer.parseInt(value.substring(0, 4)));
//     }
// }

// sql.append("""
//     GROUP BY pr.name
//     ORDER BY pr.name
// """);
// System.out.println("SQL => " + sql);
// System.out.println("PARAMS => " + params);

//     return client
//         .preparedQuery(sql.toString())
//         .execute(Tuple.from(params))
//         .map(rows -> {
//             JsonArray arr = new JsonArray();
//             for (Row r : rows) {
//                 arr.add(r.toJson());
//             }
//             return arr;
//         });
// }
public Future<JsonArray> getPurchaseRows(String time, String value, Integer branchId) {

    String sql = """
        SELECT
            pr.name AS item,
            SUM(p.quantity) AS quantity,
            AVG(p.unit_price) AS "unitPrice",
            SUM(p.total_cost) AS "totalCost"
        FROM purchase p
        JOIN products pr ON pr.id = p.product_id
        WHERE p.branch_id = $1
          AND p.status = 'APPROVED'
    """;

    Tuple params = Tuple.tuple();
    params.addInteger(branchId);

    switch (time) {
        case "daily" -> {
            sql += " AND p.purchase_date::date = $2";
            params.addLocalDate(LocalDate.parse(value));
        }
        case "monthly" -> {
            sql += """
                AND DATE_TRUNC('month', p.purchase_date)
                    = DATE_TRUNC('month', $2::date)
            """;
            params.addLocalDate(LocalDate.parse(value));
        }
        case "yearly" -> {
            sql += " AND EXTRACT(YEAR FROM p.purchase_date) = $2";
            params.addInteger(Integer.parseInt(value.substring(0, 4)));
        }
    }

    sql += """
        GROUP BY pr.name
        ORDER BY pr.name
    """;

    return client
        .preparedQuery(sql)
        .execute(params)
        .map(rows -> {
            JsonArray array = new JsonArray();
            for (Row row : rows) {
                array.add(row.toJson());
            }
            return array;
        });
}


}
