package com.example.orders.application.port.out;

import com.example.orders.domain.Money;
import com.example.orders.domain.OrderId;

/**
 * Outbound port: charge a customer for an order. The order id doubles as an
 * idempotency key, so a retried call must not charge twice.
 */
public interface PaymentPort {

    PaymentOutcome charge(OrderId orderId, Money amount);

    sealed interface PaymentOutcome {
        record Approved(String reference) implements PaymentOutcome { }
        record Declined(String reason) implements PaymentOutcome { }
    }
}
