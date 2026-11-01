package hub.patterns.visitor;

import java.util.List;
import java.util.Locale;

// region classic
/** Element hierarchy with accept(): the classic GoF form, based on double dispatch. */
interface Shape {
    <R> R accept(ShapeVisitor<R> visitor);
}

interface ShapeVisitor<R> {
    R visitCircle(Circle c);
    R visitRectangle(Rectangle r);
    R visitGroup(Group g);
}

record Circle(double x, double y, double radius) implements Shape {
    public <R> R accept(ShapeVisitor<R> v) { return v.visitCircle(this); }
}

record Rectangle(double x, double y, double width, double height) implements Shape {
    public <R> R accept(ShapeVisitor<R> v) { return v.visitRectangle(this); }
}

record Group(List<Shape> children) implements Shape {
    public <R> R accept(ShapeVisitor<R> v) { return v.visitGroup(this); }
}
// endregion

// region visitors
/** A new operation is a new visitor class; the shapes do not change. */
final class AreaVisitor implements ShapeVisitor<Double> {
    @Override public Double visitCircle(Circle c) { return Math.PI * c.radius() * c.radius(); }
    @Override public Double visitRectangle(Rectangle r) { return r.width() * r.height(); }
    @Override public Double visitGroup(Group g) {
        return g.children().stream().mapToDouble(s -> s.accept(this)).sum();
    }
}

final class SvgExportVisitor implements ShapeVisitor<String> {
    @Override public String visitCircle(Circle c) {
        return String.format(Locale.ROOT, "<circle cx=\"%.0f\" cy=\"%.0f\" r=\"%.0f\"/>", c.x(), c.y(), c.radius());
    }
    @Override public String visitRectangle(Rectangle r) {
        return String.format(Locale.ROOT, "<rect x=\"%.0f\" y=\"%.0f\" width=\"%.0f\" height=\"%.0f\"/>",
                r.x(), r.y(), r.width(), r.height());
    }
    @Override public String visitGroup(Group g) {
        return "<g>" + String.join("", g.children().stream().map(s -> s.accept(this)).toList()) + "</g>";
    }
}
// endregion

// region modern
/** Java 21 alternative: a sealed hierarchy plus an exhaustive switch, no accept() needed. */
sealed interface Node permits Num, Add, Mul { }
record Num(double value) implements Node { }
record Add(Node left, Node right) implements Node { }
record Mul(Node left, Node right) implements Node { }

final class NodeOperations {
    static double evaluate(Node n) {
        return switch (n) {                  // the compiler checks that every Node type is handled
            case Num num -> num.value();
            case Add(Node l, Node r) -> evaluate(l) + evaluate(r);
            case Mul(Node l, Node r) -> evaluate(l) * evaluate(r);
        };
    }

    static String print(Node n) {
        return switch (n) {
            case Num num -> String.valueOf(num.value());
            case Add(Node l, Node r) -> "(" + print(l) + " + " + print(r) + ")";
            case Mul(Node l, Node r) -> print(l) + " * " + print(r);
        };
    }
}
// endregion

public final class VisitorDemo {

    public static void main(String[] args) {
        Shape drawing = new Group(List.of(
                new Circle(10, 10, 5),
                new Rectangle(0, 0, 4, 3),
                new Group(List.of(new Rectangle(20, 20, 2, 2)))));

        System.out.printf(Locale.ROOT, "Total area: %.2f%n", drawing.accept(new AreaVisitor()));
        System.out.println("SVG: " + drawing.accept(new SvgExportVisitor()));

        Node expr = new Mul(new Add(new Num(2), new Num(3)), new Num(4));
        System.out.println(NodeOperations.print(expr) + " = " + NodeOperations.evaluate(expr));
    }
}
