package com.example.orders.adapter.out.persistence;

import com.example.orders.domain.CustomerId;
import com.example.orders.domain.Money;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderId;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Adapter integration test: real JPA and an embedded database, no web layer
 * and no application service. Verifies the mapping in both directions.
 */
@DataJpaTest
@Import(JpaOrderRepositoryAdapter.class)
class JpaOrderRepositoryAdapterTest {

    @Autowired
    JpaOrderRepositoryAdapter adapter;

    @Test
    void savesAndLoadsAnOrderWithItsLines() {
        Order order = Order.place(OrderId.newId(), new CustomerId("c-42"), List.of(
                new OrderLine("book", 2, Money.of("12.50", "EUR")),
                new OrderLine("pen", 1, Money.of("3.00", "EUR"))), Instant.parse("2026-03-01T12:00:00Z"));
        order.markPaid("PAY-9");

        adapter.save(order);
        Order loaded = adapter.findById(order.id()).orElseThrow();

        assertThat(loaded.status()).isEqualTo(OrderStatus.PAID);
        assertThat(loaded.paymentReference()).isEqualTo("PAY-9");
        assertThat(loaded.lines()).containsExactlyElementsOf(order.lines());
        assertThat(loaded.total()).isEqualTo(Money.of("28.00", "EUR"));
    }

    @Test
    void updatesAnExistingOrder() {
        Order order = Order.place(OrderId.newId(), new CustomerId("c-1"),
                List.of(new OrderLine("book", 1, Money.of("9.99", "EUR"))), Instant.parse("2026-03-01T12:00:00Z"));
        adapter.save(order);

        order.markPaymentDeclined();
        adapter.save(order);

        assertThat(adapter.findById(order.id()).orElseThrow().status()).isEqualTo(OrderStatus.PAYMENT_DECLINED);
    }
}
