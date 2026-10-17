# Content status

Tracks every topic in the curriculum. **Published** pages have `status: complete` in
their front matter. **Planned** topics appear on section pages as plain text (from the
section's `planned:` front matter) and in learning paths as steps without a `page:`.

When you publish an article:

1. Move its row here from *Planned* to *Published*.
2. Remove it from the `planned:` list in its section's `_index.md`.
3. Add `page:` to any learning-path step that covers it.

Last updated: 2026-10-17

## Published (batch 1)

| # | Article | Path | Code examples |
|---|---|---|---|
| 1 | What Are Design Patterns? | `/design-patterns/what-are-design-patterns` | — |
| 2 | SOLID Principles Explained | `/design-patterns/solid-principles` | inline |
| 3 | All 23 GoF Patterns: Overview and Classification | `/design-patterns/gof-overview` | — |
| 4 | Strategy Pattern | `/design-patterns/behavioral/strategy` | `hub.patterns.strategy` ✔ verified |
| 5 | Factory Method Pattern | `/design-patterns/creational/factory-method` | `hub.patterns.factorymethod` ✔ verified |
| 6 | Builder Pattern | `/design-patterns/creational/builder` | `hub.patterns.builder` ✔ verified |
| 7 | Adapter Pattern | `/design-patterns/structural/adapter` | `hub.patterns.adapter` ✔ verified |
| 8 | Dependency Injection in Java | `/spring/core/dependency-injection` | `hub.di` ✔ verified |
| 9 | Introduction to Software Architecture | `/software-architecture/introduction` | — |
| 10 | Layered versus Hexagonal Architecture | `/hexagonal-architecture/layered-vs-hexagonal` | Spring example ✔ |
| 11 | Hexagonal Architecture with Spring Boot | `/hexagonal-architecture/spring-boot-implementation` | `examples/spring-hexagonal-orders` ✔ 17 tests |
| 12 | Domain-Driven Design Fundamentals | `/domain-driven-design/fundamentals` | Spring example ✔ |
| 13 | What Is a Bounded Context? | `/domain-driven-design/strategic/bounded-context` | inline |
| 14 | Microservices versus Modular Monolith | `/microservices/fundamentals/microservices-vs-modular-monolith` | — |
| 15 | Introduction to System Design | `/system-design/introduction` | — |
| 16 | Designing an Order Management System | `/system-design/case-studies/order-management-system` | — |

Supporting pages: Diagram Building Blocks (`/system-design/diagram-patterns`), Adapter vs
Decorator vs Proxy vs Facade, Factory Patterns Compared, Glossary, About, six learning
paths.

## Suggested next batches

Batches are chosen so that each one completes a learning-path stretch.

- **Batch 2 — Creational and structural patterns:** Singleton (thread safety, enum
  singleton, why global state hurts testing), Abstract Factory, Prototype, Decorator,
  Proxy, Facade, Composite. Comparison: Strategy vs State vs Template Method.
- **Batch 3 — Remaining patterns:** Bridge, Flyweight, Chain of Responsibility,
  Command, Interpreter, Iterator, Mediator, Memento, Observer, State, Template Method,
  Visitor.
- **Batch 4 — Java fundamentals path:** JVM/JRE/JDK, collections, generics,
  exceptions, records and enums, concurrency basics, virtual threads.
- **Batch 5 — Spring core and data:** ApplicationContext and bean lifecycle, AOP and
  proxies, transactions and propagation, N+1, Spring Security basics.
- **Batch 6 — Microservices reliability and data:** timeouts and retries, circuit
  breaker, idempotency, saga, transactional outbox, CQRS.
- **Batch 7 — System design case studies:** URL shortener, rate limiter, notification
  service.

## Planned (by section)

The authoritative lists are the `planned:` entries in each section's `_index.md`:

| Section | File |
|---|---|
| Creational patterns | `content/design-patterns/creational/_index.md` |
| Structural patterns | `content/design-patterns/structural/_index.md` |
| Behavioral patterns | `content/design-patterns/behavioral/_index.md` |
| Design patterns (general) | `content/design-patterns/_index.md` |
| Java fundamentals / advanced | `content/java/fundamentals/_index.md`, `content/java/advanced/_index.md` |
| Spring (core, web, data, security, production, patterns) | `content/spring/*/_index.md` |
| Software architecture | `content/software-architecture/_index.md` |
| Hexagonal architecture | `content/hexagonal-architecture/_index.md` |
| DDD (strategic, tactical, advanced) | `content/domain-driven-design/*/_index.md` |
| Microservices (all subsections) | `content/microservices/*/_index.md` |
| System design case studies | `content/system-design/case-studies/_index.md` |
| Comparisons | `content/comparisons/_index.md` |
