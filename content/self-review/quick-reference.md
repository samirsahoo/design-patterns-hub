---
title: "Java & Spring Quick Reference"
linkTitle: "Quick reference"
description: "One page of the essentials: Java collections and concurrency choices, modern Java by version, Spring annotations by purpose, transaction propagation and isolation, Spring Boot test slices, security defaults, resilience settings, and current versions."
weight: 3
difficulty: beginner
status: complete
tags: [self-review, reference]
keywords: [java cheat sheet, spring annotations cheat sheet, spring boot cheat sheet, transaction propagation table, spring test slices table, resilience4j cheat sheet, java versions features]
lastReviewed: 2026-11-01
related:
  - /self-review/quick-check
---

## Java choices

| Need | Use |
|---|---|
| List | `ArrayList` |
| Lookup | `HashMap` / `HashSet` |
| Sorted or range queries | `TreeMap` / `TreeSet` |
| Queue or stack | `ArrayDeque` |
| Thread-safe map | `ConcurrentHashMap` (`computeIfAbsent`, `merge`) |
| Producer–consumer | `BlockingQueue` |
| Counter under contention | `LongAdder` |
| Blocking I/O fan-out | Virtual threads (`newVirtualThreadPerTaskExecutor`) |
| CPU-bound parallel work | A fixed pool ≈ cores, or `ForkJoinPool` |
| Async composition | `CompletableFuture` (always pass an executor) |
| Context passing | Scoped values (Java 25), else ThreadLocal |

## Modern Java by version

| Version | Highlights |
|---|---|
| 17 LTS | Sealed classes, records (16), pattern-matching `instanceof` (16), text blocks (15) |
| 21 LTS | Virtual threads, record patterns, pattern-matching `switch`, sequenced collections, generational ZGC |
| 22–24 | Unnamed `_`, FFM API, stream gatherers, no `synchronized` pinning (24) |
| **25 LTS** | Flexible constructor bodies, module imports, compact source files, scoped values, compact object headers |
| 26 | HTTP/3 client, warnings for reflective `final` mutation |
| 27 (Sept 2026) | G1 the default everywhere, post-quantum hybrid TLS key exchange; previews continue |

## Spring annotations by purpose

| Purpose | Annotations |
|---|---|
| Beans | `@Component`, `@Service`, `@Repository`, `@Configuration`, `@Bean`, `@Primary`, `@Qualifier`, `@Lazy`, `@Scope`, `@Profile` |
| Lifecycle | `@PostConstruct`, `@PreDestroy`, `SmartLifecycle` |
| Config | `@ConfigurationProperties`, `@Value`, `@ConfigurationPropertiesScan` |
| Conditions | `@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`, `@ConditionalOnWebApplication` |
| Web | `@RestController`, `@GetMapping`…, `@PathVariable`, `@RequestParam`, `@RequestBody`, `@Valid`, `@RestControllerAdvice`, `@ExceptionHandler` |
| Data | `@Entity`, `@Id`, `@ManyToOne(fetch = LAZY)`, `@Query`, `@EntityGraph`, `@Transactional`, `@Version`, `@Lock` |
| Cache | `@EnableCaching`, `@Cacheable`, `@CachePut`, `@CacheEvict` |
| Events and async | `@EventListener`, `@TransactionalEventListener`, `@Async`, `@Scheduled` |
| Security | `@EnableWebSecurity`, `@EnableMethodSecurity`, `@PreAuthorize`, `@PostAuthorize` |
| Resilience | Spring 7: `@Retryable`, `@ConcurrencyLimit`; Resilience4j: `@CircuitBreaker`, `@Retry`, `@Bulkhead`, `@RateLimiter` |
| Observability | `@Observed`, `@Timed` |
| Testing | `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`, `@MockitoBean`, `@ServiceConnection`, `@WithMockUser` |

## Transactions

| Propagation | Behaviour |
|---|---|
| REQUIRED (default) | Join or create |
| REQUIRES_NEW | Suspend, create a new one (second connection) |
| NESTED | Savepoint |
| SUPPORTS / NOT_SUPPORTED / NEVER / MANDATORY | Join-if-present / suspend / fail-if-present / fail-if-absent |

Rollback on `RuntimeException` and `Error` by default. Proxies apply only to public methods called from outside the bean. Default isolation: READ COMMITTED (PostgreSQL, Oracle, SQL Server), REPEATABLE READ (MySQL).

## Test slices

| Slice | Loads |
|---|---|
| `@WebMvcTest` | MVC + security filters for the given controllers |
| `@DataJpaTest` | JPA + repositories + migrations (rolled back) |
| `@JsonTest` | Jackson |
| `@RestClientTest` | HTTP client + a mock server |
| `@SpringBootTest` | Everything |

## Security defaults to remember

- Deny by default: `anyRequest().authenticated()` last.
- CSRF on for cookie sessions; off only for bearer-token APIs.
- JWT: validate signature, `iss`, **`aud`** and `exp`.
- Authorization code + PKCE; a BFF for SPAs; client credentials between services.
- Object-level checks against BOLA.

## Resilience starting values

| Setting | Typical start |
|---|---|
| HTTP connect / read timeout | 1 s / 2–5 s (within the caller's budget) |
| Retries | 2–3, exponential backoff from 100–200 ms, with jitter, idempotent calls only |
| Circuit breaker | 50% failure or slow-call rate over 20–50 calls, open for 30 s, 5 half-open trials |
| Bulkhead | Concurrent calls ≈ the dependency's capacity share |

## Versions (November 2026)

Java 25 LTS (27 latest) · Spring Boot 4.1 (4.2 due Nov 2026) · Spring Framework 7 · Spring Security 7 · Spring Cloud 2025.1 (Oakwood) · Hibernate ORM 7 · JUnit 6 · Kafka 4.2.
