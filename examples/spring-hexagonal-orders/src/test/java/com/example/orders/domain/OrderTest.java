package com.example.orders.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pure domain tests: no Spring, no database, milliseconds to run. */
class OrderTest {

    private static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");

    private static Order newOrder(OrderLine... lines) {
        return Order.place(OrderId.newId(), new CustomerId("c-42"), List.of(lines), NOW);
    }

    @Test
    void totalIsDerivedFromLines() {
        Order order = newOrder(
                new OrderLine("book", 2, Money.of("12.50", "EUR")),
                new OrderLine("pen", 1, Money.of("3.00", "EUR")));

        assertThat(order.total()).isEqualTo(Money.of("28.00", "EUR"));
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    void rejectsAnOrderWithoutLines() {
        assertThatThrownBy(OrderTest::newOrder)
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("at least one line");
    }

    @Test
    void rejectsTooManyLines() {
        List<OrderLine> lines = Collections.nCopies(Order.MAX_LINES + 1, new OrderLine("x", 1, Money.of("1", "EUR")));
        assertThatThrownBy(() -> Order.place(OrderId.newId(), new CustomerId("c-1"), lines, NOW))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void rejectsMixedCurrencies() {
        assertThatThrownBy(() -> newOrder(
                new OrderLine("a", 1, Money.of("1.00", "EUR")),
                new OrderLine("b", 1, Money.of("1.00", "USD"))))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("same currency");
    }

    @Test
    void aPaidOrderCannotBeCancelled() {
        Order order = newOrder(new OrderLine("book", 1, Money.of("10.00", "EUR")));
        order.markPaid("PAY-1");

        assertThatThrownBy(order::cancel)
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("in status PAID");
    }
}
