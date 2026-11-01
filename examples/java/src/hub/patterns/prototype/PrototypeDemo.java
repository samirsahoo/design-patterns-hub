package hub.patterns.prototype;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

// region prototype
/** A prototype that copies itself with a copy constructor (preferred in Java over Cloneable). */
final class ReportTemplate {
    private String title;
    private final List<String> sections;          // mutable state: must be deep-copied
    private final Map<String, String> styles;     // mutable state: must be deep-copied

    ReportTemplate(String title, List<String> sections, Map<String, String> styles) {
        this.title = title;
        this.sections = new ArrayList<>(sections);
        this.styles = new LinkedHashMap<>(styles);
    }

    /** Copy constructor: every mutable field gets its own new container. */
    private ReportTemplate(ReportTemplate source) {
        this(source.title, source.sections, source.styles);
    }

    ReportTemplate copy() {
        return new ReportTemplate(this);
    }

    ReportTemplate titled(String newTitle) { this.title = newTitle; return this; }
    ReportTemplate addSection(String section) { sections.add(section); return this; }
    ReportTemplate style(String key, String value) { styles.put(key, value); return this; }

    @Override public String toString() {
        return title + " " + sections + " " + styles;
    }
}
// endregion

// region shallow
/** What goes wrong with a shallow copy: both objects share the same list. */
final class ShallowTemplate {
    final List<String> sections;

    ShallowTemplate(List<String> sections) {
        this.sections = sections;
    }

    ShallowTemplate copy() {
        return new ShallowTemplate(sections);      // BUG: the list is shared, not copied
    }
}
// endregion

// region registry
/** Prototype registry: preconfigured prototypes looked up by name, always handed out as copies. */
final class TemplateRegistry {
    private final Map<String, ReportTemplate> prototypes = new LinkedHashMap<>();

    void register(String name, ReportTemplate prototype) {
        prototypes.put(name, prototype.copy());
    }

    ReportTemplate create(String name, Function<ReportTemplate, ReportTemplate> customise) {
        ReportTemplate prototype = prototypes.get(name);
        if (prototype == null) {
            throw new IllegalArgumentException("No prototype named " + name);
        }
        return customise.apply(prototype.copy());
    }
}
// endregion

public final class PrototypeDemo {

    public static void main(String[] args) {
        TemplateRegistry registry = new TemplateRegistry();
        registry.register("monthly-sales", new ReportTemplate("Monthly sales",
                List.of("Summary", "By region"), Map.of("font", "Inter")));

        ReportTemplate march = registry.create("monthly-sales",
                t -> t.titled("Sales - March").addSection("Returns").style("accent", "teal"));
        ReportTemplate april = registry.create("monthly-sales", t -> t.titled("Sales - April"));

        System.out.println("March: " + march);
        System.out.println("April: " + april);
        System.out.println("Prototype untouched: " + registry.create("monthly-sales", t -> t));

        ShallowTemplate original = new ShallowTemplate(new ArrayList<>(List.of("Summary")));
        ShallowTemplate shallowCopy = original.copy();
        shallowCopy.sections.add("Oops");
        System.out.println("Shallow copy changed the original: " + original.sections);
    }
}
