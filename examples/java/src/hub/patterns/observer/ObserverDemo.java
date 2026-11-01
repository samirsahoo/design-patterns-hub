package hub.patterns.observer;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

record OrderPlaced(String orderId, String customer, BigDecimal total) { }

// region subject
/** Subject: keeps a list of observers and notifies them; knows nothing about what they do. */
final class EventPublisher<E> {
    /** Handle returned to observers so they can unsubscribe (avoids lapsed-listener leaks). */
    interface Subscription extends AutoCloseable {
        @Override void close();
    }

    private final List<Consumer<? super E>> observers = new CopyOnWriteArrayList<>();

    Subscription subscribe(Consumer<? super E> observer) {
        observers.add(observer);
        return () -> observers.remove(observer);
    }

    void publish(E event) {
        for (Consumer<? super E> observer : observers) {   // safe even if an observer unsubscribes now
            try {
                observer.accept(event);
            } catch (RuntimeException e) {                 // one failing observer must not stop the rest
                System.out.println("  observer failed: " + e.getMessage());
            }
        }
    }
}
// endregion

// region observers
final class ConfirmationEmailer implements Consumer<OrderPlaced> {
    @Override public void accept(OrderPlaced e) {
        System.out.println("  email: thanks " + e.customer() + " for order " + e.orderId());
    }
}

final class LoyaltyPoints implements Consumer<OrderPlaced> {
    @Override public void accept(OrderPlaced e) {
        System.out.println("  loyalty: +" + e.total().intValue() + " points for " + e.customer());
    }
}
// endregion

public final class ObserverDemo {

    public static void main(String[] args) {
        EventPublisher<OrderPlaced> orders = new EventPublisher<>();
        orders.subscribe(new ConfirmationEmailer());
        orders.subscribe(new LoyaltyPoints());
        EventPublisher.Subscription analytics =
                orders.subscribe(e -> System.out.println("  analytics: revenue " + e.total()));
        orders.subscribe(e -> {
            if (e.total().signum() == 0) {
                throw new IllegalStateException("fraud check cannot score a zero-value order");
            }
        });

        System.out.println("Publish ORD-1:");
        orders.publish(new OrderPlaced("ORD-1", "ana", new BigDecimal("129.90")));

        analytics.close();
        System.out.println("Publish ORD-2 (analytics unsubscribed):");
        orders.publish(new OrderPlaced("ORD-2", "raj", BigDecimal.ZERO));
    }
}
