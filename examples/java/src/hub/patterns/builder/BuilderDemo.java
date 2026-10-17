package hub.patterns.builder;

import java.math.BigDecimal;
import java.util.List;

public final class BuilderDemo {

    public static void main(String[] args) {
        // 1. Fluent builder for an immutable object with optional fields.
        SearchQuery query = SearchQuery.builder("laptop")
                .category("computers")
                .priceBetween(new BigDecimal("500"), new BigDecimal("1500"))
                .sort(SearchQuery.Sort.PRICE_ASC)
                .build();
        System.out.println(query);

        SearchQuery minimal = SearchQuery.builder("usb cable").build();
        System.out.println(minimal);

        try {
            SearchQuery.builder("phone").pageSize(500).build();
        } catch (IllegalStateException e) {
            System.out.println("Rejected: " + e.getMessage());
        }

        // 2. GoF Builder: same construction process, two representations.
        var release = new ReleaseNotesDirector.Release("2.4.0",
                List.of("Export to JSON", "Dark mode"),
                List.of("Fix rounding in totals"));
        var director = new ReleaseNotesDirector();
        System.out.println(director.construct(release, new MarkdownBuilder()));
        System.out.print(director.construct(release, new HtmlBuilder()));
    }
}
