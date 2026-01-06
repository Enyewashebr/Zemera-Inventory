package com.zemera.inventory.service;

import com.zemera.inventory.model.Product;
import com.zemera.inventory.repository.ProductRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Create product
    public Future<Product> createProduct(JsonObject productData) {
        Product product = productData.mapTo(Product.class);
        return productRepository.createProduct(product);
    }

    // Get all products
    public Future<List<Product>> getAllProducts() {
        return productRepository.getAllProducts();
    }

    // Update product
    public Future<Product> updateProduct(int id, JsonObject data) {
        // For now, keep the old method signature but we can update this later
        return productRepository.updateProduct(id, data)
            .map(json -> json.mapTo(Product.class));
    }

    // Stock management methods
    public Future<Void> decreaseStock(int productId, int quantity) {
        return productRepository.decreaseStock(productId, quantity);
    }

    public Future<Boolean> hasSufficientStock(int productId, int quantity) {
        return productRepository.hasSufficientStock(productId, quantity);
    }

    public Future<Product> getProductById(int id) {
        return productRepository.getProductById(id);
    }

}
