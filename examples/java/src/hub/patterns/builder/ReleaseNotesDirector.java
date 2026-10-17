package hub.patterns.builder;

import java.util.List;

/**
 * GoF Director: knows the construction *sequence* for release notes,
 * but not the output format. Any DocumentBuilder can be plugged in.
 */
public final class ReleaseNotesDirector {

    public record Release(String version, List<String> features, List<String> fixes) {
    }

    public <T> T construct(Release release, DocumentBuilder<T> builder) {
        builder.title("Release " + release.version());
        if (!release.features().isEmpty()) {
            builder.section("New features");
            release.features().forEach(builder::bullet);
        }
        if (!release.fixes().isEmpty()) {
            builder.section("Bug fixes");
            release.fixes().forEach(builder::bullet);
        }
        return builder.build();
    }
}
