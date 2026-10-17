package hub.di;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * After: every collaborator arrives through the constructor. The class states
 * its dependencies honestly, they can be final, and it is trivial to test.
 */
public final class OrderService {

    private final OrderRepository repository;
    private final Notifier notifier;
    private final Clock clock;
    private final Supplier<String> idGenerator;

    public OrderService(OrderRepository repository, Notifier notifier, Clock clock, Supplier<String> idGenerator) {
        this.repository = Objects.requireNonNull(repository);
        this.notifier = Objects.requireNonNull(notifier);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public Order placeOrder(String customerEmail) {
        Order order = new Order(idGenerator.get(), customerEmail, Instant.now(clock));
        repository.save(order);
        notifier.send(customerEmail, "Order " + order.id() + " received");
        return order;
    }
}
