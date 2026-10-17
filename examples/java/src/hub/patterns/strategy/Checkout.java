package hub.patterns.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Context: calculates totals and delegates the variable part (the discount)
 * to whichever DiscountPolicy it was given. It never asks which policy it has.
 */
public final class Checkout {

    private final DiscountPolicy discountPolicy;

    public Checkout(DiscountPolicy discountPolicy) {
        this.discountPolicy = Objects.requireNonNull(discountPolicy);
    }

    public BigDecimal total(Cart cart) {
        BigDecimal discount = discountPolicy.discountFor(cart);
        return cart.subtotal().subtract(discount).setScale(2, RoundingMode.HALF_EVEN);
    }
}
