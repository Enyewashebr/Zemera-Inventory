package com.zemera.inventory.handler;

import com.zemera.inventory.service.OrderService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class OrderHandler {

    private final OrderService orderService;

    public OrderHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    public void createOrder(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        orderService.createOrder(body)
            .onSuccess(ticket -> {
                ctx.response()
                    .setStatusCode(201)
                    .putHeader("Content-Type", "application/json")
                    .end(ticket.encode());
            })
            .onFailure(err -> {
                String message = err.getMessage();
                int status = 500;
                if ("INVALID_PAYLOAD".equals(message) || "INVALID_ITEM".equals(message)) {
                    status = 400;
                } else if ("PRODUCT_NOT_FOUND".equals(message)) {
                    status = 422;
                } else if ("INSUFFICIENT_STOCK".equals(message)) {
                    status = 409;
                }

                JsonObject error = new JsonObject()
                    .put("error", message)
                    .put("message", humanMessage(message));

                ctx.response()
                    .setStatusCode(status)
                    .putHeader("Content-Type", "application/json")
                    .end(error.encode());
            });
    }

    private String humanMessage(String code) {
        return switch (code) {
            case "INVALID_PAYLOAD" -> "Invalid order payload.";
            case "INVALID_ITEM" -> "One or more order items are invalid.";
            case "PRODUCT_NOT_FOUND" -> "One of the products was not found in inventory.";
            case "INSUFFICIENT_STOCK" -> "Insufficient stock for one of the items.";
            default -> "Unexpected error while creating order.";
        };
    }
}




