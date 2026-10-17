package com.example.orders.application.service;

import com.example.orders.application.port.in.GetOrderQuery.OrderNotFoundException;
import com.example.orders.application.port.in.PlaceOrderUseCase.PlaceOrderCommand;
import com.example.orders.application.port.out.OrderRepository;
import com.example.orders.application.port.out.PaymentPort;
import com.example.orders.application.port.out.PaymentPort.PaymentOutcome;
import com.example.orders.domain.Money;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderId;
import com.example.orders.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Use-case tests without Spring or a database: the outbound ports are
 * replaced by small hand-written fakes. This is the main payoff of the
 * hexagonal structure.
 */
class OrderApplicationServiceTest {

    private static final OrderId FIXED_ID = new OrderId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-01T12:00:00Z"), ZoneOffset.UTC);

    /** Fake outbound adapter: a map instead of a database. */
    static final class InMemoryOrderRepository implements OrderRepository {
        final Map<OrderId, Order> store = new HashMap<>();
        int saves;

        @Override
        public void save(Order order) {
            saves++;
            store.put(order.id(), order);
        }

        @Override
        public Optional<Order> findById(OrderId id) {
            return Optional.ofNullable(store.get(id));
        }
    }

    private final InMemoryOrderRepository repository = new InMemoryOrderRepository();
    private final List<Money> charges = new ArrayList<>();

    private OrderApplicationService serviceWithPayments(PaymentOutcome outcome) {
        PaymentPort payments = (orderId, amount) -> {
            charges.add(amount);
            return outcome;
        };
        return new OrderApplicationService(repository, payments, CLOCK, () -> FIXED_ID);
    }

    private static PlaceOrderCommand command() {
        return new PlaceOrderCommand("c-42", "EUR", List.of(
                new PlaceOrderCommand.Line("book", 2, new BigDecimal("12.50")),
                new PlaceOrderCommand.Line("pen", 1, new BigDecimal("3.00"))));
    }

    @Test
    void approvedPaymentMarksTheOrderPaid() {
        var service = serviceWithPayments(new PaymentOutcome.Approved("PAY-123"));

        Order order = service.placeOrder(command());

        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
        assertThat(order.paymentReference()).isEqualTo("PAY-123");
        assertThat(order.placedAt()).isEqualTo(Instant.parse("2026-03-01T12:00:00Z"));
        assertThat(charges).containsExactly(Money.of("28.00", "EUR"));
        assertThat(repository.findById(FIXED_ID)).contains(order);
    }

    @Test
    void declinedPaymentIsRecordedNotThrown() {
        var service = serviceWithPayments(new PaymentOutcome.Declined("insufficient funds"));

        Order order = service.placeOrder(command());

        assertThat(order.status()).isEqualTo(OrderStatus.PAYMENT_DECLINED);
        assertThat(repository.store.get(FIXED_ID).status()).isEqualTo(OrderStatus.PAYMENT_DECLINED);
    }

    @Test
    void theOrderIsStoredBeforeThePaymentIsAttempted() {
        var service = new OrderApplicationService(repository, (orderId, amount) -> {
            assertThat(repository.findById(orderId)).isPresent();   // intent recorded first
            return new PaymentOutcome.Approved("PAY-1");
        }, CLOCK, () -> FIXED_ID);

        service.placeOrder(command());

        assertThat(repository.saves).isEqualTo(2);
    }

    @Test
    void unknownOrderIsReported() {
        var service = serviceWithPayments(new PaymentOutcome.Approved("unused"));

        assertThatThrownBy(() -> service.getOrder(FIXED_ID)).isInstanceOf(OrderNotFoundException.class);
    }
}
