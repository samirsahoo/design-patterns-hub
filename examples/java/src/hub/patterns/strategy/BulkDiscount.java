package hub.patterns.strategy;

import java.math.BigDecimal;

/** Concrete strategy: a fixed amount off when the cart has at least {@code minItems} items. */
public record BulkDiscount(int minItems, BigDecimal amountOff) implements DiscountPolicy {

    @Override
    public BigDecimal discountFor(Cart cart) {
        if (cart.itemCount() < minItems) {
            return BigDecimal.ZERO;
        }
        return amountOff.min(cart.subtotal()); // never discount more than the subtotal
    }
}
