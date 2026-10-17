package com.example.orders.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root. All changes go through methods that protect the invariants:
 * - an order has between 1 and MAX_LINES lines, all in one currency;
 * - the status only moves along allowed transitions;
 * - the total is always derived from the lines, never stored separately.
 * No framework annotations: persistence mapping lives in an adapter.
 */
public final class Order {

    public static final int MAX_LINES = 50;

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderLine> lines;
    private final Instant placedAt;
    private OrderStatus status;
    private String paymentReference;

    private Order(OrderId id, CustomerId customerId, List<OrderLine> lines, Instant placedAt,
                  OrderStatus status, String paymentReference) {
        this.id = Objects.requireNonNull(id);
        this.customerId = Objects.requireNonNull(customerId);
        this.lines = List.copyOf(lines);
        this.placedAt = Objects.requireNonNull(placedAt);
        this.status = Objects.requireNonNull(status);
        this.paymentReference = paymentReference;
        checkLines(this.lines);
    }

    /** Factory for new orders: the only way to start the lifecycle. */
    public static Order place(OrderId id, CustomerId customerId, List<OrderLine> lines, Instant now) {
        return new Order(id, customerId, lines, now, OrderStatus.PENDING_PAYMENT, null);
    }

    /** Recreates an order from storage. Used only by persistence adapters. */
    public static Order rehydrate(OrderId id, CustomerId customerId, List<OrderLine> lines, Instant placedAt,
                                  OrderStatus status, String paymentReference) {
        return new Order(id, customerId, lines, placedAt, status, paymentReference);
    }

    public void markPaid(String paymentReference) {
        requireStatus(OrderStatus.PENDING_PAYMENT, "mark as paid");
        this.paymentReference = Objects.requireNonNull(paymentReference);
        this.status = OrderStatus.PAID;
    }

    public void markPaymentDeclined() {
        requireStatus(OrderStatus.PENDING_PAYMENT, "decline payment for");
        this.status = OrderStatus.PAYMENT_DECLINED;
    }

    public void cancel() {
        requireStatus(OrderStatus.PENDING_PAYMENT, "cancel");
        this.status = OrderStatus.CANCELLED;
    }

    public Money total() {
        Money sum = Money.zero(lines.getFirst().unitPrice().currency());
        for (OrderLine line : lines) {
            sum = sum.plus(line.total());
        }
        return sum;
    }

    private void requireStatus(OrderStatus expected, String action) {
        if (status != expected) {
            throw new InvalidOrderException("Cannot " + action + " order " + id + " in status " + status);
        }
    }

    private static void checkLines(List<OrderLine> lines) {
        if (lines.isEmpty()) {
            throw new InvalidOrderException("An order needs at least one line");
        }
        if (lines.size() > MAX_LINES) {
            throw new InvalidOrderException("An order can have at most " + MAX_LINES + " lines");
        }
        var currency = lines.getFirst().unitPrice().currency();
        if (lines.stream().anyMatch(l -> !l.unitPrice().currency().equals(currency))) {
            throw new InvalidOrderException("All lines must use the same currency");
        }
    }

    public OrderId id() { return id; }
    public CustomerId customerId() { return customerId; }
    public List<OrderLine> lines() { return lines; }
    public Instant placedAt() { return placedAt; }
    public OrderStatus status() { return status; }
    public String paymentReference() { return paymentReference; }
}
