package hub.patterns.factorymethod;

import java.util.List;
import java.util.stream.Collectors;

/** Concrete product. Hand-written JSON keeps the example dependency-free. */
public final class JsonReportWriter implements ReportWriter {

    @Override
    public String fileExtension() {
        return "json";
    }

    @Override
    public String write(List<SalesRow> rows) {
        return rows.stream()
                .map(r -> "{\"region\":\"" + r.region() + "\",\"units\":" + r.units() + "}")
                .collect(Collectors.joining(",", "[", "]"));
    }
}
