package com.zemera.inventory.handler;

import com.zemera.inventory.model.Purchase;
import com.zemera.inventory.service.PurchaseService;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class PurchaseHandler {

    private final PurchaseService purchaseService;

    public PurchaseHandler(PurchaseService service) {
        this.purchaseService = service;
    }

//   POST /api/purchases

   public void createPurchase(RoutingContext ctx) {
    try {
        Purchase purchase = ctx.getBodyAsJson().mapTo(Purchase.class);

        // 🔐 Extract branchId from JWT
        Long branchId = ctx.user().principal().getLong("branch_id");

        System.out.println("Purchase payload: " + ctx.getBodyAsString());
        System.out.println("Branch ID from JWT: " + branchId);

        purchase.setBranchId(branchId);

        purchaseService.createPurchase(purchase)
            .onSuccess(saved -> ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(Json.encode(saved)))
            .onFailure(err -> {
                err.printStackTrace();   // 🔥 print real error
                ctx.fail(err);
            });

    } catch (Exception e) {
        e.printStackTrace();  // 🔥 print parsing errors
        ctx.response().setStatusCode(400).end("Invalid purchase payload");
    }
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

    Long branchId = user.getLong("branchId");

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
    Long branchId = Long.valueOf(ctx.pathParam("branchId"));

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
