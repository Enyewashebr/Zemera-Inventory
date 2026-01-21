package com.zemera.inventory.handler;

import com.zemera.inventory.service.ReportsService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ReportsHandler {

    private final ReportsService service;

    public ReportsHandler(ReportsService service) {
        this.service = service;
    }

   public void getSales(RoutingContext ctx) {

    String time = ctx.request().getParam("time");     // daily | monthly | yearly
    String value = ctx.request().getParam("value");  // date string
    Integer branchId = getBranchId(ctx);              // from JWT

    service.getSalesReport(time, value, branchId)
        .onSuccess(result -> ctx.response()
            .putHeader("Content-Type", "application/json")
            .end(result.encode()))
        .onFailure(err -> {
            err.printStackTrace();
            ctx.response()
                .setStatusCode(500)
                .end(new JsonObject()
                    .put("error", err.getMessage())
                    .encode());
        });
}

public void getPurchaseReport(RoutingContext ctx) {

    String time = ctx.request().getParam("time");
    String value = ctx.request().getParam("value");
    // Integer branchId = ctx.user().principal().getInteger("branchId");
    Integer branchId = getBranchId(ctx);


    service.getPurchaseReport(time, value, branchId)
        .onSuccess(res -> ctx.json(res))
        .onFailure(err -> {
            err.printStackTrace();
            ctx.response()
                .setStatusCode(500)
                .end(err.getMessage());
        });
}


  public void getProfit(RoutingContext ctx) {

    String time = ctx.request().getParam("time");
    String value = ctx.request().getParam("value");

    if (time == null || value == null) {
        ctx.response()
            .setStatusCode(400)
            .end("Missing time or value parameter");
        return;
    }

    Integer branchId = ctx.user().principal().getInteger("branchId");

    service.getProfitReport(time, value, branchId)
        .onSuccess(ctx::json)
        .onFailure(err -> {
            err.printStackTrace();
            ctx.response()
                .setStatusCode(500)
                .end(err.getMessage());
        });
}


    private Integer getBranchId(RoutingContext ctx) {
        String branchParam = ctx.request().getParam("branchId");
        return branchParam != null ? Integer.valueOf(branchParam) : null;
    }

   

}
