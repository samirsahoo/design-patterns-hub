package hub.di;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/** The composition root: the one place that knows which implementations are used. */
public final class DiDemo {

    public static void main(String[] args) {
        // Production-style wiring (what a DI container automates).
        OrderService production = new OrderService(
                new InMemoryOrderRepository(),
                new ConsoleNotifier(),
                Clock.systemUTC(),
                () -> UUID.randomUUID().toString());
        Order real = production.placeOrder("ana@example.com");
        System.out.println("Production wiring placed an order with a random id: " + (real.id().length() == 36));

        // Test-style wiring: deterministic clock, predictable ids, recording notifier.
        Clock fixed = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);
        AtomicInteger seq = new AtomicInteger();
        List<String> sent = new ArrayList<>();
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        OrderService underTest = new OrderService(
                repository,
                (to, msg) -> sent.add(to + ": " + msg),
                fixed,
                () -> "ORD-" + seq.incrementAndGet());

        Order order = underTest.placeOrder("ben@example.com");
        System.out.println("Placed " + order.id() + " at " + order.placedAt());
        System.out.println("Stored: " + repository.findById("ORD-1").isPresent());
        System.out.println("Notifications: " + sent);
    }
}
