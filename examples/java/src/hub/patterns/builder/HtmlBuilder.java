package hub.patterns.builder;

/** Concrete builder: produces HTML. Tracks whether a list is open. */
public final class HtmlBuilder implements DocumentBuilder<String> {

    private final StringBuilder out = new StringBuilder();
    private boolean listOpen;

    @Override
    public HtmlBuilder title(String text) {
        out.append("<h1>").append(escape(text)).append("</h1>\n");
        return this;
    }

    @Override
    public HtmlBuilder section(String heading) {
        closeList();
        out.append("<h2>").append(escape(heading)).append("</h2>\n");
        return this;
    }

    @Override
    public HtmlBuilder bullet(String text) {
        if (!listOpen) {
            out.append("<ul>\n");
            listOpen = true;
        }
        out.append("  <li>").append(escape(text)).append("</li>\n");
        return this;
    }

    @Override
    public String build() {
        closeList();
        return out.toString();
    }

    private void closeList() {
        if (listOpen) {
            out.append("</ul>\n");
            listOpen = false;
        }
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
