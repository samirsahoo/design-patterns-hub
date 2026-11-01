---
title: "Functional Programming in Java: Lambdas, Method References and Streams"
linkTitle: "Lambdas & Streams"
description: "Functional Java explained: functional interfaces and the java.util.function catalogue, lambdas and capture rules, the four kinds of method references, the Streams API pipeline (lazy evaluation, intermediate and terminal operations), collectors, primitive streams, parallel streams, stream gatherers (Java 24), Optional done right, and performance pitfalls."
weight: 4
difficulty: intermediate
status: complete
javaVersion: 21
tags: [java, functional-programming, streams, lambdas]
keywords: [java lambdas, functional interfaces, method references, java streams api, stream collectors groupingby, flatmap, lazy evaluation streams, parallel streams, stream gatherers java 24, optional best practices, effectively final]
lastReviewed: 2026-11-01
prerequisites:
  - /java/fundamentals/collections-framework
related:
  - /design-patterns/behavioral/strategy
  - /java/advanced/concurrency-api
---

## Simple definition

**Functional programming in Java** means passing **behaviour as values**:

- **lambdas** and **method references** implement **functional interfaces** (interfaces with one abstract method)
- the **Streams API** processes data declaratively: you say **what** to compute (filter, map, group), not **how** to loop.

## The problem

Loops mix the "what" with the "how": index variables, temporary collections and mutable accumulators. Anonymous inner classes for callbacks were verbose. Lambdas and streams make data transformations shorter and clearer, and easier to parallelise **when** that helps.

## Functional interfaces

| Interface | Signature | Example |
|---|---|---|
| `Supplier<T>` | `() -> T` | `() -> new ArrayList<>()` |
| `Consumer<T>` | `T -> void` | `order -> log.info("{}", order)` |
| `Function<T,R>` | `T -> R` | `Order::total` |
| `Predicate<T>` | `T -> boolean` | `o -> o.total().compareTo(LIMIT) > 0` |
| `UnaryOperator<T>` / `BinaryOperator<T>` | `T -> T` / `(T,T) -> T` | `BigDecimal::add` |
| `BiFunction<T,U,R>` | `(T,U) -> R` | `(price, qty) -> price.multiply(qty)` |
| Primitive variants | `IntPredicate`, `ToLongFunction`… | Avoid boxing |

Annotate your own with `@FunctionalInterface`. Functional interfaces have **default methods** for composition: `predicate.and(...)`, `function.andThen(...)`, `Comparator.comparing(...).thenComparing(...)`.

## Lambdas

```java
Comparator<Order> byTotalDesc = (a, b) -> b.total().compareTo(a.total());
Runnable task = () -> process(batch);
```

- They can capture **effectively final** local variables only. They capture values, not variables.
- `this` inside a lambda refers to the enclosing instance (unlike in anonymous classes).
- Lambdas compile to `invokedynamic` and `LambdaMetafactory`, with no class file per lambda. Non-capturing lambdas are reused singletons.

## Method references

| Kind | Syntax | Equivalent lambda |
|---|---|---|
| Static method | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Bound instance method | `log::info`, `this::validate` | `x -> log.info(x)` |
| Unbound instance method | `Order::total` | `o -> o.total()` |
| Constructor | `ArrayList::new`, `Order[]::new` | `() -> new ArrayList<>()` |

## The stream pipeline

```mermaid {title="Source → lazy intermediate operations → terminal operation"}
flowchart LR
    SRC["Source<br/>list.stream()"] --> F["filter"] --> M["map"] --> S["sorted"] --> T["Terminal<br/>collect / toList / sum"]
    T -.->|"nothing runs until the terminal call"| SRC
```

```java
Map<Customer, BigDecimal> revenueByCustomer = orders.stream()
        .filter(o -> o.status() == Status.PAID)
        .collect(Collectors.groupingBy(Order::customer,
                 Collectors.reducing(BigDecimal.ZERO, Order::total, BigDecimal::add)));

List<String> topSkus = orders.stream()
        .flatMap(o -> o.lines().stream())                         // one-to-many
        .collect(Collectors.groupingBy(Line::sku, Collectors.summingInt(Line::qty)))
        .entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .limit(10)
        .map(Map.Entry::getKey)
        .toList();
```

**Key properties:**

- **Lazy:** intermediate operations build a pipeline. Elements flow one at a time through `filter → map → …` when a terminal operation runs. **Short-circuiting** operations (`findFirst`, `anyMatch`, `limit`) stop early.
- **Single use:** a stream can be consumed once.
- **Stateless vs stateful operations:** `sorted` and `distinct` must see all elements, which costs memory.
- **Non-interference:** do not modify the source during the pipeline, and keep lambdas free of side effects.

### Useful collectors

`toList()`, `toSet()`, `toMap(k, v, merge)`, `groupingBy(classifier, downstream)`, `partitioningBy(predicate)`, `counting()`, `summingX`, `averagingX`, `joining(", ")`, `teeing(c1, c2, merger)` (Java 12), `mapping`, `filtering` and `flatMapping` as downstream collectors.

### Primitive streams

`IntStream`, `LongStream` and `DoubleStream` avoid boxing and add `sum()`, `average()`, `range()` and `summaryStatistics()`. Use `mapToInt` and similar.

### Stream gatherers (Java 24)

**Gatherers** (JEP 485) are custom **intermediate** operations, just as collectors are custom terminal operations. Built-ins include `windowFixed`, `windowSliding`, `fold`, `scan` and `mapConcurrent`:

```java
List<List<Reading>> batches = readings.stream().gather(Gatherers.windowFixed(100)).toList();
```

`mapConcurrent(n, fn)` runs a mapping on virtual threads with bounded concurrency, which is handy for I/O fan-out.

## Parallel streams

`parallelStream()` splits work across the **common ForkJoinPool**. It helps only when:

- the data is **large** (tens of thousands of elements or more) and **splits well** (arrays, `ArrayList`, ranges; not `LinkedList` or I/O sources)
- per-element work is **CPU-bound** and **independent**
- there is no blocking I/O and no shared mutable state.

**Measure** before using it. In servers, parallel streams compete with request threads for cores.

## Optional

`Optional<T>` is a **return type** meaning "maybe no result":

```java
return repository.findById(id)
        .map(OrderDto::from)
        .orElseThrow(() -> new OrderNotFoundException(id));
```

**Do:** return it from lookups, then chain `map`, `filter`, `orElse`, `orElseGet` and `orElseThrow`.

**Don't:** use it for fields, method parameters or collections (return an empty list instead); call `get()` without checking; or use `orElse(expensiveCall())`, which always evaluates. Use `orElseGet`.

## Performance notes

- Streams have small overhead. For tiny collections in very hot loops, a plain loop can be faster, but readability usually wins.
- Avoid boxing in numeric pipelines (use primitive streams).
- Put `filter` before `map` and `sorted`, so less data is processed.

## Common mistakes

> [!MISTAKE] Side effects in stream lambdas
> Adding to an external list inside `forEach` or `map` breaks under parallelism and hides intent. Use collectors.

> [!MISTAKE] `toMap` with duplicate keys
> It throws `IllegalStateException`. Pass a merge function: `toMap(k, v, (a, b) -> a)`.

> [!MISTAKE] Parallel streams by default
> They add overhead and contention, and are rarely faster for small or I/O-bound work.

## Quick check

**What makes an interface functional?**
Exactly one abstract method, so a lambda or method reference can implement it.

**Why are streams lazy, and why does it matter?**
Intermediate operations run only when a terminal operation is invoked. That enables short-circuiting and single-pass fusion of operations.

**Name the four kinds of method references.**
Static, bound instance, unbound instance (on an arbitrary object of a type), and constructor references.

**When does a parallel stream help?**
Large, well-splitting sources with independent, CPU-bound per-element work and no blocking or shared mutable state.

**What are stream gatherers?**
Custom intermediate operations (Java 24), such as sliding windows, scans and concurrent mapping, the counterpart of collectors.

**`orElse` vs `orElseGet`?**
`orElse` always evaluates its argument. `orElseGet` calls the supplier only when the Optional is empty.

## Summary

- Lambdas and method references implement functional interfaces; capture only effectively final values.
- Streams are lazy, single-use, declarative pipelines; collectors and gatherers extend them.
- Use parallel streams only after measuring; keep lambdas side-effect free.
- Use Optional as a return type, not everywhere.

## References

- Oracle, [`java.util.stream` package summary](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/package-summary.html).
- OpenJDK, [JEP 485: Stream Gatherers](https://openjdk.org/jeps/485).
- Brian Goetz, [State of the Lambda](https://cr.openjdk.org/~briangoetz/lambda/lambda-state-final.html).
- Stuart Marks, [Optional: the mother of all bikesheds](https://stuartmarks.wordpress.com/2016/09/27/vjug24-session-on-optional/).
