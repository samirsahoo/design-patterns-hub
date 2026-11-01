---
title: "Circuit Breakers and Resilience with Resilience4j"
linkTitle: "Circuit breakers (Resilience4j)"
description: "Keeping services up when dependencies fail: timeouts, retries with backoff and jitter, circuit breaker states and configuration, bulkheads, rate limiters and time limiters with Resilience4j in Spring Boot, the Spring Cloud CircuitBreaker abstraction, Spring Framework 7's built-in @Retryable and @ConcurrencyLimit, fallbacks, ordering of patterns, and monitoring."
weight: 1
difficulty: intermediate
status: complete
javaVersion: 21
tags: [microservices, resilience, resilience4j, reliability]
keywords: [circuit breaker pattern, resilience4j spring boot, circuit breaker states closed open half open, retry exponential backoff jitter, bulkhead pattern, rate limiter resilience4j, timelimiter, spring cloud circuitbreaker, spring framework 7 retryable concurrencylimit, fallback method, cascading failure]
lastReviewed: 2026-11-01
prerequisites:
  - /microservices/fundamentals/service-discovery
related:
  - /microservices/fundamentals/api-gateway
  - /microservices/operations/observability-micrometer-opentelemetry
---

## Simple definition

**Resilience patterns** stop one slow or failing dependency from taking down the services that call it:

- **timeouts** bound waiting
- **retries** absorb transient errors
- **circuit breakers** fail fast while a dependency is unhealthy
- **bulkheads** isolate resources
- **rate limiters** control load.

**Resilience4j** is the standard Java library for these patterns, integrated with Spring Boot.

## The problem

A payment API that becomes slow (not down, just slow) is worse than one that fails: callers' threads and connections wait, pools fill up, and the failure **cascades** up the call chain until the whole platform is unavailable. Blind retries make it worse by multiplying load on the struggling service.

## The patterns

| Pattern | What it does | Key settings |
|---|---|---|
| **Timeout / TimeLimiter** | Abandon calls that take too long | Shorter than the caller's own deadline |
| **Retry** | Repeat transient failures | Max attempts, **exponential backoff + jitter**, which exceptions to retry, **idempotent operations only** |
| **Circuit breaker** | Stop calling a failing dependency for a while; fail fast with a fallback | Failure-rate and slow-call thresholds, window, wait duration |
| **Bulkhead** | Limit concurrent calls (semaphore or thread pool) per dependency | Max concurrent calls, max wait |
| **Rate limiter** | Limit calls per time period (client-side) | Limit per period, timeout |
| **Fallback** | Alternative response | Cached value, default or degraded feature |

## Circuit breaker states

{{< interactive-diagram file="circuit-breaker.html" title="Resilience4j circuit breaker states" source="diagrams/archify/circuit-breaker.lifecycle.json" tool="Archify" height="680" >}}

- **CLOSED:** calls pass. Outcomes are recorded in a **sliding window** (count- or time-based).
- When the **failure rate** or **slow-call rate** exceeds the threshold (after a minimum number of calls), it goes **OPEN**: calls fail immediately with `CallNotPermittedException`, and the fallback runs.
- After `waitDurationInOpenState`, it goes **HALF_OPEN**: a few trial calls decide whether to go back to **CLOSED** or **OPEN** again.

## Resilience4j in Spring Boot

```yaml
resilience4j:
  circuitbreaker:
    instances:
      inventory:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 50
        minimum-number-of-calls: 20
        failure-rate-threshold: 50              # %
        slow-call-duration-threshold: 800ms
        slow-call-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 5
        record-exceptions: [java.io.IOException, org.springframework.web.client.ResourceAccessException]
        ignore-exceptions: [com.shop.inventory.SkuNotFoundException]   # business errors are not failures
  retry:
    instances:
      inventory:
        max-attempts: 3
        wait-duration: 200ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
        enable-randomized-wait: true            # jitter
  bulkhead:
    instances:
      inventory:
        max-concurrent-calls: 25
  timelimiter:
    instances:
      inventory:
        timeout-duration: 1s
```

```java
@Service
class InventoryClient {
    @CircuitBreaker(name = "inventory", fallbackMethod = "cachedStock")
    @Retry(name = "inventory")
    @Bulkhead(name = "inventory")
    public Stock stock(String sku) {
        return restClient.get().uri("/api/stock/{sku}", sku).retrieve().body(Stock.class);
    }

    private Stock cachedStock(String sku, Throwable t) {            // same signature + Throwable
        return stockCache.getOrDefault(sku, Stock.unknown(sku));    // degrade gracefully
    }
}
```

**Order of decorators** (Resilience4j default aspect order): `Retry( CircuitBreaker( RateLimiter( TimeLimiter( Bulkhead( call ) ) ) ) )`. Retry is outermost, so each retry attempt is counted by the circuit breaker, and an open circuit stops further retries quickly.

**Also set HTTP client timeouts** (connect and read) on `RestClient` and `WebClient`. Annotations do not interrupt a blocking socket read by themselves.

## Spring Cloud CircuitBreaker and Spring Framework 7

- **Spring Cloud CircuitBreaker** offers a vendor-neutral API (`CircuitBreakerFactory`) with a Resilience4j implementation. The [gateway's](/microservices/fundamentals/api-gateway) `CircuitBreaker` filter uses it.
- **Spring Framework 7** adds **core resilience features** with no extra library: `@Retryable` (with backoff and jitter, also for reactive return types), `RetryTemplate`, and `@ConcurrencyLimit` (a bulkhead-style limit, useful with virtual threads). Enable them with `@EnableResilientMethods`. They replace the retired **Spring Retry** project. **Circuit breaking and rate limiting still come from Resilience4j.**

```java
@Retryable(includes = ResourceAccessException.class, maxRetries = 3, delay = 100, multiplier = 2, jitter = 50)
@ConcurrencyLimit(20)
public Quote quote(String sku) { ... }
```

## Designing for failure

- **Timeouts everywhere**, with a budget. Each hop's timeout must be smaller than its caller's.
- **Retry only idempotent operations** (GET, or a POST with an idempotency key), with jitter, and **never at every layer**: retries multiply (3 layers × 3 retries = 27 calls).
- **Fallbacks must be safe:** stale data clearly marked, feature degradation, or a queued request. Never "pretend success" for payments.
- **Asynchronous messaging** removes temporal coupling for non-interactive work. See [event-driven architecture](/microservices/communication/event-driven-spring-cloud-stream).
- **Load shedding** at the edge (rate limits, 429) protects the core.
- **Test it:** chaos experiments (fault injection, latency injection with Toxiproxy or a service mesh).

## Monitoring

Resilience4j publishes Micrometer metrics (`resilience4j.circuitbreaker.state`, call outcomes, retry counts) and Actuator health and event endpoints. **Alert on circuits opening.** That is a symptom worth knowing about.

## Common mistakes

> [!MISTAKE] Retrying non-idempotent operations
> Retrying a payment POST without idempotency can charge twice.

> [!MISTAKE] Counting business errors as failures
> A 404 "SKU not found" should not open the circuit. Use `ignore-exceptions` and record only technical failures.

> [!MISTAKE] No timeouts on the HTTP client
> Without connect and read timeouts, threads hang until the OS gives up. Set them explicitly.

## Quick check

**What are the three main circuit breaker states?**
CLOSED (calls pass and are measured), OPEN (fail fast) and HALF_OPEN (trial calls decide whether to close or reopen).

**Why add jitter to retry backoff?**
So that many clients do not retry in synchronised waves and overload the recovering service.

**What does a bulkhead do?**
It limits concurrent calls to a dependency, so one slow dependency cannot exhaust all threads or connections.

**What did Spring Framework 7 add for resilience?**
Built-in `@Retryable`, `RetryTemplate` and `@ConcurrencyLimit` (enabled with `@EnableResilientMethods`). Circuit breakers still need Resilience4j.

**Why shouldn't every layer retry?**
Retries multiply across layers and create retry storms that amplify outages.

## Summary

- Timeouts first, then careful retries (idempotent, backoff + jitter), circuit breakers, bulkheads and rate limits.
- Resilience4j provides them all, configured per dependency, with Micrometer metrics.
- Spring 7 adds core `@Retryable` and `@ConcurrencyLimit`; fallbacks must be safe; test with chaos.

## References

- [Resilience4j documentation](https://resilience4j.readme.io/docs).
- Spring Cloud CircuitBreaker, [reference](https://docs.spring.io/spring-cloud-circuitbreaker/reference/).
- Spring Framework reference, [Resilience features](https://docs.spring.io/spring-framework/reference/core/resilience.html).
- Michael Nygard, *Release It!* (2nd edition), stability patterns.
- AWS Builders' Library, [Timeouts, retries, and backoff with jitter](https://aws.amazon.com/builders-library/timeouts-retries-and-backoff-with-jitter/).
