package hub.patterns.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// region grammar
/**
 * Grammar of a tiny feature-flag rule language:
 *   expr   := term ("or" term)*
 *   term   := factor ("and" factor)*
 *   factor := "not" factor | "(" expr ")" | NAME "=" VALUE | NAME "in" "[" VALUE ("," VALUE)* "]"
 * Each rule of the grammar becomes one class (record) with an interpret method.
 */
sealed interface Rule permits Or, And, Not, Equals, In {
    boolean interpret(Map<String, String> context);
}

record Or(Rule left, Rule right) implements Rule {
    public boolean interpret(Map<String, String> ctx) { return left.interpret(ctx) || right.interpret(ctx); }
}

record And(Rule left, Rule right) implements Rule {
    public boolean interpret(Map<String, String> ctx) { return left.interpret(ctx) && right.interpret(ctx); }
}

record Not(Rule inner) implements Rule {
    public boolean interpret(Map<String, String> ctx) { return !inner.interpret(ctx); }
}

record Equals(String name, String value) implements Rule {
    public boolean interpret(Map<String, String> ctx) { return value.equals(ctx.get(name)); }
}

record In(String name, Set<String> values) implements Rule {
    public boolean interpret(Map<String, String> ctx) { return values.contains(ctx.get(name)); }
}
// endregion

// region parser
/** Recursive-descent parser: builds the abstract syntax tree. Not part of GoF Interpreter, but always needed. */
final class RuleParser {
    private final List<String> tokens;
    private int pos;

    private RuleParser(String source) {
        this.tokens = new ArrayList<>();
        for (String t : source.replace("(", " ( ").replace(")", " ) ").replace("[", " [ ")
                .replace("]", " ] ").replace(",", " , ").replace("=", " = ").trim().split("\\s+")) {
            tokens.add(t);
        }
    }

    static Rule parse(String source) {
        RuleParser p = new RuleParser(source);
        Rule rule = p.expr();
        if (p.pos != p.tokens.size()) {
            throw new IllegalArgumentException("Unexpected token '" + p.tokens.get(p.pos) + "'");
        }
        return rule;
    }

    private Rule expr() {
        Rule left = term();
        while (accept("or")) { left = new Or(left, term()); }
        return left;
    }

    private Rule term() {
        Rule left = factor();
        while (accept("and")) { left = new And(left, factor()); }
        return left;
    }

    private Rule factor() {
        if (accept("not")) { return new Not(factor()); }
        if (accept("(")) { Rule r = expr(); expect(")"); return r; }
        String name = next();
        if (accept("=")) { return new Equals(name, next()); }
        expect("in");
        expect("[");
        Set<String> values = new java.util.LinkedHashSet<>();
        do { values.add(next()); } while (accept(","));
        expect("]");
        return new In(name, values);
    }

    private boolean accept(String t) {
        if (pos < tokens.size() && tokens.get(pos).equals(t)) { pos++; return true; }
        return false;
    }
    private void expect(String t) {
        if (!accept(t)) { throw new IllegalArgumentException("Expected '" + t + "' at token " + pos); }
    }
    private String next() {
        if (pos >= tokens.size()) { throw new IllegalArgumentException("Unexpected end of rule"); }
        return tokens.get(pos++);
    }
}
// endregion

public final class InterpreterDemo {

    public static void main(String[] args) {
        Rule rule = RuleParser.parse("plan = pro and not country in [US, CA] or beta = true");
        System.out.println("AST: " + rule);

        List<Map<String, String>> users = List.of(
                Map.of("user", "ana", "plan", "pro", "country", "DE", "beta", "false"),
                Map.of("user", "bob", "plan", "pro", "country", "US", "beta", "false"),
                Map.of("user", "cy", "plan", "free", "country", "US", "beta", "true"),
                Map.of("user", "dee", "plan", "free", "country", "FR", "beta", "false"));
        users.forEach(u -> System.out.printf("%-4s -> %s%n", u.get("user"), rule.interpret(u)));

        try {
            RuleParser.parse("plan = pro and");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid rule: " + e.getMessage());
        }
    }
}
