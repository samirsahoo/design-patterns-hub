---
title: "Java & Software Architecture Knowledge Hub"
description: "A handbook on Core Java, Spring Framework, Spring Boot, persistence, security, testing, Spring Cloud microservices, design patterns, software architecture, Domain-Driven Design and system design, with diagrams, runnable Java examples, trade-offs and quick checks."
featured:
  - /java/advanced/concurrency-api
  - /spring/core/applicationcontext-and-beanfactory
  - /spring/boot/auto-configuration-and-conditions
  - /spring/data/transactions
  - /spring/security/security-filter-chain
  - /microservices/reliability/circuit-breakers-and-resilience4j
  - /design-patterns/solid-principles
  - /hexagonal-architecture/layered-vs-hexagonal
  - /self-review/quick-check
---

This handbook explains *why* designs work, not only *what* they are called. Every article covers the problem, the core idea, diagrams and flows, a practical example, trade-offs, common mistakes, and a **quick check** (or interview questions) with answers. Key flows are interactive [Archify](https://github.com/tt-a1i/archify) diagrams you can zoom, explore through guided views and export.

**What's covered:**

- **[Core Java](/java):** generics and type erasure, collections internals, exceptions, reflection and proxies, records, sealed types and pattern matching, the concurrency API, virtual threads, the Java Memory Model, garbage collection, lambdas and streams, and modules.
- **[Spring](/spring):**
  - **Core:** IoC container, bean lifecycle and scopes, configuration, SpEL, AOP, events.
  - **Boot:** auto-configuration, externalized configuration, Actuator.
  - **Web:** MVC, REST and Problem Details, WebFlux.
  - **Data:** JPA and Hibernate, fetching, JPQL and Criteria, transactions, migrations, caching.
  - **Security:** the filter chain, OAuth2/OIDC/JWT, method security.
  - **Testing:** JUnit and Mockito, Testcontainers.
- **[Microservices](/microservices):** service discovery, API gateway, centralized configuration, Resilience4j, observability with Micrometer and OpenTelemetry, and event-driven services with Spring Cloud Stream, Kafka and RabbitMQ.
- **[Design patterns](/design-patterns), [software architecture](/software-architecture), [hexagonal architecture](/hexagonal-architecture), [DDD](/domain-driven-design) and [system design](/system-design).**
- **[Self Review & Quick Check](/self-review):** questions for every area, a service review checklist and a quick reference.

Other details:

- **Versions:** Java examples target **Java 21 LTS**, with Java 25 LTS and 27 features labelled. Spring examples target **Spring Boot 4.1** (Spring Framework 7). Code included from source files is compiled and run by CI on every build.
- **Honest trade-offs:** no pattern or architecture style is presented as universally best. Each article says when *not* to use what it describes.
- **Planned topics:** the handbook grows in batches, and section pages list the topics that are planned but not written yet.
