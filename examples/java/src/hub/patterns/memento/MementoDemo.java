package hub.patterns.memento;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

// region originator
/** Originator: creates mementos of its own state and restores from them. */
final class DrawingCanvas {
    /** Narrow interface: all that the caretaker may see of a memento. */
    interface Memento {
        String label();
    }

    /** Wide interface: private to the originator, so nobody else can read or forge the state. */
    private record Snapshot(String label, List<String> shapes, String strokeColour) implements Memento { }

    private List<String> shapes = List.of();
    private String strokeColour = "black";

    void draw(String shape) { shapes = append(shapes, shape); }
    void stroke(String colour) { strokeColour = colour; }

    Memento save(String label) {
        return new Snapshot(label, shapes, strokeColour);      // shapes is immutable: no copy needed
    }

    void restore(Memento memento) {
        if (!(memento instanceof Snapshot s)) {
            throw new IllegalArgumentException("Not a memento from this canvas");
        }
        shapes = s.shapes();
        strokeColour = s.strokeColour();
    }

    @Override public String toString() { return shapes + " stroke=" + strokeColour; }

    private static List<String> append(List<String> list, String item) {
        var copy = new java.util.ArrayList<>(list);
        copy.add(item);
        return List.copyOf(copy);
    }
}
// endregion

// region caretaker
/** Caretaker: stores mementos and decides when to restore. It never looks inside them. */
final class History {
    private final Deque<DrawingCanvas.Memento> checkpoints = new ArrayDeque<>();
    private final int limit;

    History(int limit) { this.limit = limit; }

    void push(DrawingCanvas.Memento m) {
        checkpoints.push(m);
        if (checkpoints.size() > limit) {
            checkpoints.removeLast();                // bound the memory used by snapshots
        }
    }

    DrawingCanvas.Memento pop() { return checkpoints.pop(); }
    List<String> labels() { return checkpoints.stream().map(DrawingCanvas.Memento::label).toList(); }
}
// endregion

public final class MementoDemo {

    public static void main(String[] args) {
        DrawingCanvas canvas = new DrawingCanvas();
        History history = new History(2);

        canvas.draw("circle");
        history.push(canvas.save("after circle"));
        canvas.draw("square");
        canvas.stroke("red");
        history.push(canvas.save("after red square"));
        canvas.draw("triangle");
        history.push(canvas.save("after triangle"));
        canvas.draw("scribble");

        System.out.println("Now:          " + canvas);
        System.out.println("Checkpoints:  " + history.labels() + " (oldest dropped, limit 2)");
        DrawingCanvas.Memento m = history.pop();
        canvas.restore(m);
        System.out.println("Restore '" + m.label() + "': " + canvas);
        m = history.pop();
        canvas.restore(m);
        System.out.println("Restore '" + m.label() + "': " + canvas);
    }
}
