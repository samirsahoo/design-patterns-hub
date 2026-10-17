package com.example.orders.adapter.in.web;

import com.example.orders.domain.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** HTTP response body. The domain object is never serialized directly. */
public record OrderResponse(String id, String customerId, String status, BigDecimal total, String currency,
                            Instant placedAt, List<Line> lines) {

    public record Line(String productId, int quantity, BigDecimal unitPrice) {
    }

    static OrderResponse from(Order order) {
        return new OrderResponse(
                order.id().toString(),
                order.customerId().value(),
                order.status().name(),
                order.total().amount(),
                order.total().currency().getCurrencyCode(),
                order.placedAt(),
                order.lines().stream()
                        .map(l -> new Line(l.productId(), l.quantity(), l.unitPrice().amount()))
                        .toList());
    }
}
