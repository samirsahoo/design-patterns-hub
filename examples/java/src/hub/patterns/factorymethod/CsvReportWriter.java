package hub.patterns.factorymethod;

import java.util.List;
import java.util.stream.Collectors;

/** Concrete product. */
public final class CsvReportWriter implements ReportWriter {

    @Override
    public String fileExtension() {
        return "csv";
    }

    @Override
    public String write(List<SalesRow> rows) {
        return rows.stream()
                .map(r -> r.region() + "," + r.units())
                .collect(Collectors.joining("\n", "region,units\n", ""));
    }
}
