package hub.patterns.factorymethod;

import java.util.List;

/**
 * Creator. export() contains the stable workflow; the one step that varies —
 * which ReportWriter to use — is deferred to subclasses via the factory method.
 */
public abstract class ReportExporter {

    /** The factory method. Subclasses decide which concrete product to create. */
    protected abstract ReportWriter createWriter();

    public final ExportedFile export(String baseName, List<SalesRow> rows) {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Nothing to export");
        }
        ReportWriter writer = createWriter();
        String fileName = baseName + "." + writer.fileExtension();
        return new ExportedFile(fileName, writer.write(rows));
    }

    public record ExportedFile(String name, String content) {
    }
}
