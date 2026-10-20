package com.example.orders.adapter.out.payment;

import com.example.orders.application.port.out.PaymentPort;
import com.example.orders.domain.Money;
import com.example.orders.domain.OrderId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Outbound adapter standing in for a payment provider's HTTP API, so the
 * example runs without credentials. It approves charges up to a configured
 * limit and is idempotent per order id, as a real provider integration must be.
 * A production adapter would use RestClient, timeouts and retries here —
 * and the application core would not change.
 */
@Component
class SimulatedPaymentAdapter implements PaymentPort {

    private final BigDecimal approvalLimit;
    private final Map<OrderId, PaymentOutcome> processed = new ConcurrentHashMap<>();

    SimulatedPaymentAdapter(@Value("${payments.simulated.approval-limit:1000.00}") BigDecimal approvalLimit) {
        this.approvalLimit = approvalLimit;
    }

    @Override
    public PaymentOutcome charge(OrderId orderId, Money amount) {
        return processed.computeIfAbsent(orderId, id -> amount.amount().compareTo(approvalLimit) <= 0
                ? new PaymentOutcome.Approved("PAY-" + id.value().toString().substring(0, 8))
                : new PaymentOutcome.Declined("amount exceeds limit"));
    }
}
