package com.zemera.inventory.handler;

import com.zemera.inventory.model.Purchase;
import com.zemera.inventory.service.PurchaseService;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Tuple;

public class PurchaseHandler {

    private final PurchaseService purchaseService;

    public PurchaseHandler(PurchaseService service) {
        this.purchaseService = service;
    }

//   POST /api/purchases

  public void createPurchase(RoutingContext ctx) {

    JsonObject body = ctx.getBodyAsJson();

    JsonObject jwt = ctx.user().principal();
    Integer branchId = jwt.getInteger("branchId");
    Long userId = jwt.getLong("userId");

    Purchase p = body.mapTo(Purchase.class);
    p.setBranchId(branchId);
    p.setStatus("PENDING");
    p.setApprovedBy(null);

    purchaseService.createPurchase(p)
        .onSuccess(res -> ctx.json(res))
        .onFailure(err ->
            ctx.response().setStatusCode(400).end(err.getMessage())
        );
}




    public void getAllPurchases(RoutingContext ctx) {
        purchaseService.getAllPurchases().onComplete(ar -> {
            if (ar.succeeded()) {
                ctx.response()
                   .putHeader("Content-Type", "application/json")
                   .end(Json.encodePrettily(ar.result()));
            } else {
                ctx.response().setStatusCode(500).end(ar.cause().getMessage());
            }
        });
    }

    public void getPurchaseById(RoutingContext ctx) {
        Long id = Long.valueOf(ctx.pathParam("id"));
        purchaseService.getPurchaseById(id).onComplete(ar -> {
            if (ar.succeeded() && ar.result() != null) {
                ctx.response()
                   .putHeader("Content-Type", "application/json")
                   .end(Json.encodePrettily(ar.result()));
            } else {
                ctx.response().setStatusCode(404).end("Purchase not found");
            }
        });
    }

    // get my purchases
   public void getMyPurchases(RoutingContext ctx) {

    JsonObject user = ctx.user().principal();

    Integer branchId = user.getInteger("branchId");

    purchaseService.getPurchasesByBranch(branchId)
        .onSuccess(list -> {
            ctx.response()
               .putHeader("Content-Type", "application/json")
               .end(Json.encodePrettily(list));
        })
        .onFailure(err -> {
            ctx.response()
               .setStatusCode(500)
               .end(err.getMessage());
        });
}

// approval flow can be added later
public void approve(RoutingContext ctx) {

    Long purchaseId = Long.valueOf(ctx.pathParam("id"));

    JsonObject jwt = ctx.user().principal();
    Long approvedBy = jwt.getLong("userId");

    purchaseService.approvePurchase(purchaseId, approvedBy)
        .onSuccess(v -> ctx.response().setStatusCode(204).end())
        .onFailure(err ->
            ctx.response().setStatusCode(500).end(err.getMessage())
        );
}

public void decline(RoutingContext ctx) {

    Long purchaseId = Long.valueOf(ctx.pathParam("id"));

    JsonObject jwt = ctx.user().principal();
    Long approvedBy = jwt.getLong("userId");

    JsonObject body = ctx.getBodyAsJson();
    String comment = body != null ? body.getString("comment") : null;

    purchaseService.declinePurchase(purchaseId, approvedBy, comment)
        .onSuccess(v -> ctx.response().setStatusCode(204).end())
        .onFailure(err ->
            ctx.response().setStatusCode(500).end(err.getMessage())
        );
}


    public void updatePurchase(RoutingContext ctx) {
        Long id = Long.valueOf(ctx.pathParam("id"));
        Purchase p = Json.decodeValue(ctx.getBodyAsString(), Purchase.class);
        purchaseService.updatePurchase(id, p).onComplete(ar -> {
            if (ar.succeeded()) {
                ctx.response()
                   .putHeader("Content-Type", "application/json")
                   .end(Json.encodePrettily(ar.result()));
            } else {
                ctx.response().setStatusCode(500).end(ar.cause().getMessage());
            }
        });
    }

    public void getPurchasesByBranch(RoutingContext ctx) {
    Integer branchId = Integer.valueOf(ctx.pathParam("branchId"));

    purchaseService.getPurchasesByBranch(branchId)
        .onComplete(ar -> {
            if (ar.succeeded()) {
                ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encodePrettily(ar.result()));
            } else {
                ctx.response()
                    .setStatusCode(500)
                    .end(ar.cause().getMessage());
            }
        });
}


    public void deletePurchase(RoutingContext ctx) {
        Long id = Long.valueOf(ctx.pathParam("id"));
        purchaseService.deletePurchase(id).onComplete(ar -> {
            if (ar.succeeded()) {
                ctx.response().setStatusCode(204).end();
            } else {
                ctx.response().setStatusCode(500).end(ar.cause().getMessage());
            }
        });
    }
}
