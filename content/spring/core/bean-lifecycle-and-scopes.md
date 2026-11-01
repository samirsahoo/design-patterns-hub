---
title: "Bean Lifecycle and Scopes"
linkTitle: "Bean lifecycle & scopes"
description: "What happens to a Spring bean from definition to destruction: instantiation, injection, Aware callbacks, BeanPostProcessors, @PostConstruct and InitializingBean, SmartLifecycle, @PreDestroy and graceful shutdown; plus singleton, prototype, request, session and application scopes, scoped proxies, and thread-safety rules."
weight: 3
difficulty: intermediate
status: complete
javaVersion: 21
tags: [spring, bean-lifecycle, scopes]
keywords: [spring bean lifecycle, postconstruct predestroy, initializingbean disposablebean, beanpostprocessor order, smartlifecycle, spring bean scopes, singleton vs prototype, request scope session scope, scoped proxy, prototype in singleton, lookup method, objectprovider]
lastReviewed: 2026-11-01
prerequisites:
  - /spring/core/applicationcontext-and-beanfactory
related:
  - /spring/core/aop-and-proxies
  - /spring/core/application-events
---

## Simple definition

The **bean lifecycle** is the sequence of steps Spring performs to create, initialise, use and destroy a bean. A bean's **scope** decides **how many instances** exist and **how long** each lives.

## The problem

Opening connections in a constructor before properties are injected, forgetting to close thread pools on shutdown, or storing per-user data in a singleton are common, serious bugs. Knowing the lifecycle tells you **where** code belongs; knowing scopes tells you what is **shared**.

## The lifecycle

{{< interactive-diagram file="bean-lifecycle.html" title="Spring singleton bean lifecycle" source="diagrams/archify/bean-lifecycle.lifecycle.json" tool="Archify" height="680" >}}

| # | Step | Hook you can use |
|---|---|---|
| 1 | Instantiate (constructor; dependencies injected here with constructor injection) | Constructor |
| 2 | Populate properties (setter and field injection) | — |
| 3 | Aware callbacks | `BeanNameAware`, `BeanFactoryAware`, `ApplicationContextAware` |
| 4 | `BeanPostProcessor.postProcessBeforeInitialization` | Framework hooks |
| 5 | **Initialisation**: `@PostConstruct` → `InitializingBean.afterPropertiesSet()` → `@Bean(initMethod)` | Validate config, warm caches |
| 6 | `BeanPostProcessor.postProcessAfterInitialization` | **AOP proxies are created here** |
| 7 | Bean in use. `SmartLifecycle.start()` for components that run (consumers, schedulers) | `SmartLifecycle` |
| 8 | On shutdown: `SmartLifecycle.stop()` → `@PreDestroy` → `DisposableBean.destroy()` → `destroyMethod` | Release resources |

```java
@Component
class ExchangeRateCache implements SmartLifecycle {
    private final RatesClient client;
    private volatile boolean running;
    ExchangeRateCache(RatesClient client) { this.client = client; }       // 1: dependencies

    @PostConstruct void validate() { /* check configuration, fail fast */ } // 5
    @Override public void start()  { warmUp(); running = true; }          // 7: after the context is ready
    @Override public void stop()   { running = false; }                   // 8: graceful stop
    @Override public boolean isRunning() { return running; }
    @PreDestroy void close() { client.close(); }                          // 8: release
}
```

> [!TIP] Where to put startup work
> - **Validation and cheap setup:** `@PostConstruct`.
> - **Long or external work** (warming caches, connecting consumers): `SmartLifecycle` or `ApplicationReadyEvent`, so it runs after the context is fully built.
> - Avoid calling other beans' proxied methods in `@PostConstruct`; the proxies of later beans may not be ready.

**Graceful shutdown** (Spring Boot): `server.shutdown=graceful` (the default since Boot 3.4) stops accepting new requests and lets in-flight ones finish within `spring.lifecycle.timeout-per-shutdown-phase`. `SmartLifecycle` beans stop in reverse **phase** order.

## Scopes

| Scope | Instances | Lives for | Typical use |
|---|---|---|---|
| **singleton** (default) | One per container | The container's life | Stateless services, repositories, clients |
| **prototype** | New one per injection or `getBean` | Until garbage collected; **Spring does not call destroy** | Stateful, short-lived helpers |
| **request** | One per HTTP request | The request | Request context data |
| **session** | One per HTTP session | The session | User session state (use sparingly) |
| **application** | One per `ServletContext` | The web app | Rarely needed |
| **websocket** | One per WebSocket session | The session | WebSocket state |
| Custom | Your rules | — | For example a thread or tenant scope |

### Singleton beans must be thread-safe

A singleton serves **all requests concurrently**. Keep it **stateless**: only final, injected dependencies and immutable configuration. Put per-request data in method parameters and local variables.

```java
@Service
class PriceService {
    private BigDecimal lastPrice;        // ❌ shared mutable state: a race between requests
    BigDecimal quote(String sku) { ... }
}
```

### Injecting a shorter-lived bean into a singleton

A singleton is created once, so injecting a `request`-scoped or `prototype` bean directly gives it **one instance forever**. Solutions:

```java
// 1. Scoped proxy: inject a proxy that resolves the current request's instance on each call
@Component @RequestScope           // proxyMode = TARGET_CLASS by default
class RequestContext { String tenantId; }

// 2. ObjectProvider: ask for a fresh prototype when needed
@Service
class ReportService {
    private final ObjectProvider<ReportBuilder> builders;
    ReportService(ObjectProvider<ReportBuilder> builders) { this.builders = builders; }
    Report build() { return builders.getObject().withDefaults().build(); }  // new prototype each time
}
```

The other option is `@Lookup` method injection, where Spring overrides an abstract method to return a new instance.

## Lazy initialisation

- `@Lazy` creates a bean on first use instead of at startup.
- `spring.main.lazy-initialization=true` makes everything lazy: faster startup, but configuration errors appear later at runtime. That is useful in development, risky in production.

## Common mistakes

> [!MISTAKE] Mutable fields in singletons
> Per-request data in a singleton field leaks between users and races under load.

> [!MISTAKE] Expecting `@PreDestroy` on prototypes
> Spring does not manage prototype destruction. Close resources yourself, or use a different scope.

> [!MISTAKE] Session scope as a data store
> Large session beans hurt memory and horizontal scaling. Keep sessions small, or stateless with tokens.

## Quick check

**In what order do initialisation callbacks run?**
`@PostConstruct`, then `afterPropertiesSet()`, then the custom `initMethod`. All run after injection and before `postProcessAfterInitialization`.

**At which lifecycle step are AOP proxies created?**
In `BeanPostProcessor.postProcessAfterInitialization`.

**What is the default scope, and what does it imply?**
Singleton: one shared instance per container, so it must be stateless or thread-safe.

**How do you use a request-scoped bean from a singleton?**
Inject a scoped proxy (the default for `@RequestScope`), or look it up through `ObjectProvider`.

**Does Spring call `@PreDestroy` on prototype beans?**
No. The container hands prototypes over and does not track their destruction.

**Where should long startup work go?**
In `SmartLifecycle.start()` or an `ApplicationReadyEvent` listener, not the constructor.

## Summary

- Lifecycle: instantiate → inject → Aware → init callbacks → proxies → use → stop → destroy.
- `SmartLifecycle` and graceful shutdown handle running components cleanly.
- Singleton is the default: keep it stateless. Use scoped proxies or `ObjectProvider` for shorter scopes.

## References

- Spring Framework reference, [Customizing the Nature of a Bean](https://docs.spring.io/spring-framework/reference/core/beans/factory-nature.html).
- Spring Framework reference, [Bean Scopes](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html).
- Spring Boot reference, [Graceful shutdown](https://docs.spring.io/spring-boot/reference/web/graceful-shutdown.html).
