package hub.patterns.factorymethod;

import java.util.List;

/** Product: the object the factory method creates. */
public interface ReportWriter {

    String fileExtension();

    String write(List<SalesRow> rows);
}
