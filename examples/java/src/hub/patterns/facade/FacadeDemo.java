package hub.patterns.facade;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

// region subsystem
/** Subsystem classes: each is useful on its own and knows nothing about the facade. */
final class InventoryService {
    private final Map<String, Integer> stock = new HashMap<>(Map.of("SKU-1", 5, "SKU-2", 0));

    String reserve(String sku, int qty) {
        int available = stock.getOrDefault(sku, 0);
        if (available < qty) {
            throw new IllegalStateException("only " + available + " of " + sku + " in stock");
        }
        stock.put(sku, available - qty);
        System.out.println("  inventory: reserved " + qty + " x " + sku);
        return "RES-" + sku;
    }

    void release(String reservationId) {
        System.out.println("  inventory: released " + reservationId);
    }
}

final class PaymentService {
    String charge(String customerId, BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("1000")) > 0) {
            throw new IllegalStateException("card declined");
        }
        System.out.println("  payment: charged " + amount + " to " + customerId);
        return "PAY-" + customerId;
    }
}

final class ShippingService {
    String schedule(String customerId, String reservationId) {
        System.out.println("  shipping: scheduled " + reservationId + " for " + customerId);
        return "SHP-1";
    }
}

final class NotificationService {
    void orderConfirmed(String customerId, String shipmentId) {
        System.out.println("  notify: order confirmed for " + customerId + ", shipment " + shipmentId);
    }
}
// endregion

// region facade
/** Facade: one simple operation that hides the order and the compensation logic of the subsystem calls. */
final class CheckoutFacade {
    private final InventoryService inventory;
    private final PaymentService payments;
    private final ShippingService shipping;
    private final NotificationService notifications;

    CheckoutFacade(InventoryService inventory, PaymentService payments,
                   ShippingService shipping, NotificationService notifications) {
        this.inventory = inventory;
        this.payments = payments;
        this.shipping = shipping;
        this.notifications = notifications;
    }

    String placeOrder(String customerId, String sku, int qty, BigDecimal unitPrice) {
        String reservation = inventory.reserve(sku, qty);
        try {
            payments.charge(customerId, unitPrice.multiply(BigDecimal.valueOf(qty)));
        } catch (RuntimeException e) {
            inventory.release(reservation);          // compensate: callers never have to remember
            return "FAILED (" + e.getMessage() + ")";
        }
        String shipment = shipping.schedule(customerId, reservation);
        notifications.orderConfirmed(customerId, shipment);
        return "CONFIRMED";
    }
}
// endregion

public final class FacadeDemo {

    public static void main(String[] args) {
        CheckoutFacade checkout = new CheckoutFacade(new InventoryService(), new PaymentService(),
                new ShippingService(), new NotificationService());

        System.out.println("Order 1:");
        System.out.println("=> " + checkout.placeOrder("C-7", "SKU-1", 2, new BigDecimal("49.50")));
        System.out.println("Order 2:");
        System.out.println("=> " + checkout.placeOrder("C-8", "SKU-1", 3, new BigDecimal("400.00")));
    }
}
