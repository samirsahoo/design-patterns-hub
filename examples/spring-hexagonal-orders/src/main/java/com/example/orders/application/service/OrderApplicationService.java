package com.example.orders.application.service;

import com.example.orders.application.port.in.GetOrderQuery;
import com.example.orders.application.port.in.PlaceOrderUseCase;
import com.example.orders.application.port.out.OrderRepository;
import com.example.orders.application.port.out.PaymentPort;
import com.example.orders.application.port.out.PaymentPort.PaymentOutcome;
import com.example.orders.domain.CustomerId;
import com.example.orders.domain.Money;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderId;
import com.example.orders.domain.OrderLine;

import java.time.Clock;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Application service: orchestrates a use case. It translates the command
 * into domain objects, lets the aggregate enforce business rules, and talks
 * to the outside world only through outbound ports. Plain Java — Spring
 * wiring happens in the config package.
 */
public final class OrderApplicationService implements PlaceOrderUseCase, GetOrderQuery {

    private final OrderRepository orders;
    private final PaymentPort payments;
    private final Clock clock;
    private final Supplier<OrderId> ids;

    public OrderApplicationService(OrderRepository orders, PaymentPort payments, Clock clock, Supplier<OrderId> ids) {
        this.orders = Objects.requireNonNull(orders);
        this.payments = Objects.requireNonNull(payments);
        this.clock = Objects.requireNonNull(clock);
        this.ids = Objects.requireNonNull(ids);
    }

    @Override
    public Order placeOrder(PlaceOrderCommand command) {
        Currency currency = Currency.getInstance(command.currency());
        List<OrderLine> lines = command.lines().stream()
                .map(l -> new OrderLine(l.productId(), l.quantity(), new Money(l.unitPrice(), currency)))
                .toList();

        Order order = Order.place(ids.get(), new CustomerId(command.customerId()), lines, clock.instant());
        orders.save(order);                         // 1. record the intent first

        switch (payments.charge(order.id(), order.total())) {   // 2. call the outside world
            case PaymentOutcome.Approved approved -> order.markPaid(approved.reference());
            case PaymentOutcome.Declined declined -> order.markPaymentDeclined();
        }
        orders.save(order);                         // 3. record the outcome
        return order;
    }

    @Override
    public Order getOrder(OrderId id) {
        return orders.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
