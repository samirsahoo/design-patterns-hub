package hub.patterns.strategy;

import java.math.BigDecimal;

/**
 * Strategy: one interchangeable way of calculating a discount for a cart.
 * It is a functional interface, so simple strategies can be lambdas.
 */
@FunctionalInterface
public interface DiscountPolicy {

    /** Returns the discount amount (never negative, never more than the subtotal). */
    BigDecimal discountFor(Cart cart);
}
