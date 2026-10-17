package com.example.orders.domain;

import java.util.Objects;

/** Value object inside the Order aggregate. */
public record OrderLine(String productId, int quantity, Money unitPrice) {

    public OrderLine {
        Objects.requireNonNull(productId, "productId");
        Objects.requireNonNull(unitPrice, "unitPrice");
        if (quantity < 1) {
            throw new InvalidOrderException("Quantity must be at least 1 for product " + productId);
        }
    }

    public Money total() {
        return unitPrice.times(quantity);
    }
}
