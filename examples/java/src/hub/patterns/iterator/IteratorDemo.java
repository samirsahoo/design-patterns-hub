package hub.patterns.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

record Page<T>(List<T> items, String nextCursor) { }

/** A remote API that returns results one page at a time (cursor-based pagination). */
interface CustomerApi {
    Page<String> fetch(String cursor, int pageSize);
}

// region iterator
/** Aggregate: hides the paging protocol behind Iterable, so clients just loop. */
final class AllCustomers implements Iterable<String> {
    private final CustomerApi api;
    private final int pageSize;

    AllCustomers(CustomerApi api, int pageSize) {
        this.api = api;
        this.pageSize = pageSize;
    }

    @Override public Iterator<String> iterator() {
        return new Iterator<>() {                       // concrete iterator: one per traversal
            private Iterator<String> current = List.<String>of().iterator();
            private String cursor = "";                 // "" = first page, null = no more pages

            @Override public boolean hasNext() {
                while (!current.hasNext() && cursor != null) {   // fetch lazily, only when needed
                    Page<String> page = api.fetch(cursor, pageSize);
                    current = page.items().iterator();
                    cursor = page.nextCursor();
                }
                return current.hasNext();
            }

            @Override public String next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return current.next();
            }
        };
    }

    Stream<String> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
// endregion

public final class IteratorDemo {

    public static void main(String[] args) {
        List<String> database = IntStream.rangeClosed(1, 7).mapToObj(i -> "customer-" + i).toList();
        CustomerApi api = (cursor, size) -> {
            int start = cursor.isEmpty() ? 0 : Integer.parseInt(cursor);
            int end = Math.min(start + size, database.size());
            System.out.println("  [fetch page starting at " + start + "]");
            return new Page<>(database.subList(start, end), end < database.size() ? String.valueOf(end) : null);
        };

        AllCustomers customers = new AllCustomers(api, 3);
        System.out.println("for-each over every page:");
        for (String c : customers) {
            System.out.println("  " + c);
        }

        System.out.println("Stream, first 2 only (one page fetched):");
        customers.stream().limit(2).forEach(c -> System.out.println("  " + c));
    }
}
