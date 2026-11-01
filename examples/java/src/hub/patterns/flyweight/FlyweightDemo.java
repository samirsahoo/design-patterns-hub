package hub.patterns.flyweight;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// region flyweight
/** Flyweight: intrinsic, immutable, shareable state. The icon bytes are the expensive part. */
record MarkerStyle(String category, String colour, byte[] icon) {
    void draw(double lat, double lon, StringBuilder canvas) {
        canvas.append(category).append('@').append(lat).append(',').append(lon).append(' ');
    }
}
// endregion

// region factory
/** Flyweight factory: returns the shared instance for each key, creating it at most once. */
final class MarkerStyles {
    private final Map<String, MarkerStyle> cache = new ConcurrentHashMap<>();

    MarkerStyle forCategory(String category) {
        return cache.computeIfAbsent(category, c -> {
            String colour = switch (c) {
                case "restaurant" -> "red";
                case "hotel" -> "blue";
                default -> "grey";
            };
            return new MarkerStyle(c, colour, new byte[16 * 1024]);   // pretend 16 KB icon
        });
    }

    int size() {
        return cache.size();
    }
}
// endregion

// region context
/** Context: extrinsic state (position) plus a reference to the shared flyweight. */
record Marker(double lat, double lon, MarkerStyle style) {
    void draw(StringBuilder canvas) {
        style.draw(lat, lon, canvas);
    }
}
// endregion

public final class FlyweightDemo {

    public static void main(String[] args) {
        MarkerStyles styles = new MarkerStyles();
        String[] categories = {"restaurant", "hotel", "museum"};

        List<Marker> markers = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            String category = categories[i % categories.length];
            markers.add(new Marker(48.0 + i * 1e-5, 2.0 + i * 1e-5, styles.forCategory(category)));
        }

        StringBuilder canvas = new StringBuilder();
        markers.subList(0, 3).forEach(m -> m.draw(canvas));

        long sharedIconBytes = (long) styles.size() * 16 * 1024;
        long unsharedIconBytes = (long) markers.size() * 16 * 1024;
        System.out.println("Markers: " + markers.size());
        System.out.println("Distinct MarkerStyle objects: " + styles.size());
        System.out.println("Same style instance reused: " + (markers.get(0).style() == markers.get(3).style()));
        System.out.printf("Icon memory shared: %,d KB vs unshared: %,d KB%n",
                sharedIconBytes / 1024, unsharedIconBytes / 1024);
        System.out.println("First draws: " + canvas.toString().trim());
    }
}
