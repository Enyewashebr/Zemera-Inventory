package com.zemera.inventory.service;

import com.zemera.inventory.model.Order;
import com.zemera.inventory.model.OrderItem;
import com.zemera.inventory.repository.OrderRepository;
import com.zemera.inventory.repository.ProductRepository;
import com.zemera.inventory.repository.StockRepository;
import io.vertx.core.Future;

public class OrderService {

    private final OrderRepository orderRepo;
    private final StockRepository stockRepo;
    private final ProductRepository productRepo;

    public OrderService(OrderRepository orderRepo, StockRepository stockRepo, ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.stockRepo = stockRepo;
        this.productRepo = productRepo;
    }

    public Future<Order> createOrder(Order order) {

        // Check stock for each item if sellable
        Future<Void> stockCheck = Future.succeededFuture();

        for (OrderItem item : order.getItems()) {
            stockCheck = stockCheck.compose(v ->
                productRepo.isSellable(item.getProductId())
                    .compose(sellable -> {
                        if (!sellable) {
                            // Kitchen item, skip stock
                            return Future.succeededFuture();
                        }

                        // Check stock
                        return stockRepo.getStock(order.getBranchId(), item.getProductId())
                                .compose(stockQty -> {
                                    if (stockQty < item.getQuantity()) {
                                        return Future.failedFuture("Insufficient stock for product ID " + item.getProductId());
                                    }
                                    return Future.succeededFuture();
                                });
                    })
            );
        }

        return stockCheck.compose(v -> {
            // Decrease stock for sellable items
            Future<Void> stockUpdate = Future.succeededFuture();
            for (OrderItem item : order.getItems()) {
                stockUpdate = stockUpdate.compose(v2 ->
                    productRepo.isSellable(item.getProductId())
                        .compose(sellable -> {
                            if (!sellable) return Future.succeededFuture();
                            return stockRepo.decreaseStock(order.getBranchId(), item.getProductId(), item.getQuantity());
                        })
                );
            }

            return stockUpdate.compose(v3 -> orderRepo.create(order));
        });
    }

    public Future<Order> getOrderById(Long orderId) {
        return orderRepo.getById(orderId);
    }
}
