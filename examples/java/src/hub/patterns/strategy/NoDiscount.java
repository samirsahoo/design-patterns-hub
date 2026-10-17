package hub.patterns.strategy;

import java.math.BigDecimal;

/** Concrete strategy: the "null object" policy, so Checkout never needs a null check. */
public final class NoDiscount implements DiscountPolicy {

    @Override
    public BigDecimal discountFor(Cart cart) {
        return BigDecimal.ZERO;
    }
}
