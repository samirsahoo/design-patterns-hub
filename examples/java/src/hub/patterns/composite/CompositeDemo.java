package hub.patterns.composite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

// region component
/** Component: the one interface that clients use for single products and bundles alike. */
sealed interface CatalogItem permits Product, Bundle {
    String name();
    BigDecimal price();
    int itemCount();
    void print(String indent);
}
// endregion

// region leaf
/** Leaf: has no children. */
record Product(String name, BigDecimal price) implements CatalogItem {
    @Override public int itemCount() { return 1; }

    @Override public void print(String indent) {
        System.out.printf("%s- %s %s%n", indent, name, price);
    }
}
// endregion

// region composite
/** Composite: holds children (products or other bundles) and delegates to them. */
final class Bundle implements CatalogItem {
    private final String name;
    private final int discountPercent;
    private final List<CatalogItem> children = new ArrayList<>();

    Bundle(String name, int discountPercent) {
        this.name = name;
        this.discountPercent = discountPercent;
    }

    Bundle add(CatalogItem item) {
        children.add(item);
        return this;
    }

    @Override public String name() { return name; }

    @Override public BigDecimal price() {
        BigDecimal sum = children.stream().map(CatalogItem::price).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal factor = BigDecimal.valueOf(100 - discountPercent).movePointLeft(2);
        return sum.multiply(factor).setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override public int itemCount() {
        return children.stream().mapToInt(CatalogItem::itemCount).sum();
    }

    @Override public void print(String indent) {
        System.out.printf("%s+ %s (-%d%%) %s%n", indent, name, discountPercent, price());
        children.forEach(child -> child.print(indent + "    "));
    }
}
// endregion

public final class CompositeDemo {

    public static void main(String[] args) {
        Product laptop = new Product("Laptop", new BigDecimal("1200.00"));
        Product mouse = new Product("Mouse", new BigDecimal("40.00"));
        Product keyboard = new Product("Keyboard", new BigDecimal("80.00"));
        Product monitor = new Product("Monitor", new BigDecimal("300.00"));

        Bundle peripherals = new Bundle("Peripherals kit", 10).add(mouse).add(keyboard);
        Bundle office = new Bundle("Home office bundle", 5).add(laptop).add(monitor).add(peripherals);

        office.print("");

        // The client treats a single product and a nested bundle the same way.
        List<CatalogItem> cart = List.of(office, new Product("USB cable", new BigDecimal("9.99")));
        BigDecimal total = cart.stream().map(CatalogItem::price).reduce(BigDecimal.ZERO, BigDecimal::add);
        int items = cart.stream().mapToInt(CatalogItem::itemCount).sum();
        System.out.println("Cart: " + items + " items, total " + total);
    }
}
