---
title: "Quick Check: Java, Spring and Microservices"
linkTitle: "Quick check"
description: "Over 90 short self-review questions with answers across Core Java (generics, collections, concurrency, JMM, GC, streams, modules, modern features), Spring Core, Spring Boot and web, data and transactions, security and testing, and Spring Cloud microservices."
weight: 1
difficulty: beginner
status: complete
tags: [self-review]
keywords: [java quick check, spring boot self assessment, spring questions answers, java concurrency questions, jpa transactions questions, spring security questions, microservices spring cloud questions]
lastReviewed: 2026-11-01
related:
  - /self-review/service-review-checklist
  - /self-review/quick-reference
---

Answers are short, just enough to check your understanding. Each section links to the full articles.

> [!TIP] How to use this page
> Cover the answer, explain it in your own words, then compare. Mark anything you hesitated on and reread the linked article. Aim for 80% before moving on.

## 1. Core Java language

Read: [Generics](/java/fundamentals/generics-and-type-erasure), [Collections](/java/fundamentals/collections-framework), [Exceptions](/java/fundamentals/exceptions-and-try-with-resources), [Reflection](/java/fundamentals/reflection-and-dynamic-proxies), [Modern Java](/java/fundamentals/modern-java-features).

**What is type erasure?**
The compiler checks generic types, then removes them from the bytecode, replacing them with bounds and inserting casts.

**What does PECS mean?**
Producer `extends`, consumer `super`: use `? extends T` to read and `? super T` to write.

**How does HashMap choose a bucket?**
It spreads `hashCode()` and computes `hash & (n - 1)` on a power-of-two table.

**When does a HashMap bucket become a tree?**
At 8 entries in one bucket, if the table has at least 64 buckets.

**What did Java 21 add to collections?**
Sequenced collections: `getFirst`, `getLast` and `reversed` on ordered collections and maps.

**Checked vs unchecked exceptions?**
Checked exceptions must be caught or declared; unchecked (`RuntimeException`) need not be.

**What does try-with-resources do with a failing `close()`?**
It closes resources in reverse order and adds close failures as suppressed exceptions.

**JDK proxy vs CGLIB?**
JDK proxies implement interfaces; CGLIB subclasses classes, so it cannot intercept `final` methods.

**What do records generate?**
Final fields, a canonical constructor, accessors, `equals`, `hashCode` and `toString`.

**Why omit `default` when switching over a sealed type?**
So the compiler reports unhandled new subtypes (exhaustiveness).

**Name three finalised Java 25 features.**
Flexible constructor bodies, module imports, compact source files, scoped values.

## 2. Concurrency, memory and performance

Read: [Concurrency API](/java/advanced/concurrency-api), [Virtual threads](/java/advanced/virtual-threads-and-structured-concurrency), [JMM](/java/advanced/java-memory-model), [GC](/java/advanced/garbage-collection), [Streams](/java/advanced/functional-programming-and-streams), [JPMS](/java/advanced/java-module-system).

**Why bound thread pools and queues?**
To get backpressure instead of unbounded memory growth under overload.

**`thenApply` vs `thenCompose`?**
`thenApply` maps the result; `thenCompose` flat-maps to another future.

**When choose ReentrantLock over synchronized?**
When you need `tryLock` with timeouts, interruptible locking, fairness or several conditions.

**Safe "get or create" in a ConcurrentHashMap?**
`computeIfAbsent`.

**What do virtual threads improve?**
Throughput of blocking, I/O-bound concurrency. They do not speed up CPU-bound work.

**What did JEP 491 (Java 24) fix?**
Virtual threads no longer pin carriers inside `synchronized`.

**What does `volatile` guarantee?**
Visibility and ordering (happens-before), not atomicity.

**Why is double-checked locking broken without volatile?**
Another thread may see a partially constructed object.

**G1 vs ZGC?**
G1 is the balanced default with a pause goal; ZGC gives sub-millisecond pauses on large heaps.

**How should heap be sized in containers?**
With `MaxRAMPercentage` (around 75%), leaving room for native memory.

**Why are streams lazy?**
Intermediate operations run only when a terminal operation is called, which enables short-circuiting.

**`orElse` vs `orElseGet`?**
`orElse` always evaluates its argument; `orElseGet` evaluates it only when empty.

**`exports` vs `opens` in JPMS?**
`exports` gives compile and runtime access to public types; `opens` gives runtime deep reflection.

## 3. Spring Core

Read: [ApplicationContext](/spring/core/applicationcontext-and-beanfactory), [Lifecycle & scopes](/spring/core/bean-lifecycle-and-scopes), [Configuration](/spring/core/configuration-and-component-scanning), [SpEL](/spring/core/spring-expression-language), [AOP](/spring/core/aop-and-proxies), [Events](/spring/core/application-events).

**BeanFactory vs ApplicationContext?**
The core DI engine vs the full container (eager singletons, events, post-processors, environment).

**BeanFactoryPostProcessor vs BeanPostProcessor?**
The first edits definitions before creation; the second processes instances (proxies).

**When are AOP proxies created?**
In `postProcessAfterInitialization`.

**What is the default scope, and the rule that follows?**
Singleton, so keep beans stateless and thread-safe.

**How do you use a request-scoped bean from a singleton?**
Through a scoped proxy or `ObjectProvider`.

**What does `proxyBeanMethods = false` change?**
No CGLIB proxy, so inter-`@Bean` calls create new instances. Use parameters instead.

**`${}` vs `#{}`?**
A property placeholder vs a SpEL expression.

**Which advice can skip or retry a call?**
`@Around`.

**Why doesn't self-invocation trigger `@Transactional`?**
The call bypasses the proxy.

**Are Spring events synchronous by default?**
Yes: same thread, same transaction.

**What does `@TransactionalEventListener` default to?**
AFTER_COMMIT.

## 4. Spring Boot and web

Read: [Auto-configuration](/spring/boot/auto-configuration-and-conditions), [Externalized config](/spring/boot/externalized-configuration), [Actuator](/spring/boot/actuator-and-custom-endpoints), [MVC](/spring/web/spring-mvc-architecture), [REST & errors](/spring/web/rest-api-design-and-error-handling), [WebFlux](/spring/web/webflux-and-reactive-programming).

**Where are auto-configuration candidates listed?**
In `META-INF/spring/…AutoConfiguration.imports`.

**Why does your bean replace Boot's default?**
`@ConditionalOnMissingBean`, and user configuration is processed first.

**How do you see why an auto-configuration did not apply?**
`--debug` (the conditions report) or `/actuator/conditions`.

**Environment variable vs `application.yml` in the JAR: which wins?**
The environment variable.

**Why `@ConfigurationProperties` over `@Value`?**
Type-safe, grouped, validated, relaxed binding, IDE metadata.

**Liveness vs readiness?**
Restart-worthy own state vs ability to serve now. Dependencies go in readiness.

**HandlerMapping vs HandlerAdapter?**
The mapping finds the handler; the adapter invokes it.

**Filter vs interceptor?**
Servlet level before MVC vs MVC level around the handler.

**What should `POST /orders` return?**
201 Created with a `Location` header.

**What is RFC 9457?**
Problem Details: a standard JSON error body.

**What versioning support did Spring 7 add?**
Built-in API versioning strategies (header, path, query, media type).

**Mono vs Flux?**
0 or 1 items vs 0 to N items.

**Why never block in WebFlux?**
A few event-loop threads serve all requests.

## 5. Data and transactions

Read: [JPA & Hibernate](/spring/data/spring-data-jpa-and-hibernate), [Mappings & fetching](/spring/data/entity-mappings-and-fetch-strategies), [JPQL & Criteria](/spring/data/jpql-and-criteria-api), [Transactions](/spring/data/transactions), [Auditing & migrations](/spring/data/auditing-and-migrations), [Caching](/spring/data/caching).

**What is the persistence context?**
The set of managed entities: a first-level cache and a unit of work.

**Why does modifying a managed entity update the database without `save()`?**
Dirty checking at flush.

**Default fetch type of `@ManyToOne`?**
EAGER. Set it to LAZY.

**What is N+1, and two fixes?**
One query plus N queries for associations. Fix it with fetch joins, entity graphs, batch fetching or projections.

**When use Specifications or Criteria?**
For dynamic, optional filters.

**Which exceptions roll back by default?**
Unchecked exceptions and Errors.

**REQUIRED vs REQUIRES_NEW?**
Join or create, vs suspend and create an independent transaction.

**Optimistic vs pessimistic locking?**
A version check at write vs `SELECT … FOR UPDATE`.

**Why not `ddl-auto=update` in production?**
It is uncontrolled schema change. Use Flyway or Liquibase with `validate`.

**What is expand–contract?**
Backward-compatible schema changes first, removing old structures later.

**What does `@Cacheable` implement?**
Cache-aside.

**Why evict a cache after commit?**
Otherwise a concurrent read can re-cache stale data.

## 6. Security and testing

Read: [Filter chain](/spring/security/security-filter-chain), [OAuth2/OIDC/JWT](/spring/security/oauth2-oidc-and-jwt), [Method security](/spring/security/method-level-security), [Unit testing](/spring/production/unit-testing-junit-mockito), [Integration testing](/spring/production/integration-testing).

**What does FilterChainProxy do?**
It picks the first matching SecurityFilterChain and runs its filters.

**When can CSRF be disabled?**
For stateless, bearer-token-only APIs.

**OAuth2 vs OIDC?**
Delegated authorization vs authentication on top (the ID token).

**Why PKCE?**
It binds the authorization code to the client, so stolen codes are useless.

**What must a resource server validate in a JWT?**
Signature, issuer, audience, expiry, then scopes.

**Why use a BFF for SPAs?**
Tokens stay server-side; the browser gets an HttpOnly cookie.

**`@PreAuthorize` vs `@PostAuthorize`?**
Before the call vs after it, using the returned object.

**What is BOLA?**
Missing per-object authorization checks.

**Mock vs stub vs spy?**
A stub returns canned answers; a mock also verifies calls; a spy wraps a real object.

**`@WebMvcTest` vs `@SpringBootTest`?**
The web slice vs the full context.

**What does `@ServiceConnection` do?**
It derives connection properties from Testcontainers.

**What replaced `@MockBean`?**
`@MockitoBean`.

## 7. Microservices with Spring Cloud

Read: [Discovery](/microservices/fundamentals/service-discovery), [Gateway](/microservices/fundamentals/api-gateway), [Config](/microservices/fundamentals/centralized-configuration), [Resilience](/microservices/reliability/circuit-breakers-and-resilience4j), [Observability](/microservices/operations/observability-micrometer-opentelemetry), [Event-driven](/microservices/communication/event-driven-spring-cloud-stream).

**Client-side vs server-side discovery?**
The caller picks an instance through a registry, vs the platform routes the request.

**Eureka self-preservation?**
Stops evicting instances when many heartbeats are lost (it favours availability).

**What discovery should you use on Kubernetes?**
Kubernetes Services and DNS.

**Predicate vs filter in Spring Cloud Gateway?**
Route matching vs request or response behaviour.

**What is token relay?**
The gateway forwards the user's access token downstream.

**How does a client import Config Server configuration?**
`spring.config.import=configserver:…`

**Circuit breaker states?**
CLOSED, OPEN, HALF_OPEN.

**Why add jitter to retries?**
To avoid synchronised retry storms.

**What did Spring 7 add for resilience?**
`@Retryable`, `RetryTemplate` and `@ConcurrencyLimit`.

**What is the Observation API?**
One instrumentation point that produces a metric and a span.

**What is `traceparent`?**
The W3C header that propagates trace context.

**Why avoid user IDs as metric tags?**
Cardinality explosion.

**What do consumer groups in Spring Cloud Stream do?**
Each group gets every message; instances in a group share the load.

**How do you publish an event reliably with a DB change?**
The transactional outbox.

**Why idempotent consumers?**
Delivery is at-least-once.

## Score yourself

| Score | Meaning |
|---|---|
| 90%+ | Ready to review services. Use the [service review checklist](/self-review/service-review-checklist). |
| 70–89% | Reread the sections you hesitated on. |
| Below 70% | Follow the [Spring Boot Developer learning path](/learning-paths/spring-boot-developer). |
