---
title: "Spring Boot Developer"
description: "From the IoC container to production-ready Spring Boot services."
weight: 3
audience: "Java developers starting with Spring, or Spring users who want to understand what the framework does for them"
steps:
  - title: "Dependency injection"
    page: /spring/core/dependency-injection
    why: "The idea behind the whole framework, shown first in plain Java and then with Spring."
  - title: "The ApplicationContext, bean lifecycle and scopes"
    why: "What the container creates, when, and for how long."
  - title: "Configuration, profiles and properties"
    why: "How one build runs in development, test and production."
  - title: "AOP and proxies"
    why: "How `@Transactional`, `@Cacheable` and security annotations actually work — and the self-invocation pitfall."
  - title: "Spring MVC and REST controllers"
    why: "Request handling, validation, error responses and DTOs."
  - title: "A complete service with ports and adapters"
    page: /hexagonal-architecture/spring-boot-implementation
    why: "Controllers, validation, Problem Details, JPA and tests in one runnable example."
  - title: "Spring Data JPA and Hibernate"
    why: "Entity lifecycle, relationships, lazy loading and the N+1 problem."
  - title: "Transactions, propagation and isolation"
    why: "Where transactions start and end, and what each isolation level prevents."
  - title: "Spring Security"
    why: "The filter chain, password storage, OAuth 2.0 and JWT validation."
  - title: "Testing: slices, full context and Testcontainers"
    why: "Choosing the cheapest test that proves each behaviour."
  - title: "Production readiness: Actuator, metrics, tracing, graceful shutdown"
    why: "What a service needs before it can be operated safely."
---

Examples use **Spring Boot 4.1** on **Java 21**. Version-specific behaviour is labelled and linked to the official Spring documentation.
