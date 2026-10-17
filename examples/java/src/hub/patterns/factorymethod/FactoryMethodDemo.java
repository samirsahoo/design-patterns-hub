package hub.patterns.factorymethod;

import java.util.List;

public final class FactoryMethodDemo {

    public static void main(String[] args) {
        List<SalesRow> rows = List.of(new SalesRow("EMEA", 120), new SalesRow("APAC", 75));

        // GoF Factory Method: the client picks a creator; the creator picks the product.
        List<ReportExporter> exporters = List.of(new CsvReportExporter(), new JsonReportExporter());
        for (ReportExporter exporter : exporters) {
            ReportExporter.ExportedFile file = exporter.export("q3-sales", rows);
            System.out.println("== " + file.name());
            System.out.println(file.content());
        }

        // Simple factory: selection by a runtime value, no subclassing involved.
        ReportWriter writer = ReportWriters.forFormat("CSV");
        System.out.println("Simple factory chose: " + writer.getClass().getSimpleName());
    }
}
