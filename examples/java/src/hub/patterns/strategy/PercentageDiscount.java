package hub.patterns.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Concrete strategy: a percentage off the whole cart. */
public record PercentageDiscount(int percent) implements DiscountPolicy {

    public PercentageDiscount {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("percent must be between 0 and 100");
        }
    }

    @Override
    public BigDecimal discountFor(Cart cart) {
        return cart.subtotal()
                .multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);
    }
}
