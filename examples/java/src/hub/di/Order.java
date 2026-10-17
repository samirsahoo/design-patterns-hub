package hub.di;

import java.time.Instant;

public record Order(String id, String customerEmail, Instant placedAt) {
}
