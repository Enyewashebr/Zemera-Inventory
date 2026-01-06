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
        JsonObject body = ctx.getBodyAsJson();

        productService.createProduct(body)
            .onSuccess(product -> ctx.response()
                .setStatusCode(201)
                .putHeader("Content-Type", "application/json")
                .end(Json.encodePrettily(product)))
            .onFailure(err -> ctx.response()
                .setStatusCode(500)
                .end(err.getMessage()));
    }

    // update product
    public void updateProduct(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        JsonObject body = ctx.getBodyAsJson();

        productService.updateProduct(id, body)
            .onSuccess(product -> ctx.json(product))
            .onFailure(err -> ctx.response()
                .setStatusCode(500)
                .end(err.getMessage()));
    }

    // GET /api/products
    public void getAllProducts(RoutingContext ctx) {
        productService.getAllProducts()
            .onSuccess(products -> ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(Json.encodePrettily(products)))
            .onFailure(err -> ctx.response()
                .setStatusCode(500)
                .end(err.getMessage()));
    }
}
