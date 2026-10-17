package hub.patterns.factorymethod;

/** Concrete creator: overrides the factory method to produce JSON writers. */
public final class JsonReportExporter extends ReportExporter {

    @Override
    protected ReportWriter createWriter() {
        return new JsonReportWriter();
    }
}
