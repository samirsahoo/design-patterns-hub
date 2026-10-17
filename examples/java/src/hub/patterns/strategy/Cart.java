package hub.patterns.strategy;

import java.math.BigDecimal;
import java.util.List;

/** The data every discount strategy works on. Immutable. */
public record Cart(List<Line> lines) {

    public record Line(String sku, int quantity, BigDecimal unitPrice) {
        public BigDecimal total() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public Cart {
        lines = List.copyOf(lines);
    }

    public BigDecimal subtotal() {
        return lines.stream().map(Line::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int itemCount() {
        return lines.stream().mapToInt(Line::quantity).sum();
    }
}
