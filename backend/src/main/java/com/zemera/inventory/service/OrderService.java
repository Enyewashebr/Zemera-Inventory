package com.zemera.inventory.service;

import com.zemera.inventory.model.OrderItem;
import com.zemera.inventory.repository.OrderRepository;
import com.zemera.inventory.repository.StockRepository;
import io.vertx.core.*;
import io.vertx.sqlclient.*;

import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private final Pool client;
    private final OrderRepository orderRepo;
    private final StockRepository stockRepo;

    public OrderService(Pool client,
                        OrderRepository orderRepo,
                        StockRepository stockRepo) {

        this.client = client;
        this.orderRepo = orderRepo;
        this.stockRepo = stockRepo;
    }

    /* ==========================
       CREATE ORDER
    ========================== */

   public Future<Long> createOrder(int branchId,
                                String waiterName,
                                List<OrderItem> items) {

    double total = items.stream()
        .mapToDouble(i -> i.getQuantity() * i.getUnitPrice())
        .sum();

    Promise<Long> promise = Promise.promise();

    client.getConnection(connAr -> {
        if (connAr.failed()) {
            promise.fail(connAr.cause());
            return;
        }

        SqlConnection conn = connAr.result();

        conn.begin(txAr -> {
            if (txAr.failed()) {
                conn.close();
                promise.fail(txAr.cause());
                return;
            }

            Transaction tx = txAr.result();

            orderRepo.insertOrder(conn, branchId, waiterName, total)
                .compose(orderId -> {

                    List<Future> ops = new ArrayList<>();

                    for (OrderItem item : items) {
                        ops.add(
                            orderRepo.insertOrderItem(conn, orderId, item)
                                .compose(v -> {
                                    if (item.getProductId() != null) {
                                        return stockRepo.decreaseStock(
                                            branchId,
                                            item.getProductId(),
                                            item.getQuantity()
                                        );
                                    }
                                    return Future.succeededFuture();
                                })
                        );
                    }

                    return CompositeFuture.all(ops).map(orderId);
                })
                .onSuccess(orderId -> {
    tx.commit(ar -> {
        conn.close();
        if (ar.succeeded()) {
            promise.complete(orderId);
        } else {
            promise.fail(ar.cause());
        }
    });
})
.onFailure(err -> {
    tx.rollback(ar -> {
        conn.close();
        promise.fail(err);
    });
});

        });
    });

    return promise.future();
}

    /* ==========================
       REPRINT TICKET
    ========================== */

    public Future<RowSet<Row>> getOrderTicket(long orderId) {
        return orderRepo.getOrderWithItems(orderId);
    }
}
