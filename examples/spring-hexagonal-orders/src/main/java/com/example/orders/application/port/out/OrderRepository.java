package com.example.orders.application.port.out;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderId;

import java.util.Optional;

/**
 * Outbound port: the application needs to store and load orders.
 * It does not care whether that is JPA, JDBC, a document store or memory.
 */
public interface OrderRepository {

    void save(Order order);

    Optional<Order> findById(OrderId id);
}
