package hub.patterns.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

// region receiver
/** Receiver: knows how to do the actual work. */
final class TextDocument {
    private final StringBuilder text = new StringBuilder();

    void insert(int position, String s) { text.insert(position, s); }
    String delete(int position, int length) {
        String removed = text.substring(position, position + length);
        text.delete(position, position + length);
        return removed;
    }
    int length() { return text.length(); }
    @Override public String toString() { return "\"" + text + "\""; }
}
// endregion

// region command
/** Command: a request turned into an object, so it can be stored, undone and replayed. */
interface Command {
    void execute();
    void undo();
    String describe();
}

final class InsertText implements Command {
    private final TextDocument doc;
    private final int position;
    private final String text;

    InsertText(TextDocument doc, int position, String text) {
        this.doc = doc; this.position = position; this.text = text;
    }

    @Override public void execute() { doc.insert(position, text); }
    @Override public void undo() { doc.delete(position, text.length()); }
    @Override public String describe() { return "insert '" + text + "' at " + position; }
}

final class DeleteText implements Command {
    private final TextDocument doc;
    private final int position;
    private final int length;
    private String removed = "";              // state captured on execute, needed for undo

    DeleteText(TextDocument doc, int position, int length) {
        this.doc = doc; this.position = position; this.length = length;
    }

    @Override public void execute() { removed = doc.delete(position, length); }
    @Override public void undo() { doc.insert(position, removed); }
    @Override public String describe() { return "delete " + length + " chars at " + position; }
}

/** Macro command: a composite of commands, undone in reverse order. */
record MacroCommand(String name, List<Command> steps) implements Command {
    @Override public void execute() { steps.forEach(Command::execute); }
    @Override public void undo() { steps.reversed().forEach(Command::undo); }
    @Override public String describe() { return "macro '" + name + "'"; }
}
// endregion

// region invoker
/** Invoker: executes commands and keeps the undo and redo stacks. It never touches the document. */
final class CommandHistory {
    private final Deque<Command> undo = new ArrayDeque<>();
    private final Deque<Command> redo = new ArrayDeque<>();

    void run(Command c) {
        c.execute();
        undo.push(c);
        redo.clear();                         // a new action invalidates the redo history
    }

    void undo() {
        if (!undo.isEmpty()) {
            Command c = undo.pop();
            c.undo();
            redo.push(c);
            System.out.println("  undo: " + c.describe());
        }
    }

    void redo() {
        if (!redo.isEmpty()) {
            Command c = redo.pop();
            c.execute();
            undo.push(c);
            System.out.println("  redo: " + c.describe());
        }
    }
}
// endregion

public final class CommandDemo {

    public static void main(String[] args) {
        TextDocument doc = new TextDocument();
        CommandHistory history = new CommandHistory();

        history.run(new InsertText(doc, 0, "Hello world"));
        history.run(new InsertText(doc, 5, ","));
        System.out.println("After edits:  " + doc);
        history.run(new DeleteText(doc, 6, 6));
        System.out.println("After delete: " + doc);

        history.undo();
        System.out.println("              " + doc);
        history.undo();
        System.out.println("              " + doc);
        history.redo();
        System.out.println("              " + doc);

        history.run(new MacroCommand("sign off", List.of(
                new InsertText(doc, doc.length(), " Regards,"),
                new InsertText(doc, doc.length() + " Regards,".length(), " Ana"))));
        System.out.println("After macro:  " + doc);
        history.undo();
        System.out.println("              " + doc);
    }
}
