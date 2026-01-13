package com.zemera.inventory.handler;

import com.zemera.inventory.model.Order;
import com.zemera.inventory.model.OrderItem;
import com.zemera.inventory.service.OrderService;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class OrderHandler {

    private final OrderService orderService;

    public OrderHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    public void createOrder(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        Integer branchId = ctx.user().principal().getInteger("branchId");
        Long waiterId = ctx.user().principal().getLong("userId");

        try {
            Order order = new Order();
            order.setBranchId(branchId);
            // order.setWaiterId(waiterId);

            List<OrderItem> items = new ArrayList<>();
           for (Object obj : body.getJsonArray("items")) {
    JsonObject itemJson = (JsonObject) obj;

    OrderItem item = new OrderItem();
    item.setProductId(itemJson.getLong("productId"));
    item.setQuantity(itemJson.getDouble("quantity"));
    item.setUnitPrice(itemJson.getDouble("unitPrice"));
    item.calculateTotalPrice();

    items.add(item);
}
            order.setItems(items);

            double totalAmount = items.stream().mapToDouble(OrderItem::getTotalPrice).sum();
            order.setTotalAmount(totalAmount);

            orderService.createOrder(order)
                        .onSuccess(created -> ctx.json(created))
                        .onFailure(err -> ctx.response().setStatusCode(400).end(err.getMessage()));

        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Invalid request data: " + e.getMessage());
        }
    }

    public void getOrderById(RoutingContext ctx) {
        Long id = Long.valueOf(ctx.pathParam("id"));
        orderService.getOrderById(id)
                    .onSuccess(order -> {
                        if (order == null) ctx.response().setStatusCode(404).end("Order not found");
                        else ctx.json(order);
                    })
                    .onFailure(err -> ctx.response().setStatusCode(500).end(err.getMessage()));
    }
}
