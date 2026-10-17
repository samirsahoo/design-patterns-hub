package hub.patterns.builder;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable value with one required field and several optional ones,
 * built with a fluent builder (the "Effective Java" style of Builder).
 */
public final class SearchQuery {

    public enum Sort { RELEVANCE, PRICE_ASC, PRICE_DESC, NEWEST }

    private final String text;            // required
    private final String category;        // optional
    private final BigDecimal minPrice;    // optional
    private final BigDecimal maxPrice;    // optional
    private final Sort sort;
    private final int pageSize;

    private SearchQuery(Builder b) {
        this.text = b.text;
        this.category = b.category;
        this.minPrice = b.minPrice;
        this.maxPrice = b.maxPrice;
        this.sort = b.sort;
        this.pageSize = b.pageSize;
    }

    public static Builder builder(String text) {
        return new Builder(text);
    }

    public String text() { return text; }
    public Optional<String> category() { return Optional.ofNullable(category); }
    public Optional<BigDecimal> minPrice() { return Optional.ofNullable(minPrice); }
    public Optional<BigDecimal> maxPrice() { return Optional.ofNullable(maxPrice); }
    public Sort sort() { return sort; }
    public int pageSize() { return pageSize; }

    @Override
    public String toString() {
        return "SearchQuery[text=" + text
                + category().map(c -> ", category=" + c).orElse("")
                + minPrice().map(p -> ", min=" + p).orElse("")
                + maxPrice().map(p -> ", max=" + p).orElse("")
                + ", sort=" + sort + ", pageSize=" + pageSize + "]";
    }

    public static final class Builder {
        private final String text;
        private String category;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Sort sort = Sort.RELEVANCE;   // sensible defaults live in one place
        private int pageSize = 20;

        private Builder(String text) {
            this.text = Objects.requireNonNull(text, "text");
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder priceBetween(BigDecimal min, BigDecimal max) {
            this.minPrice = min;
            this.maxPrice = max;
            return this;
        }

        public Builder sort(Sort sort) {
            this.sort = Objects.requireNonNull(sort, "sort");
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        /** Validates the combination of fields once, then creates the immutable object. */
        public SearchQuery build() {
            if (text.isBlank()) {
                throw new IllegalStateException("search text must not be blank");
            }
            if (pageSize < 1 || pageSize > 100) {
                throw new IllegalStateException("pageSize must be between 1 and 100");
            }
            if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
                throw new IllegalStateException("minPrice must not exceed maxPrice");
            }
            return new SearchQuery(this);
        }
    }
}
