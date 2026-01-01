package com.zemera.inventory.handler;

import com.zemera.inventory.model.Purchase;
import com.zemera.inventory.service.PurchaseService;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class PurchaseHandler {

    private final PurchaseService purchaseService;

    public PurchaseHandler(PurchaseService service) {
        this.purchaseService = service;
    }

    public void createPurchase(RoutingContext ctx) {
        Purchase p = Json.decodeValue(ctx.getBodyAsString(), Purchase.class);
        purchaseService.createPurchase(p).onComplete(ar -> {
            if (ar.succeeded()) {
                ctx.response()
                   .setStatusCode(201)
                   .putHeader("Content-Type", "application/json")
                   .end(Json.encodePrettily(ar.result()));
            } else {
                ctx.response().setStatusCode(500).end(ar.cause().getMessage());
            }
        });
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
