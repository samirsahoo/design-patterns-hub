package com.example.orders.domain;

import java.util.Objects;
import java.util.UUID;

/** Typed identifier: an OrderId can never be passed where a CustomerId is expected. */
public record OrderId(UUID value) {

    public OrderId {
        Objects.requireNonNull(value, "value");
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId of(String value) {
        return new OrderId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
