---
title: "API Gateway with Spring Cloud Gateway"
linkTitle: "API gateway"
description: "The API gateway pattern and Spring Cloud Gateway: responsibilities (routing, authentication, rate limiting, resilience, observability), the gateway in a Spring Cloud platform, routes, predicates and filters, the reactive (WebFlux) and Server Web MVC variants, token relay and the BFF pattern, rate limiting with Redis, circuit breakers and retries, and gateway anti-patterns."
weight: 7
difficulty: intermediate
status: complete
javaVersion: 21
tags: [microservices, spring-cloud, api-gateway]
keywords: [api gateway pattern, spring cloud gateway, gateway routes predicates filters, spring cloud gateway server webmvc, spring cloud gateway webflux, token relay bff, redis rate limiter gateway, gateway circuit breaker, backend for frontend, api gateway vs service mesh, gateway api kubernetes]
lastReviewed: 2026-11-01
prerequisites:
  - /microservices/fundamentals/service-discovery
related:
  - /spring/security/oauth2-oidc-and-jwt
  - /microservices/reliability/circuit-breakers-and-resilience4j
---

## Simple definition

An **API gateway** is the **single entry point** for clients into a set of backend services. It **routes** each request to the right service and applies **cross-cutting edge concerns**: authentication, rate limiting, TLS, CORS, request shaping and observability. **Spring Cloud Gateway** is Spring's gateway, configured with **routes**, **predicates** and **filters**.

## The problem

Without a gateway, every client must know every service's address, every service re-implements authentication, rate limiting and CORS, and internal structure leaks to the outside, so you can never split or rename a service without breaking clients.

## The gateway in a Spring Cloud platform

{{< interactive-diagram file="microservices-platform.html" title="Spring Cloud microservices platform" source="diagrams/archify/microservices-platform.architecture.json" tool="Archify" height="700" >}}

| Gateway responsibility | Example |
|---|---|
| Routing and composition | `/api/orders/**` → order-service; path rewriting |
| Authentication | Validate JWTs, or OIDC login + **token relay** (BFF) |
| Rate limiting and quotas | Per user, API key or IP |
| Resilience | Timeouts, retries, circuit breakers, fallbacks |
| Observability | Access logs, metrics, trace propagation |
| Edge protocol handling | TLS termination, CORS, compression, request size limits |
| Canary routing | Weight-based routes for gradual rollouts |

## Routes, predicates and filters

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:                              # Spring Cloud 2025.x property prefix (or .webmvc for the MVC variant)
          routes:
            - id: orders
              uri: lb://order-service         # lb:// uses discovery + LoadBalancer
              predicates:
                - Path=/api/orders/**
                - Method=GET,POST
              filters:
                - StripPrefix=1
                - TokenRelay=                  # forward the user's access token
                - name: RequestRateLimiter
                  args:
                    redis-rate-limiter.replenishRate: 20
                    redis-rate-limiter.burstCapacity: 40
                    key-resolver: "#{@userKeyResolver}"
                - name: CircuitBreaker
                  args:
                    name: orders
                    fallbackUri: forward:/fallback/orders
                - name: Retry
                  args:
                    retries: 2
                    methods: GET
                    backoff: { firstBackoff: 100ms, maxBackoff: 1s, factor: 2 }
            - id: catalog-canary
              uri: lb://catalog-v2
              predicates: [ "Path=/api/catalog/**", "Weight=catalog, 10" ]   # 10% canary
            - id: catalog
              uri: lb://catalog
              predicates: [ "Path=/api/catalog/**", "Weight=catalog, 90" ]
```

- **Predicates** match requests: path, host, method, header, query, cookie, time, remote address, weight.
- **Filters** change requests or responses: add or remove headers, rewrite paths, rate limit, circuit breaker, retry, request size, token relay, cache, de-duplicate response headers.
- Routes can also be defined in Java (`RouteLocatorBuilder`) or loaded dynamically.

## Two flavours

| | Gateway (WebFlux) | Gateway Server Web MVC |
|---|---|---|
| Stack | Reactive, Netty | Servlet, works well with **virtual threads** |
| Strength | Mature, streaming, very high concurrency | Familiar MVC programming model; blocking filters are OK |
| Configuration | `spring.cloud.gateway.server.webflux.*` | `spring.cloud.gateway.server.webmvc.*` (functional routes as well) |

Spring Cloud 2025.1 (for Boot 4) aligned both variants, adding JSpecify null-safety and retry built on Spring Framework 7's retry support. Later releases added MVC retry backoff and security fixes, so keep the gateway patched.

## Security at the gateway

- **JWT validation at the edge** (`oauth2ResourceServer`) rejects bad tokens early, **but services must still validate tokens and authorise** (zero trust; never trust "it came through the gateway").
- **BFF with token relay:** the gateway performs OIDC login, keeps tokens server-side, gives the browser a session cookie, and relays access tokens downstream. See [OAuth2, OIDC and JWT](/spring/security/oauth2-oidc-and-jwt).
- Strip incoming headers that downstream services trust (`X-User-Id`), and set them from the validated token.

## Gateway vs other edge components

| Component | Focus |
|---|---|
| **API gateway** (Spring Cloud Gateway, Kong, Apigee, AWS API Gateway) | Application-level API concerns |
| **Backend for Frontend** | One gateway per client type (web, mobile) shaping APIs for it |
| **Ingress / Kubernetes Gateway API** | Cluster entry routing (L7), TLS |
| **Service mesh** (Istio, Linkerd) | **East-west** (service-to-service) mTLS, retries, telemetry |

Many platforms combine them: cloud load balancer → Ingress/Gateway API → Spring Cloud Gateway (API concerns, BFF) → services in a mesh.

## Anti-patterns

> [!MISTAKE] Business logic in the gateway
> Aggregating domain rules at the edge creates a distributed monolith. Keep the gateway thin: routing and cross-cutting concerns only.

> [!MISTAKE] One gateway team as a bottleneck
> Every route change waits for a central team. Use self-service, GitOps-managed route definitions.

> [!MISTAKE] Retrying non-idempotent calls
> Retrying `POST /payments` at the gateway can double-charge. Retry only safe or idempotent methods, or requests with idempotency keys.

## Quick check

**Why use an API gateway?**
To give clients one entry point and to centralise edge concerns (routing, authentication, rate limiting, resilience, observability) without leaking the internal service structure.

**Predicate vs filter in Spring Cloud Gateway?**
Predicates decide whether a route matches a request. Filters modify the request or response, or add behaviour such as rate limiting or circuit breaking.

**What does `lb://order-service` mean?**
Resolve `order-service` through service discovery and Spring Cloud LoadBalancer.

**What is token relay?**
The gateway forwards the logged-in user's OAuth2 access token to downstream services, typically in a BFF setup.

**Should services trust the gateway's authentication alone?**
No. Services must still validate tokens and enforce authorization (zero trust).

## Summary

- The gateway is the thin, central edge: routes, predicates and filters.
- It handles authentication (JWT or BFF token relay), rate limits, resilience and canaries; services still authorise.
- Choose the WebFlux or MVC variant; avoid business logic and non-idempotent retries at the gateway.

## References

- Spring Cloud Gateway, [reference documentation](https://docs.spring.io/spring-cloud-gateway/reference/).
- Spring blog, [Spring Cloud 2025.1.0 (Oakwood) released](https://spring.io/blog/2025/11/25/spring-cloud-2025-1-0-aka-oakwood-has-been-released/).
- Chris Richardson, [API gateway pattern](https://microservices.io/patterns/apigateway.html); Sam Newman, [Backends For Frontends](https://samnewman.io/patterns/architectural/bff/).
