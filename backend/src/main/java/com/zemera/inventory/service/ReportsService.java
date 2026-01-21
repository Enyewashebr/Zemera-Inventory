package com.zemera.inventory.service;

import java.time.YearMonth;

import com.zemera.inventory.repository.ReportsRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

public class ReportsService {

    private final ReportsRepository repository;

    public ReportsService(ReportsRepository repository) {
        this.repository = repository;
    }

    // ================= SALES =================
public Future<JsonObject> getSalesReport(String time, String value, Integer branchId) {

    return repository.getSalesRows(time, value, branchId)
        .map(rows -> {

            double totalSales = rows.stream()
                .mapToDouble(r ->
                    ((JsonObject) r)
                        .getDouble("totalPrice", 0.0)
                )
                .sum();

            return new JsonObject()
                .put("rows", rows)
                .put("totalSales", totalSales);
        });
}


    // ================= PURCHASES =================
  public Future<JsonObject> getPurchaseReport(String time, String value, Integer branchId) {

    return repository.getPurchaseRows(time, value, branchId)
        .map(rows -> {

            double totalPurchases = rows.stream()
                .map(obj -> (JsonObject) obj)
                .mapToDouble(r -> ((Number) r.getValue("totalCost")).doubleValue())
                .sum();

            return new JsonObject()
                .put("rows", rows)
                .put("totalPurchases", totalPurchases);
        });
}



    // // ================= PROFIT =================

public Future<JsonObject> getProfitReport(String time, String value, Integer branchId) {

    return repository.getSalesRows(time, value, branchId)
        .compose(salesRows -> {

            double totalSales = salesRows.stream()
                .mapToDouble(r -> {
                    JsonObject row = (JsonObject) r;
                    Number v = row.getNumber("totalPrice");
                    return v != null ? v.doubleValue() : 0.0;
                })
                .sum();

            return repository.getPurchaseRows(time, value, branchId)
                .map(purchaseRows -> {

                    double totalPurchases = purchaseRows.stream()
                        .mapToDouble(r -> {
                            JsonObject row = (JsonObject) r;
                            Number v = row.getNumber("totalCost");
                            return v != null ? v.doubleValue() : 0.0;
                        })
                        .sum();

                    return new JsonObject()
                        .put("totalSales", totalSales)
                        .put("totalPurchases", totalPurchases)
                        .put("profit", totalSales - totalPurchases);
                });
        });
}




//     public Future<JsonObject> getProfitReport(String time, String value, Integer branchId) {

//     return repository.getSalesRows(time, value, branchId)
//         .compose(salesRows -> {

//             double totalSales = salesRows.stream()
//                 .mapToDouble(r -> {
//                     JsonObject row = (JsonObject) r;
//                     Number v = row.getNumber("totalPrice");
//                     return v != null ? v.doubleValue() : 0.0;
//                 })
//                 .sum();

//             return repository.getPurchaseRows(time, value, branchId)
//                 .map(purchaseRows -> {

//                     double totalPurchases = purchaseRows.stream()
//                         .mapToDouble(r -> {
//                             JsonObject row = (JsonObject) r;
//                             Number v = row.getNumber("totalCost");
//                             return v != null ? v.doubleValue() : 0.0;
//                         })
//                         .sum();

//                     return new JsonObject()
//                         .put("totalSales", totalSales)
//                         .put("totalPurchases", totalPurchases)
//                         .put("profit", totalSales - totalPurchases);
//                 });
//         });
// }


}
