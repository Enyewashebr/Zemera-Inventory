package com.zemera.inventory.handler;

import com.zemera.inventory.service.StockService;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class StockHandler {

    private final StockService stockService;

    public StockHandler(StockService stockService) {
        this.stockService = stockService;
    }

    // GET /api/stock/:productId
    public void getStock(RoutingContext ctx) {
        int productId = Integer.parseInt(ctx.pathParam("productId"));
        int branchId = ctx.user().principal().getInteger("branchId");

        stockService.getStock(branchId, productId)
            .onSuccess(qty -> ctx.json(new JsonObject().put("quantity", qty)))
            .onFailure(err ->
                ctx.response().setStatusCode(400).end(err.getMessage())
            );
    }

    // POST /api/stock/increase
    public void increaseStock(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        int branchId = ctx.user().principal().getInteger("branchId");

        stockService.increaseStock(
            branchId,
            body.getInteger("productId"),
            body.getDouble("quantity")
        )
        .onSuccess(v -> ctx.response().setStatusCode(200).end())
        .onFailure(err ->
            ctx.response().setStatusCode(400).end(err.getMessage())
        );
    }





 public void getMyStock(RoutingContext ctx) {

    Integer branchId = ctx.user().principal().getInteger("branchId");

    stockService.getStockViewByBranch(branchId)
        .onSuccess(list -> ctx.json(list))
        .onFailure(err ->
            ctx.response().setStatusCode(500).end(err.getMessage())
        );
}


public void getStockByBranch(RoutingContext ctx) {
    Integer branchId = Integer.valueOf(ctx.pathParam("branchId"));

    stockService.getStockViewByBranch(branchId)
        .onSuccess(list -> ctx.json(list))
        .onFailure(err ->
            ctx.response().setStatusCode(500).end(err.getMessage())
        );
}

}
