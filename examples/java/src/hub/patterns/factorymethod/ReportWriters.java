package hub.patterns.factorymethod;

import java.util.Locale;

/**
 * NOT the GoF Factory Method: this is a "simple factory" — one static method
 * that selects a class with a conditional. Useful, common, and different.
 */
public final class ReportWriters {

    private ReportWriters() {
    }

    public static ReportWriter forFormat(String format) {
        return switch (format.toLowerCase(Locale.ROOT)) {
            case "csv" -> new CsvReportWriter();
            case "json" -> new JsonReportWriter();
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }
}
