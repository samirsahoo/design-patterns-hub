package hub.patterns.chain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

record ExpenseClaim(String employee, BigDecimal amount, String purpose) { }

// region classic
/** Classic GoF handler: each approver either handles the claim or passes it to its successor. */
abstract class Approver {
    private Approver next;

    Approver then(Approver next) {
        this.next = next;
        return next;                  // allows chain.then(a).then(b) wiring
    }

    final String handle(ExpenseClaim claim) {
        if (canApprove(claim)) {
            return getClass().getSimpleName() + " approved " + claim.amount();
        }
        if (next == null) {
            return "Rejected: nobody may approve " + claim.amount();
        }
        return next.handle(claim);
    }

    protected abstract boolean canApprove(ExpenseClaim claim);
}

final class TeamLead extends Approver {
    @Override protected boolean canApprove(ExpenseClaim c) { return c.amount().compareTo(new BigDecimal("500")) <= 0; }
}

final class Director extends Approver {
    @Override protected boolean canApprove(ExpenseClaim c) { return c.amount().compareTo(new BigDecimal("5000")) <= 0; }
}

final class Cfo extends Approver {
    @Override protected boolean canApprove(ExpenseClaim c) {
        return c.amount().compareTo(new BigDecimal("50000")) <= 0 && !c.purpose().contains("personal");
    }
}
// endregion

// region pipeline
/** Modern variant: a list of handlers; the first one that returns a result wins. */
interface ClaimRule {
    Optional<String> evaluate(ExpenseClaim claim);
}

final class ClaimPipeline {
    private final List<ClaimRule> rules;

    ClaimPipeline(List<ClaimRule> rules) {
        this.rules = List.copyOf(rules);
    }

    String process(ExpenseClaim claim) {
        return rules.stream()
                .map(rule -> rule.evaluate(claim))
                .flatMap(Optional::stream)
                .findFirst()
                .orElse("Escalated to manual review");
    }
}
// endregion

public final class ChainOfResponsibilityDemo {

    public static void main(String[] args) {
        Approver chain = new TeamLead();
        chain.then(new Director()).then(new Cfo());

        List<ExpenseClaim> claims = List.of(
                new ExpenseClaim("ana", new BigDecimal("120"), "team lunch"),
                new ExpenseClaim("raj", new BigDecimal("3200"), "conference"),
                new ExpenseClaim("li", new BigDecimal("18000"), "servers"),
                new ExpenseClaim("sam", new BigDecimal("20000"), "personal car"));
        claims.forEach(c -> System.out.printf("%-4s %6s -> %s%n", c.employee(), c.amount(), chain.handle(c)));

        ClaimPipeline pipeline = new ClaimPipeline(List.of(
                c -> c.amount().signum() <= 0 ? Optional.of("Rejected: amount must be positive") : Optional.empty(),
                c -> c.purpose().isBlank() ? Optional.of("Rejected: purpose required") : Optional.empty(),
                c -> c.amount().compareTo(new BigDecimal("100")) <= 0 ? Optional.of("Auto-approved") : Optional.empty()));
        System.out.println("Pipeline: " + pipeline.process(new ExpenseClaim("ana", new BigDecimal("45"), "taxi")));
        System.out.println("Pipeline: " + pipeline.process(new ExpenseClaim("raj", new BigDecimal("-3"), "refund")));
        System.out.println("Pipeline: " + pipeline.process(new ExpenseClaim("li", new BigDecimal("900"), "monitor")));
    }
}
