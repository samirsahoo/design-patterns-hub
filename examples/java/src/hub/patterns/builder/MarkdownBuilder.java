package hub.patterns.builder;

/** Concrete builder: produces Markdown text. */
public final class MarkdownBuilder implements DocumentBuilder<String> {

    private final StringBuilder out = new StringBuilder();

    @Override
    public MarkdownBuilder title(String text) {
        out.append("# ").append(text).append("\n");
        return this;
    }

    @Override
    public MarkdownBuilder section(String heading) {
        out.append("\n## ").append(heading).append("\n");
        return this;
    }

    @Override
    public MarkdownBuilder bullet(String text) {
        out.append("- ").append(text).append("\n");
        return this;
    }

    @Override
    public String build() {
        return out.toString();
    }
}
