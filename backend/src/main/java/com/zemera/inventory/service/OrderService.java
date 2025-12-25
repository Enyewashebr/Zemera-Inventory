package com.zemera.inventory.service;

import com.zemera.inventory.repository.OrderRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class OrderService {

    private final com.zemera.inventory.repository.ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderService(com.zemera.inventory.repository.ProductRepository productRepository,
                        OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public Future<JsonObject> createOrder(JsonObject payload) {
        // All heavy lifting (transactions, stock checks) is in OrderRepository.
        return orderRepository.createOrder(payload);
    }
}



