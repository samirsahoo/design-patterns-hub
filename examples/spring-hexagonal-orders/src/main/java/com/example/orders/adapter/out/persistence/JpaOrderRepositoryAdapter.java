package com.example.orders.adapter.out.persistence;

import com.example.orders.application.port.out.OrderRepository;
import com.example.orders.domain.CustomerId;
import com.example.orders.domain.Money;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderId;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.Optional;

/**
 * Outbound (driven) adapter: implements the OrderRepository port with JPA
 * and maps between the domain model and the persistence model.
 */
@Component
class JpaOrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository jpa;

    JpaOrderRepositoryAdapter(SpringDataOrderRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Order order) {
        OrderJpaEntity entity = jpa.findById(order.id().value()).orElseGet(OrderJpaEntity::new);
        entity.id = order.id().value();
        entity.customerId = order.customerId().value();
        entity.currency = order.total().currency().getCurrencyCode();
        entity.status = order.status().name();
        entity.paymentReference = order.paymentReference();
        entity.placedAt = order.placedAt();
        entity.lines.clear();
        order.lines().forEach(l -> entity.lines.add(
                new OrderJpaEntity.LineEmbeddable(l.productId(), l.quantity(), l.unitPrice().amount())));
        jpa.save(entity);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpa.findById(id.value()).map(JpaOrderRepositoryAdapter::toDomain);
    }

    private static Order toDomain(OrderJpaEntity e) {
        Currency currency = Currency.getInstance(e.currency);
        return Order.rehydrate(
                new OrderId(e.id),
                new CustomerId(e.customerId),
                e.lines.stream()
                        .map(l -> new OrderLine(l.productId, l.quantity, new Money(l.unitPrice, currency)))
                        .toList(),
                e.placedAt,
                OrderStatus.valueOf(e.status),
                e.paymentReference);
    }
}
