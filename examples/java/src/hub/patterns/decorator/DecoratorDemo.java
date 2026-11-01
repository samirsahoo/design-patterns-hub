package hub.patterns.decorator;

import java.util.HashMap;
import java.util.Map;

// region component
/** Component: the interface that both the real client and every decorator implement. */
interface HttpClient {
    String get(String url);
}

/** Concrete component: fails the first call to each URL, to show the retry decorator at work. */
final class FlakyHttpClient implements HttpClient {
    private final Map<String, Integer> calls = new HashMap<>();

    @Override public String get(String url) {
        int n = calls.merge(url, 1, Integer::sum);
        System.out.println("      network GET " + url + " (call " + n + ")");
        if (n == 1) {
            throw new IllegalStateException("connection reset");
        }
        return "200 OK body-of-" + url;
    }
}
// endregion

// region decorators
/** Base decorator: holds the wrapped component and forwards by default. */
abstract class HttpClientDecorator implements HttpClient {
    protected final HttpClient inner;

    protected HttpClientDecorator(HttpClient inner) {
        this.inner = inner;
    }

    @Override public String get(String url) {
        return inner.get(url);
    }
}

final class LoggingClient extends HttpClientDecorator {
    LoggingClient(HttpClient inner) { super(inner); }

    @Override public String get(String url) {
        System.out.println("  -> GET " + url);
        String response = inner.get(url);
        System.out.println("  <- " + response);
        return response;
    }
}

final class RetryingClient extends HttpClientDecorator {
    private final int maxAttempts;

    RetryingClient(HttpClient inner, int maxAttempts) {
        super(inner);
        this.maxAttempts = maxAttempts;
    }

    @Override public String get(String url) {
        for (int attempt = 1; ; attempt++) {
            try {
                return inner.get(url);
            } catch (IllegalStateException e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                System.out.println("    retry after: " + e.getMessage());
            }
        }
    }
}

final class CachingClient extends HttpClientDecorator {
    private final Map<String, String> cache = new HashMap<>();

    CachingClient(HttpClient inner) { super(inner); }

    @Override public String get(String url) {
        String cached = cache.get(url);
        if (cached != null) {
            System.out.println("    cache hit " + url);
            return cached;
        }
        String response = inner.get(url);
        cache.put(url, response);
        return response;
    }
}
// endregion

public final class DecoratorDemo {

    public static void main(String[] args) {
        // region wiring
        HttpClient client = new LoggingClient(
                new CachingClient(
                        new RetryingClient(new FlakyHttpClient(), 3)));
        // endregion

        System.out.println("First request:");
        client.get("/products/42");
        System.out.println("Second request:");
        client.get("/products/42");
    }
}
