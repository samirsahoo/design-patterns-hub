# Content status

Tracks every topic in the curriculum. **Published** pages have `status: complete` in
their front matter. **Planned** topics appear on section pages as plain text (from the
section's `planned:` front matter) and in learning paths as steps without a `page:`.

When you publish an article:

1. Move its row here from *Planned* to *Published*.
2. Remove it from the `planned:` list in its section's `_index.md`.
3. Add `page:` to any learning-path step that covers it.

Last updated: 2026-11-01

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

## Published (batch 2, 2026-11-01): Java, Spring, Spring Boot, data, security, testing, Spring Cloud

Inline code examples; every article ends with a Quick check. 11 Archify diagrams under `diagrams/archify/`.

| # | Article | Path |
|---|---|---|
| 17 | Generics, Wildcards and Type Erasure | `/java/fundamentals/generics-and-type-erasure` |
| 18 | The Java Collections Framework: Internal Architecture | `/java/fundamentals/collections-framework` |
| 19 | Exception Handling and Try-With-Resources | `/java/fundamentals/exceptions-and-try-with-resources` |
| 20 | Reflection API and Dynamic Proxies | `/java/fundamentals/reflection-and-dynamic-proxies` |
| 21 | Modern Java: Records, Sealed Classes and Pattern Matching | `/java/fundamentals/modern-java-features` |
| 22 | The Java Concurrency API | `/java/advanced/concurrency-api` |
| 23 | Virtual Threads, Structured Concurrency and Scoped Values | `/java/advanced/virtual-threads-and-structured-concurrency` |
| 24 | The Java Memory Model and Happens-Before | `/java/advanced/java-memory-model` |
| 25 | Garbage Collection: G1, ZGC and Tuning Basics | `/java/advanced/garbage-collection` |
| 26 | Functional Programming: Lambdas, Method References and Streams | `/java/advanced/functional-programming-and-streams` |
| 27 | The Java Platform Module System | `/java/advanced/java-module-system` |
| 28 | ApplicationContext and BeanFactory | `/spring/core/applicationcontext-and-beanfactory` |
| 29 | Bean Lifecycle and Scopes | `/spring/core/bean-lifecycle-and-scopes` |
| 30 | Configuration, @Bean, Component Scanning and Profiles | `/spring/core/configuration-and-component-scanning` |
| 31 | Spring Expression Language | `/spring/core/spring-expression-language` |
| 32 | AOP, Advice Types and Proxies | `/spring/core/aop-and-proxies` |
| 33 | Application Events and Listeners | `/spring/core/application-events` |
| 34 | Auto-Configuration and @ConditionalOn* | `/spring/boot/auto-configuration-and-conditions` |
| 35 | Externalized Configuration and @ConfigurationProperties | `/spring/boot/externalized-configuration` |
| 36 | Actuator and Custom Endpoints | `/spring/boot/actuator-and-custom-endpoints` |
| 37 | Spring MVC Architecture | `/spring/web/spring-mvc-architecture` |
| 38 | RESTful API Design and Global Exception Handling | `/spring/web/rest-api-design-and-error-handling` |
| 39 | Spring WebFlux and Project Reactor | `/spring/web/webflux-and-reactive-programming` |
| 40 | Spring Data JPA and the Hibernate Lifecycle | `/spring/data/spring-data-jpa-and-hibernate` |
| 41 | Entity Mappings and Fetch Strategies | `/spring/data/entity-mappings-and-fetch-strategies` |
| 42 | JPQL, Criteria API and Specifications | `/spring/data/jpql-and-criteria-api` |
| 43 | Transactions: @Transactional, Propagation and Isolation | `/spring/data/transactions` |
| 44 | Auditing and Migrations (Flyway, Liquibase) | `/spring/data/auditing-and-migrations` |
| 45 | Spring Cache and Redis | `/spring/data/caching` |
| 46 | Spring Security Filter Chain | `/spring/security/security-filter-chain` |
| 47 | OAuth 2.0, OpenID Connect and JWT | `/spring/security/oauth2-oidc-and-jwt` |
| 48 | Method-Level Security | `/spring/security/method-level-security` |
| 49 | Unit Testing with JUnit and Mockito | `/spring/production/unit-testing-junit-mockito` |
| 50 | Integration Testing with Testcontainers | `/spring/production/integration-testing` |
| 51 | Service Discovery | `/microservices/fundamentals/service-discovery` |
| 52 | API Gateway with Spring Cloud Gateway | `/microservices/fundamentals/api-gateway` |
| 53 | Centralized Configuration | `/microservices/fundamentals/centralized-configuration` |
| 54 | Circuit Breakers and Resilience4j | `/microservices/reliability/circuit-breakers-and-resilience4j` |
| 55 | Observability with Micrometer and OpenTelemetry | `/microservices/operations/observability-micrometer-opentelemetry` |
| 56 | Event-Driven Microservices with Spring Cloud Stream | `/microservices/communication/event-driven-spring-cloud-stream` |

New sections: `/spring/boot` and `/self-review` (quick check, service review checklist, quick reference).

## Published (batch 3, 2026-11-01): all GoF patterns, concurrency design, data-intensive systems, system design building blocks and case studies

Sources studied for this batch: *Design Patterns* (GoF), *Head First Design Patterns*, Doug Lea's *Concurrent Programming in Java*, Kleppmann's *Designing Data-Intensive Applications*, the *ByteByteGo System Design Handbook* and handwritten system design notes. All text is original; the books are cited as references.

Pattern articles include runnable examples under `examples/java/src/hub/patterns/<name>/` with expected output (19 new demos, all verified by `scripts/verify_java.py`).

| # | Article | Path |
|---|---|---|
| 57 | Singleton | `/design-patterns/creational/singleton` |
| 58 | Abstract Factory | `/design-patterns/creational/abstract-factory` |
| 59 | Prototype | `/design-patterns/creational/prototype` |
| 60 | Bridge | `/design-patterns/structural/bridge` |
| 61 | Composite | `/design-patterns/structural/composite` |
| 62 | Decorator | `/design-patterns/structural/decorator` |
| 63 | Facade | `/design-patterns/structural/facade` |
| 64 | Flyweight | `/design-patterns/structural/flyweight` |
| 65 | Proxy | `/design-patterns/structural/proxy` |
| 66 | Chain of Responsibility | `/design-patterns/behavioral/chain-of-responsibility` |
| 67 | Command | `/design-patterns/behavioral/command` |
| 68 | Interpreter | `/design-patterns/behavioral/interpreter` |
| 69 | Iterator | `/design-patterns/behavioral/iterator` |
| 70 | Mediator | `/design-patterns/behavioral/mediator` |
| 71 | Memento | `/design-patterns/behavioral/memento` |
| 72 | Observer | `/design-patterns/behavioral/observer` |
| 73 | State | `/design-patterns/behavioral/state` |
| 74 | Template Method | `/design-patterns/behavioral/template-method` |
| 75 | Visitor | `/design-patterns/behavioral/visitor` |
| 76 | OO Design Principles Behind the Patterns | `/design-patterns/oo-design-principles` |
| 77 | Compound Patterns and MVC | `/design-patterns/compound-patterns-mvc` |
| 78 | Strategy vs State vs Template Method | `/comparisons/strategy-state-template-method` |
| 79 | Observer vs Mediator vs Event Bus | `/comparisons/observer-mediator-event-bus` |
| 80 | Designing Concurrent Objects | `/java/advanced/concurrent-object-design` |
| 81 | State-Dependent Actions | `/java/advanced/state-dependent-actions` |
| 82 | Concurrency Design Patterns | `/java/advanced/concurrency-design-patterns` |
| 83 | Reliability, Scalability and Maintainability | `/data-systems/reliability-scalability-maintainability` |
| 84 | Data Models and Query Languages | `/data-systems/data-models-and-query-languages` |
| 85 | Storage Engines | `/data-systems/storage-engines` |
| 86 | Encoding and Schema Evolution | `/data-systems/encoding-and-schema-evolution` |
| 87 | Replication | `/data-systems/replication` |
| 88 | Partitioning (Sharding) | `/data-systems/partitioning` |
| 89 | Transactions and Isolation Levels | `/data-systems/transactions-and-isolation` |
| 90 | The Trouble with Distributed Systems | `/data-systems/distributed-systems-faults` |
| 91 | Consistency and Consensus | `/data-systems/consistency-and-consensus` |
| 92 | Batch Processing | `/data-systems/batch-processing` |
| 93 | Stream Processing | `/data-systems/stream-processing` |
| 94 | Scaling from Zero to Millions of Users | `/system-design/building-blocks/scaling-from-zero-to-millions` |
| 95 | Load Balancing and Proxies | `/system-design/building-blocks/load-balancing` |
| 96 | Caching Strategies | `/system-design/building-blocks/caching-strategies` |
| 97 | Consistent Hashing | `/system-design/building-blocks/consistent-hashing` |
| 98 | CAP Theorem and PACELC | `/system-design/building-blocks/cap-theorem-and-pacelc` |
| 99 | Choosing a Database | `/system-design/building-blocks/choosing-a-database` |
| 100 | Unique ID Generation | `/system-design/building-blocks/unique-id-generation` |
| 101 | Message Queues and Delivery Semantics | `/system-design/building-blocks/message-queues-and-delivery-semantics` |
| 102 | Long Polling, WebSockets and SSE | `/system-design/building-blocks/real-time-communication` |
| 103 | Deployment Strategies | `/system-design/building-blocks/deployment-strategies` |
| 104 | HTTPS, Password Storage and API Request Signing | `/system-design/building-blocks/security-fundamentals` |
| 105 | Designing a URL Shortener | `/system-design/case-studies/url-shortener` |
| 106 | Designing a Rate Limiter | `/system-design/case-studies/rate-limiter` |
| 107 | Designing a Payment System | `/system-design/case-studies/payment-system` |
| 108 | Designing a Proximity Service | `/system-design/case-studies/proximity-service` |

New sections: `/data-systems` (Data-Intensive Systems) and `/system-design/building-blocks`.

## Suggested next batches

Batches are chosen so that each one completes a learning-path stretch.

- **Batch 4 — Java fundamentals path:** JVM/JRE/JDK, collections, generics,
  exceptions, records and enums, concurrency basics, virtual threads.
- **Batch 5 — Spring core and data:** ApplicationContext and bean lifecycle, AOP and
  proxies, transactions and propagation, N+1, Spring Security basics.
- **Batch 6 — Microservices reliability and data:** timeouts and retries, circuit
  breaker, idempotency, saga, transactional outbox, CQRS.
- **Next system design case studies:** notification service, file upload service,
  inventory management, flash sale system.

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
