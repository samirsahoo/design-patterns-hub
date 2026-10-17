package hub.patterns.factorymethod;

/** Concrete creator: overrides the factory method to produce CSV writers. */
public final class CsvReportExporter extends ReportExporter {

    @Override
    protected ReportWriter createWriter() {
        return new CsvReportWriter();
    }
}
