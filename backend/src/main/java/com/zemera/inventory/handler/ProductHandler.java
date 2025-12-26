package com.zemera.inventory.handler;

import com.zemera.inventory.service.ProductService;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ProductHandler {

    private final ProductService productService;

    public ProductHandler(ProductService productService) {
        this.productService = productService;
    }

    // POST /api/products
    public void createProduct(RoutingContext ctx) {

        productService.createProduct(ctx.getBodyAsJson(), ar -> {
            if (ar.succeeded()) {
                ctx.response()
                    .setStatusCode(201)
                    .putHeader("Content-Type", "application/json")
                    .end(ar.result().encode());
            } else {
                ctx.response()
                    .setStatusCode(500)
                    .end(ar.cause().getMessage());
            }
        });
    }

    // update product
    public void updateProduct(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    JsonObject body = ctx.getBodyAsJson();

    productService.updateProduct(id, body)
        .onSuccess(product -> ctx.json(product))
        .onFailure(err -> {
            ctx.response()
                .setStatusCode(500)
                .end(err.getMessage());
        });
}


    // GET /api/products
    public void getAllProducts(RoutingContext ctx) {

        productService.getAllProducts(ar -> {
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
}
