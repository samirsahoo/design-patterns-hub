package hub.patterns.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

// region subject
/** Subject: the interface shared by the real object and its proxies. */
interface DocumentStore {
    String read(String id);
}

/** Real subject: expensive to create (opens a connection, loads an index). */
final class RemoteDocumentStore implements DocumentStore {
    RemoteDocumentStore() {
        System.out.println("  [RemoteDocumentStore created: connected and index loaded]");
    }

    @Override public String read(String id) {
        System.out.println("  [remote read " + id + "]");
        return "contents of " + id;
    }
}
// endregion

// region virtual
/** Virtual proxy: delays creating the real subject until it is first needed. */
final class LazyDocumentStore implements DocumentStore {
    private final Supplier<DocumentStore> factory;
    private DocumentStore target;          // not thread-safe; see the article for options

    LazyDocumentStore(Supplier<DocumentStore> factory) {
        this.factory = factory;
    }

    @Override public String read(String id) {
        if (target == null) {
            target = factory.get();
        }
        return target.read(id);
    }
}
// endregion

// region protection
/** Protection proxy: checks permissions before forwarding. */
final class AccessControlledStore implements DocumentStore {
    private final DocumentStore target;
    private final Supplier<Set<String>> currentUserRoles;

    AccessControlledStore(DocumentStore target, Supplier<Set<String>> currentUserRoles) {
        this.target = target;
        this.currentUserRoles = currentUserRoles;
    }

    @Override public String read(String id) {
        if (id.startsWith("hr/") && !currentUserRoles.get().contains("HR")) {
            throw new SecurityException("access to " + id + " denied");
        }
        return target.read(id);
    }
}
// endregion

// region caching
/** Caching proxy: answers repeated requests without calling the real subject. */
final class CachingDocumentStore implements DocumentStore {
    private final DocumentStore target;
    private final Map<String, String> cache = new HashMap<>();

    CachingDocumentStore(DocumentStore target) {
        this.target = target;
    }

    @Override public String read(String id) {
        return cache.computeIfAbsent(id, target::read);
    }
}
// endregion

public final class ProxyDemo {

    public static void main(String[] args) {
        Set<String> roles = new java.util.HashSet<>(Set.of("ENGINEER"));
        DocumentStore store = new AccessControlledStore(
                new CachingDocumentStore(new LazyDocumentStore(RemoteDocumentStore::new)),
                () -> roles);

        System.out.println("Proxy chain built; nothing remote created yet.");
        System.out.println("1: " + store.read("wiki/onboarding"));
        System.out.println("2: " + store.read("wiki/onboarding"));
        try {
            store.read("hr/salaries");
        } catch (SecurityException e) {
            System.out.println("3: " + e.getMessage());
        }
        roles.add("HR");
        System.out.println("4: " + store.read("hr/salaries"));
    }
}
