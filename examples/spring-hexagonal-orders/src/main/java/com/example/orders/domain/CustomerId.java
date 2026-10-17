package com.example.orders.domain;

import java.util.Objects;

public record CustomerId(String value) {

    public CustomerId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new InvalidOrderException("Customer id must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
