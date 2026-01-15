package com.zemera.inventory.service;

import com.zemera.inventory.repository.ReportsRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class ReportsService {

    private final ReportsRepository repository;

    public ReportsService(ReportsRepository repository) {
        this.repository = repository;
    }

    public Future<JsonObject> getReport(
            String time,
            String type,
            String value,
            Integer branchId
    ) {
        return switch (type) {
            case "sales" -> repository
                    .getSalesRows(time, value, branchId)
                    .map(rows -> new JsonObject().put("rows", rows));

            case "profit" -> repository
                    .getTotalSales(time, value, branchId)
                    .compose(sales ->
                            repository.getTotalPurchases(time, value, branchId)
                                    .map(purchases ->
                                            new JsonObject()
                                                    .put("sales", sales)
                                                    .put("purchases", purchases)
                                                    .put("profit", sales - purchases)
                                    )
                    );

            default -> Future.failedFuture("Invalid report type: " + type);
        };
    }
}
