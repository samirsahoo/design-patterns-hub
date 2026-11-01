---
title: "Modern Java: Records, Sealed Classes and Pattern Matching"
linkTitle: "Records, sealed & patterns"
description: "Data-oriented programming in modern Java: records, sealed classes and interfaces, pattern matching for instanceof and switch, record patterns, exhaustiveness, unnamed variables, text blocks and var, plus what Java 22–27 added (flexible constructor bodies, module imports, compact source files, primitive patterns in preview)."
weight: 13
difficulty: intermediate
status: complete
javaVersion: 21
tags: [java, records, sealed-classes, pattern-matching, modern-java]
keywords: [java records, sealed classes java, pattern matching switch java 21, record patterns, data oriented programming java, unnamed variables java 22, flexible constructor bodies java 25, module import declarations, compact source files, java 25 lts features, java 27 features]
lastReviewed: 2026-11-01
related:
  - /java/fundamentals/collections-framework
  - /design-patterns/behavioral/strategy
---

## Simple definition

Modern Java (16 to 21, refined through 25 LTS and 27) adds features for modelling **data as data**:

- **records** for transparent, immutable data carriers
- **sealed types** for closed sets of alternatives
- **pattern matching** to safely take data apart and branch on its shape.

Together these are called **data-oriented programming**.

## The problem

Classic Java needed about 50 lines for a simple value class (fields, constructor, getters, `equals`, `hashCode`, `toString`). Hierarchies of alternatives were open to anyone, so `if (x instanceof A) ... else if (x instanceof B)` chains could never be checked for completeness. Adding a new case silently fell through.

## Records (Java 16)

```java
public record Money(BigDecimal amount, Currency currency) {
    public Money {                                         // compact canonical constructor: validate
        Objects.requireNonNull(currency);
        if (amount.signum() < 0) throw new IllegalArgumentException("negative amount");
        amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
    }
    public Money plus(Money other) { return new Money(amount.add(other.amount), currency); }
}
```

- Fields are `private final`. Accessors are `amount()`, not `getAmount()`. `equals`, `hashCode` and `toString` are generated.
- Records can implement interfaces and have static members and methods. They **cannot extend** classes, because they implicitly extend `Record`.
- Records are **shallowly immutable**, so copy mutable components (`List.copyOf`).
- They suit DTOs, value objects, events, map keys, multiple return values and Spring `@ConfigurationProperties`. They do **not** suit JPA entities, which need mutability and proxies.

## Sealed classes and interfaces (Java 17)

```java
public sealed interface PaymentResult permits Approved, Declined, PendingReview {}
public record Approved(String authCode) implements PaymentResult {}
public record Declined(String reason, boolean retryable) implements PaymentResult {}
public record PendingReview(Duration expectedWait) implements PaymentResult {}
```

- Only the listed types may implement or extend the sealed type. Subtypes must be `final`, `sealed` or `non-sealed`.
- The compiler **knows all cases**, which enables exhaustive `switch`.
- Use them to model domain alternatives (results, commands, states) instead of enums with payloads or class hierarchies open to anyone.

## Pattern matching

### For instanceof (Java 16)

```java
if (obj instanceof Money m && m.amount().signum() > 0) { ... }   // test + cast + bind
```

### For switch, with record patterns (Java 21)

```java
String describe(PaymentResult r) {
    return switch (r) {
        case Approved(var code)                       -> "Approved: " + code;
        case Declined(var reason, var retry) when retry -> "Declined, retry later: " + reason;
        case Declined(var reason, var _)              -> "Declined: " + reason;
        case PendingReview(var wait)                  -> "Under review for " + wait.toMinutes() + " min";
    };   // no default: the compiler checks exhaustiveness over the sealed hierarchy
}
```

- **Record patterns** deconstruct records, including nested ones.
- **Guards** (`when`) add conditions to a case.
- **Exhaustiveness:** if someone adds a new `PaymentResult` subtype, every such `switch` **fails to compile** until it is handled. This replaces the Visitor pattern in many designs.
- `case null` can be handled explicitly; otherwise a null selector throws `NullPointerException`.
- **Unnamed variables and patterns** `_` (final in **Java 22**, JEP 456) ignore components you do not need. On Java 21 use a named variable instead.

```mermaid {title="Data-oriented design: sealed types + records + exhaustive switch"}
flowchart LR
    S["sealed interface PaymentResult"] --> A["record Approved"]
    S --> D["record Declined"]
    S --> P["record PendingReview"]
    A & D & P --> SW["switch (result) { case … }<br/>compiler checks every case"]
```

## Other everyday modern features

| Feature | Since | Example |
|---|---|---|
| `var` local type inference | 10 | `var orders = new ArrayList<Order>();` |
| Switch expressions | 14 | `int days = switch (m) { case FEB -> 28; default -> 31; };` |
| Text blocks | 15 | `"""` multi-line SQL and JSON `"""` |
| Helpful NullPointerExceptions | 14 | Messages say *which* reference was null |
| `Stream.toList()` | 16 | Unmodifiable result |
| Sequenced collections | 21 | `list.getLast()`, `map.firstEntry()` |
| Virtual threads | 21 | See [virtual threads](/java/advanced/virtual-threads-and-structured-concurrency) |

## What came after Java 21

| Release | Finalised features relevant to everyday code |
|---|---|
| **Java 22** | Unnamed variables and patterns (`_`); launch multi-file source programs; Foreign Function & Memory API |
| **Java 23** | Markdown documentation comments (`///`) |
| **Java 24** | **Stream gatherers** (custom intermediate operations); class-file API; virtual threads no longer pin on `synchronized` (JEP 491) |
| **Java 25 (LTS, Sept 2025)** | **Flexible constructor bodies** (code before `super(...)`); **module import declarations** (`import module java.base;`); **compact source files and instance `main`** (`void main() { ... }`); **scoped values**; compact object headers |
| **Java 26 (Mar 2026)** | HTTP/3 for the HTTP client; warnings when deep reflection mutates `final` fields; continued previews |
| **Java 27 (Sept 2026)** | G1 the default GC in all environments; post-quantum hybrid key exchange for TLS 1.3; previews continue (for example primitive types in patterns, lazy constants, structured concurrency) |

```java
// Java 25: flexible constructor body — validate before calling super()
public class PositiveAmount extends Amount {
    public PositiveAmount(BigDecimal value) {
        if (value.signum() <= 0) throw new IllegalArgumentException("must be positive");
        super(value);
    }
}
```

> [!VERSION] Baseline
> This handbook's examples target **Java 21**. Java 25 is the current LTS, and Java 27 is the latest feature release as of November 2026. Preview features need `--enable-preview` and may change, so avoid them in production code.

## Common mistakes

> [!MISTAKE] Records as JPA entities
> Hibernate needs a no-arg constructor, mutability and proxies. Use records for DTOs and projections; use classes for entities.

> [!MISTAKE] `default` in switches over sealed types
> A `default` branch hides new cases from the compiler. Omit it so exhaustiveness checking protects you.

> [!MISTAKE] Assuming records are deeply immutable
> A record holding a `List` exposes a mutable list unless you copy it in the constructor.

## Quick check

**What does a record generate for you?**
Private final fields, a canonical constructor, accessors, and `equals`, `hashCode` and `toString`.

**What does `sealed` add?**
A closed, compiler-known set of permitted subtypes, which enables exhaustive pattern matching.

**What is a record pattern?**
A pattern that tests a type and deconstructs a record's components in one step, for example `case Declined(var reason, var retry)`.

**Why omit `default` in a switch over a sealed interface?**
So that adding a new subtype causes compile errors everywhere it is not handled.

**Name three finalised Java 25 features.**
Flexible constructor bodies, module import declarations, compact source files and instance main methods, and scoped values.

## Summary

- Records give concise, immutable data carriers; sealed types give closed alternatives.
- Pattern matching with record patterns and guards gives safe, exhaustive branching.
- Later releases add `_`, gatherers, flexible constructors, module imports and more.
- Model data with records + sealed types + switch; keep behaviour-heavy objects as classes.

## References

- OpenJDK, [JEP 395: Records](https://openjdk.org/jeps/395), [JEP 409: Sealed Classes](https://openjdk.org/jeps/409), [JEP 440: Record Patterns](https://openjdk.org/jeps/440), [JEP 441: Pattern Matching for switch](https://openjdk.org/jeps/441).
- OpenJDK, [JDK 25](https://openjdk.org/projects/jdk/25/), [JDK 26](https://openjdk.org/projects/jdk/26/) and [JDK 27](https://openjdk.org/projects/jdk/27/) feature lists.
- Brian Goetz, [Data-Oriented Programming in Java](https://www.infoq.com/articles/data-oriented-programming-java/).
- InfoQ, [Java 27 released](https://infoq.com/news/2026/09/java27-released).
