package com.zemera.inventory.repository;

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

    // ================= SALES (ITEM LEVEL) =================
    public Future<JsonArray> getSalesRows(String time, String value, Integer branchId) {

        String sql = buildSalesQuery(time, branchId);
        Tuple params = buildParams(value, branchId);

        System.out.println("SQL: " + sql);
        System.out.println("Params: " + params);

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

    // ================= TOTAL SALES =================
    public Future<Double> getTotalSales(String time, String value, Integer branchId) {
        String sql = buildTotalSalesQuery(time, branchId);
        Tuple params = buildParams(value, branchId);

        System.out.println("SQL: " + sql);
        System.out.println("Params: " + params);

        return client
            .preparedQuery(sql)
            .execute(params)
            .map(rows -> {
                if (!rows.iterator().hasNext()) return 0.0;
                return rows.iterator().next().getDouble(0);
            });
    }

    // ================= TOTAL PURCHASES =================
    public Future<Double> getTotalPurchases(String time, String value, Integer branchId) {
        String sql = buildTotalPurchasesQuery(time, branchId);
        Tuple params = buildParams(value, branchId);

        System.out.println("SQL: " + sql);
        System.out.println("Params: " + params);

        return client
            .preparedQuery(sql)
            .execute(params)
            .map(rows -> {
                if (!rows.iterator().hasNext()) return 0.0;
                return rows.iterator().next().getDouble(0);
            });
    }

    // ================= QUERY BUILDERS =================
    private String buildSalesQuery(String time, Integer branchId) {
        String dateFilter = getDateFilter("o.created_at", time);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
           .append("oi.product_name AS item, ")
           .append("oi.quantity AS qty, ")
           .append("oi.unit AS unit, ")
           .append("oi.unit_price AS \"unitPrice\", ")
           .append("(oi.quantity * oi.unit_price) AS \"totalPrice\", ")
           .append("o.waiter_name AS waiter, ")
           .append("o.created_at AS timestamp ")
           .append("FROM orders o ")
           .append("JOIN order_items oi ON oi.order_id = o.id ")
           .append("WHERE ")
           .append(dateFilter);

        if (branchId != null) {
            sql.append(" AND o.branch_id = ?");
        }

        sql.append(" ORDER BY o.created_at DESC");

        return sql.toString();
    }

    private String buildTotalSalesQuery(String time, Integer branchId) {
        String dateFilter = getDateFilter("created_at", time);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE ")
           .append(dateFilter);

        if (branchId != null) {
            sql.append(" AND branch_id = ?");
        }

        return sql.toString();
    }

    private String buildTotalPurchasesQuery(String time, Integer branchId) {
        String dateFilter = getDateFilter("created_at", time);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COALESCE(SUM(total_amount), 0) FROM purchases WHERE ")
           .append(dateFilter);

        if (branchId != null) {
            sql.append(" AND branch_id = ?");
        }

        return sql.toString();
    }

    // ================= DATE FILTER HELPER =================
    private String getDateFilter(String column, String time) {
        return switch (time) {
            case "daily" -> "DATE(" + column + ") = ?";
            case "monthly" -> "DATE_TRUNC('month', " + column + ") = DATE_TRUNC('month', ?::date)";
            case "yearly" -> "DATE_TRUNC('year', " + column + ") = DATE_TRUNC('year', ?::date)";
            default -> throw new IllegalArgumentException("Invalid time: " + time);
        };
    }

    // ================= PARAM BUILDERS =================
    private Tuple buildParams(String value, Integer branchId) {
        Tuple tuple = Tuple.tuple();
        tuple.addString(value); // date/month/year
        if (branchId != null) tuple.addInteger(branchId);
        return tuple;
    }
}
