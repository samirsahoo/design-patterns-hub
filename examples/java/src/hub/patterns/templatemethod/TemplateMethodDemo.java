package hub.patterns.templatemethod;

import java.util.ArrayList;
import java.util.List;

record ImportResult(String source, int imported, List<String> rejected) { }

// region template
/** Abstract class: the template method fixes the algorithm; subclasses fill in the steps. */
abstract class DataImporter<T> {

    /** The template method. final: subclasses cannot change the order of the steps. */
    public final ImportResult importFrom(String source, List<String> lines) {
        List<T> valid = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        for (String line : skipHeader() ? lines.subList(1, lines.size()) : lines) {
            try {
                T record = parse(line);                       // primitive operation (abstract)
                String problem = validate(record);            // hook with a default
                if (problem == null) { valid.add(record); } else { rejected.add(line + " -> " + problem); }
            } catch (RuntimeException e) {
                rejected.add(line + " -> unparseable");
            }
        }
        save(valid);                                          // primitive operation (abstract)
        afterImport(valid.size());                            // hook: does nothing by default
        return new ImportResult(source, valid.size(), rejected);
    }

    protected abstract T parse(String line);
    protected abstract void save(List<T> records);

    protected boolean skipHeader() { return true; }           // hook
    protected String validate(T record) { return null; }      // hook: null means valid
    protected void afterImport(int count) { }                 // hook
}
// endregion

// region concrete
record Customer(String email, String name) { }

final class CustomerCsvImporter extends DataImporter<Customer> {
    @Override protected Customer parse(String line) {
        String[] f = line.split(",");
        return new Customer(f[0].trim(), f[1].trim());
    }

    @Override protected String validate(Customer c) {
        return c.email().contains("@") ? null : "invalid email";
    }

    @Override protected void save(List<Customer> records) {
        System.out.println("  saving " + records.size() + " customers");
    }
}

record StockLevel(String sku, int quantity) { }

final class StockFeedImporter extends DataImporter<StockLevel> {
    @Override protected boolean skipHeader() { return false; }   // the feed has no header

    @Override protected StockLevel parse(String line) {
        String[] f = line.split("\\|");
        return new StockLevel(f[0], Integer.parseInt(f[1]));
    }

    @Override protected void save(List<StockLevel> records) {
        System.out.println("  upserting " + records.size() + " stock levels");
    }

    @Override protected void afterImport(int count) {
        System.out.println("  publishing StockUpdated event for " + count + " SKUs");
    }
}
// endregion

public final class TemplateMethodDemo {

    public static void main(String[] args) {
        System.out.println("Customers:");
        System.out.println(new CustomerCsvImporter().importFrom("customers.csv", List.of(
                "email,name", "ana@example.com, Ana", "not-an-email, Bob", "raj@example.com, Raj")));
        System.out.println("Stock feed:");
        System.out.println(new StockFeedImporter().importFrom("warehouse-feed", List.of(
                "SKU-1|10", "SKU-2|oops", "SKU-3|0")));
    }
}
