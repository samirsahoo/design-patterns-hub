package hub.patterns.builder;

/**
 * GoF Builder interface: the construction steps for a document,
 * independent of the representation that is produced.
 */
public interface DocumentBuilder<T> {

    DocumentBuilder<T> title(String text);

    DocumentBuilder<T> section(String heading);

    DocumentBuilder<T> bullet(String text);

    /** Returns the finished product. */
    T build();
}
