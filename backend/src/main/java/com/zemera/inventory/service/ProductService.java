package com.zemera.inventory.service;

import com.zemera.inventory.repository.ProductRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Create product
    public void createProduct(
        JsonObject productData,
        Handler<AsyncResult<JsonObject>> resultHandler
    ) {
        productRepository.createProduct(productData, resultHandler);
    }

    // Get all products
    public void getAllProducts(
        Handler<AsyncResult<List<JsonObject>>> resultHandler
    ) {
        productRepository.getAllProducts(resultHandler);
    }

    // Update product
    public Future<JsonObject> updateProduct(int id, JsonObject data) {
    return productRepository.updateProduct(id, data);
}

}
