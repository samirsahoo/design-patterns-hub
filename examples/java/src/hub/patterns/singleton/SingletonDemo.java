package hub.patterns.singleton;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

// region enum
/** The simplest correct singleton in Java: thread-safe, serialization-safe and reflection-safe. */
enum IdGenerator {
    INSTANCE;

    private final AtomicLong next = new AtomicLong(1);

    long nextId() {
        return next.getAndIncrement();
    }
}
// endregion

// region holder
/** Initialization-on-demand holder: lazy, and thread-safe thanks to JVM class initialization. */
final class AppConfig {
    private final String environment;

    private AppConfig() {
        System.out.println("  (AppConfig constructor runs now)");
        this.environment = System.getProperty("app.env", "dev");
    }

    private static final class Holder {
        static final AppConfig INSTANCE = new AppConfig();   // runs on first access to Holder
    }

    static AppConfig getInstance() {
        return Holder.INSTANCE;
    }

    String environment() {
        return environment;
    }
}
// endregion

// region dcl
/** Double-checked locking: correct only because the field is volatile. Prefer the two forms above. */
final class ConnectionRegistry {
    private static volatile ConnectionRegistry instance;

    private ConnectionRegistry() { }

    static ConnectionRegistry getInstance() {
        ConnectionRegistry local = instance;          // one volatile read on the fast path
        if (local == null) {
            synchronized (ConnectionRegistry.class) {
                local = instance;
                if (local == null) {
                    instance = local = new ConnectionRegistry();
                }
            }
        }
        return local;
    }
}
// endregion

// region injected
/** Testable alternative: depend on an abstraction and let the composition root pass the singleton in. */
final class InvoiceNumberer {
    private final LongSupplier ids;

    InvoiceNumberer(LongSupplier ids) {
        this.ids = ids;
    }

    String next() {
        return "INV-%05d".formatted(ids.getAsLong());
    }
}
// endregion

public final class SingletonDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Before first use of AppConfig");
        System.out.println("Environment: " + AppConfig.getInstance().environment());
        System.out.println("Same AppConfig instance: " + (AppConfig.getInstance() == AppConfig.getInstance()));

        Set<Long> ids = ConcurrentHashMap.newKeySet();
        Set<ConnectionRegistry> registries = Collections.synchronizedSet(
                Collections.newSetFromMap(new IdentityHashMap<>()));
        try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
            for (int t = 0; t < 8; t++) {
                pool.submit(() -> {
                    for (int i = 0; i < 1_000; i++) {
                        ids.add(IdGenerator.INSTANCE.nextId());
                        registries.add(ConnectionRegistry.getInstance());
                    }
                });
            }
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);
        }
        System.out.println("Unique ids from 8 threads x 1000 calls: " + ids.size());
        System.out.println("Distinct ConnectionRegistry instances: " + registries.size());

        // Production wiring passes the singleton in; a test passes a predictable fake.
        InvoiceNumberer production = new InvoiceNumberer(IdGenerator.INSTANCE::nextId);
        InvoiceNumberer test = new InvoiceNumberer(() -> 42);
        System.out.println("Production invoice: " + production.next());
        System.out.println("Test invoice:       " + test.next());
    }
}
