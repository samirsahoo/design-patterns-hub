---
title: "Spring Boot Service Review Checklist"
linkTitle: "Service review checklist"
description: "A repeatable checklist for reviewing a Java/Spring Boot service before production: code and Java usage, Spring configuration, API design, persistence and transactions, security, resilience, observability, testing and operations, with red flags and a scoring rubric."
weight: 2
difficulty: intermediate
status: complete
tags: [self-review, spring-boot, review]
keywords: [spring boot code review checklist, production readiness checklist spring boot, java service review, microservice review checklist, spring boot best practices checklist]
lastReviewed: 2026-11-01
related:
  - /self-review/quick-check
  - /hexagonal-architecture/spring-boot-implementation
---

## How to use it

Review your own service before release, or a team's service at a production-readiness review. Score each area 0–3 and note evidence (links to code, tests, dashboards).

```mermaid {title="Nine review areas"}
flowchart LR
    A1["1 · Java & code"] --> A2["2 · Spring config"] --> A3["3 · API"] --> A4["4 · Data & tx"] --> A5["5 · Security"]
    A5 --> A6["6 · Resilience"] --> A7["7 · Observability"] --> A8["8 · Testing"] --> A9["9 · Operations"]
```

## 1. Java and code

- Immutable value objects and records; no shared mutable state in singletons?
- Executors bounded and named; no blocking in the common ForkJoinPool; virtual threads considered for I/O?
- Exceptions chained, translated at boundaries, never swallowed?
- **Red flags:** raw types; `catch (Exception e) {}`; `synchronized` around blocking I/O on Java 21–23; unbounded caches.

## 2. Spring configuration

- Constructor injection throughout; no circular dependencies?
- `@ConfigurationProperties` records with validation; no secrets in `application.yml`?
- Auto-configuration customised through properties or customisers rather than exclusions?
- **Red flags:** field injection everywhere; `@EnableWebMvc` in Boot; logic branching on profiles.

## 3. API design

- Resource URIs, correct methods and status codes; 201 + `Location` on create?
- Validation at the edge; DTOs, not entities; pagination?
- Errors as RFC 9457 Problem Details through `@RestControllerAdvice`; no stack traces?
- Idempotency keys for non-idempotent operations; versioning strategy?
- **Red flags:** 200 with error bodies; returning JPA entities; retrying POSTs.

## 4. Persistence and transactions

- All associations LAZY; N+1 checked (query counts in tests); open-in-view disabled?
- `@Transactional` on public service methods; rollback rules understood; no remote calls inside transactions?
- Locking strategy for contended data (`@Version` or `FOR UPDATE`)?
- Flyway or Liquibase migrations with `ddl-auto=validate`; expand–contract for breaking changes?
- Caches with TTLs, evicted after commit?
- **Red flags:** EAGER `@ManyToOne`; `ddl-auto=update`; `@Transactional` on private methods.

## 5. Security

- A SecurityFilterChain per area; deny by default; CSRF correct for the client type?
- JWT validation with issuer and **audience**; scopes and roles mapped?
- Object-level checks (ownership or tenant) on every resource access (`@PreAuthorize` or bean checks)?
- Actuator on a separate port, with sensitive endpoints protected?
- Dependencies scanned; secrets from a vault or secret manager?
- **Red flags:** role checks only (BOLA); `permitAll` before specific rules; tokens in localStorage.

## 6. Resilience

- Timeouts on every outbound call (connect and read)?
- Retries only on idempotent calls, with backoff and jitter; not at every layer?
- Circuit breakers and bulkheads per dependency; safe fallbacks?
- Outbox + idempotent consumers + DLQs for messaging?
- **Red flags:** no HTTP client timeouts; infinite retries; "pretend success" fallbacks for payments.

## 7. Observability

- Micrometer metrics (RED) with percentile histograms; business metrics?
- Traces exported via OTLP; context propagated over HTTP and messaging?
- Structured logs with trace IDs; no PII in logs?
- SLOs with burn-rate alerts; dashboards per service?
- **Red flags:** high-cardinality tags; logs without correlation; alerts on every spike.

## 8. Testing

- Fast unit tests for domain logic (real objects and fakes; mocks at boundaries)?
- `@WebMvcTest` for controllers (validation, errors, security)?
- `@DataJpaTest` + Testcontainers on the real database engine; migrations tested?
- A few `@SpringBootTest` end-to-end flows; contract tests with consumers?
- **Red flags:** H2 instead of the real database; `@SpringBootTest` everywhere; flaky time-dependent tests.

## 9. Operations

- Liveness and readiness probes correct (dependencies only in readiness); graceful shutdown?
- Container memory sized (`MaxRAMPercentage`); GC logs available?
- Configuration externalised; one artifact for every environment?
- Runbooks, on-call ownership, dependency and version upgrade plan (Java 25 LTS, Boot 4.x)?
- **Red flags:** heap equal to the container limit; database checks in liveness; unpatched framework versions.

## Scoring rubric

| Score | Meaning |
|---|---|
| 0 | Not considered |
| 1 | Considered, but not implemented |
| 2 | Implemented, but not verified |
| 3 | Implemented and verified (tests, dashboards, drills) |

A service is **ready for production** with no zeros in areas 4–7 and an average of at least 2.

## Quick check

**Which areas must not score zero for production?**
Persistence and transactions, security, resilience and observability.

**What makes a score of 3?**
Implemented **and** verified with evidence: tests, dashboards or drills.
