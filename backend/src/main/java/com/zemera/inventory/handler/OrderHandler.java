package com.zemera.inventory.handler;

import com.zemera.inventory.model.OrderItem;
import com.zemera.inventory.service.OrderService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

public class OrderHandler {

    private final OrderService orderService;

    public OrderHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    /* ==========================
       CREATE ORDER
    ========================== */

   public void createOrder(RoutingContext ctx) {

    JsonObject body = ctx.body().asJsonObject();

    String waiterName = body.getString("waiterName");
    JsonArray itemsJson = body.getJsonArray("items");

    int branchId = ctx.user().principal().getInteger("branchId");

    List<OrderItem> items = new ArrayList<>();

    for (Object o : itemsJson) {
        JsonObject j = (JsonObject) o;

        OrderItem item = new OrderItem();
        item.setProductId(j.getLong("productId"));
        item.setProductName(j.getString("productName"));
        item.setQuantity(j.getDouble("quantity"));
        item.setUnit(j.getString("unit"));
        item.setUnitPrice(j.getDouble("unitPrice"));

        items.add(item);
    }

    orderService.createOrder(branchId, waiterName, items)
        .onSuccess(orderId -> {
            ctx.json(new JsonObject()
                .put("orderId", orderId)
                .put("waiterName", waiterName)
                .put("items", items)
            );
        })
        .onFailure(err -> {
           ctx.json(new JsonObject()
    .put("message", "Failed to create order")
    .put("error", err.getMessage())
);
        });
}

    /* ==========================
       REPRINT ORDER TICKET
    ========================== */

    public void getOrderTicket(RoutingContext ctx) {

        long orderId = Long.parseLong(ctx.pathParam("id"));

        orderService.getOrderTicket(orderId)
            .onSuccess(rs -> {

                JsonArray items = new JsonArray();
                JsonObject ticket = new JsonObject();

                for (var row : rs) {

                    ticket
                        .put("orderId", row.getLong("order_id"))
                        .put("waiterName", row.getString("waiter_name"))
                        .put("createdAt", row.getLocalDateTime("created_at"))
                        .put("totalAmount", row.getDouble("total_amount"));

                    items.add(new JsonObject()
                        .put("productName", row.getString("product_name"))
                        .put("quantity", row.getDouble("quantity"))
                        .put("unit", row.getString("unit"))
                        .put("unitPrice", row.getDouble("unit_price"))
                        .put("lineTotal", row.getDouble("line_total"))
                    );
                }

                ticket.put("items", items);
                ctx.json(ticket);
            })
            .onFailure(err -> {
                ctx.json(new JsonObject()
    .put("message", "Failed to create order")
    .put("error", err.getMessage())
);
            });
    }
}
