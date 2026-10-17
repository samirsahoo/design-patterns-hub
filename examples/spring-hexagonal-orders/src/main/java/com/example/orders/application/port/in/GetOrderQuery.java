package com.example.orders.application.port.in;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderId;

/** Inbound port for reads. Kept separate so callers depend only on what they use. */
public interface GetOrderQuery {

    /** @throws OrderNotFoundException if no order has this id */
    Order getOrder(OrderId id);

    class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(OrderId id) {
            super("Order " + id + " not found");
        }
    }
}
