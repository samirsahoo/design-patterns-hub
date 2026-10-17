package hub.di;

import java.time.Instant;
import java.util.UUID;

/**
 * Before: the service creates its own collaborators. It works, but it cannot
 * be tested without a real clock and real notifications, and every change of
 * implementation means editing this class.
 */
public final class TightlyCoupledOrderService {

    private final OrderRepository repository = new InMemoryOrderRepository();
    private final Notifier notifier = new ConsoleNotifier();

    public Order placeOrder(String customerEmail) {
        Order order = new Order(UUID.randomUUID().toString(), customerEmail, Instant.now());
        repository.save(order);
        notifier.send(customerEmail, "Order " + order.id() + " received");
        return order;
    }
}
