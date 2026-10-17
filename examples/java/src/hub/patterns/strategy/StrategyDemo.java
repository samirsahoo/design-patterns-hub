package hub.patterns.strategy;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StrategyDemo {

    public static void main(String[] args) {
        Cart cart = new Cart(List.of(
                new Cart.Line("BOOK-1", 2, new BigDecimal("25.00")),
                new Cart.Line("PEN-7", 1, new BigDecimal("70.00"))));

        // A lambda is a perfectly good strategy when it needs no state or name.
        DayOfWeek today = DayOfWeek.SATURDAY;
        DiscountPolicy weekend = c -> today.getValue() >= 6
                ? new PercentageDiscount(5).discountFor(c)
                : BigDecimal.ZERO;

        Map<String, DiscountPolicy> policies = new LinkedHashMap<>();
        policies.put("No discount", new NoDiscount());
        policies.put("10% off", new PercentageDiscount(10));
        policies.put("5.00 off 3+ items", new BulkDiscount(3, new BigDecimal("5.00")));
        policies.put("Weekend 5% (lambda)", weekend);

        System.out.println("Subtotal: " + cart.subtotal());
        policies.forEach((name, policy) -> {
            Checkout checkout = new Checkout(policy);
            System.out.printf("%-20s -> total %s%n", name, checkout.total(cart));
        });
    }
}
