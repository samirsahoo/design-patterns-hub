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
    page: /spring/core/applicationcontext-and-beanfactory
    also: [/spring/core/bean-lifecycle-and-scopes]
    why: "What the container creates, when, and for how long."
  - title: "Configuration, profiles and properties"
    page: /spring/core/configuration-and-component-scanning
    also: [/spring/boot/auto-configuration-and-conditions, /spring/boot/externalized-configuration]
    why: "How one build runs in development, test and production."
  - title: "AOP and proxies"
    page: /spring/core/aop-and-proxies
    also: [/spring/core/application-events, /spring/core/spring-expression-language]
    why: "How `@Transactional`, `@Cacheable` and security annotations actually work — and the self-invocation pitfall."
  - title: "Spring MVC and REST controllers"
    page: /spring/web/spring-mvc-architecture
    also: [/spring/web/rest-api-design-and-error-handling, /spring/web/webflux-and-reactive-programming]
    why: "Request handling, validation, error responses and DTOs."
  - title: "A complete service with ports and adapters"
    page: /hexagonal-architecture/spring-boot-implementation
    why: "Controllers, validation, Problem Details, JPA and tests in one runnable example."
  - title: "Spring Data JPA and Hibernate"
    page: /spring/data/spring-data-jpa-and-hibernate
    also: [/spring/data/entity-mappings-and-fetch-strategies, /spring/data/jpql-and-criteria-api]
    why: "Entity lifecycle, relationships, lazy loading and the N+1 problem."
  - title: "Transactions, propagation and isolation"
    page: /spring/data/transactions
    also: [/spring/data/auditing-and-migrations, /spring/data/caching]
    why: "Where transactions start and end, and what each isolation level prevents."
  - title: "Spring Security"
    page: /spring/security/security-filter-chain
    also: [/spring/security/oauth2-oidc-and-jwt, /spring/security/method-level-security]
    why: "The filter chain, password storage, OAuth 2.0 and JWT validation."
  - title: "Testing: slices, full context and Testcontainers"
    page: /spring/production/integration-testing
    also: [/spring/production/unit-testing-junit-mockito]
    why: "Choosing the cheapest test that proves each behaviour."
  - title: "Production readiness: Actuator, metrics, tracing, graceful shutdown"
    page: /spring/boot/actuator-and-custom-endpoints
    also: [/microservices/operations/observability-micrometer-opentelemetry]
    why: "What a service needs before it can be operated safely."
---

Examples use **Spring Boot 4.1** on **Java 21**. Version-specific behaviour is labelled and linked to the official Spring documentation.
