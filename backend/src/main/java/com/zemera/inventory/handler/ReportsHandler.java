package com.zemera.inventory.handler;

import com.zemera.inventory.service.ReportsService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ReportsHandler {

    private final ReportsService service;

    public ReportsHandler(ReportsService service) {
        this.service = service;
    }

    public void getReports(RoutingContext ctx) {

        String time = ctx.request().getParam("time");
        String type = ctx.request().getParam("type");
        String value = ctx.request().getParam("value");
        String branchParam = ctx.request().getParam("branchId");

        Integer branchId = branchParam != null ? Integer.valueOf(branchParam) : null;

        if (time == null || type == null || value == null) {
            ctx.response()
                    .setStatusCode(400)
                    .end(new JsonObject().put("error", "Missing parameters").encode());
            return;
        }

        // Debug log
        System.out.println("Generating report -> time: " + time + ", type: " + type + ", value: " + value + ", branchId: " + branchId);

        service.getReport(time, type, value, branchId)
                .onSuccess(result ->
                        ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .end(result.encode())
                )
                .onFailure(err ->
                        ctx.response()
                                .setStatusCode(500)
                                .end(new JsonObject().put("error", err.getMessage()).encode())
                );
    }
}
