---
title: "Microservices Engineer"
description: "Service boundaries, communication, resilience, distributed data, observability and deployment."
weight: 5
audience: "Developers building or operating distributed systems"
steps:
  - title: "Should this be microservices at all?"
    page: /microservices/fundamentals/microservices-vs-modular-monolith
    why: "The decision that comes before every other one in this path."
  - title: "Service boundaries from bounded contexts"
    page: /domain-driven-design/strategic/bounded-context
    why: "Boundaries drawn along the domain avoid the distributed monolith."
  - title: "Spring Cloud platform: discovery, gateway and configuration"
    page: /microservices/fundamentals/service-discovery
    also: [/microservices/fundamentals/api-gateway, /microservices/fundamentals/centralized-configuration]
    why: "How services find each other, how traffic enters, and where configuration lives."
  - title: "API design and contracts"
    page: /spring/web/rest-api-design-and-error-handling
    also: [/comparisons/rest-graphql-grpc]
    why: "Versioning, compatibility and consumer-driven contracts."
  - title: "Messaging: Kafka and RabbitMQ fundamentals"
    page: /microservices/communication/event-driven-spring-cloud-stream
    why: "Delivery guarantees, ordering, partitions and consumer groups."
  - title: "Resilience: timeouts, retries, circuit breakers, bulkheads"
    page: /microservices/reliability/circuit-breakers-and-resilience4j
    why: "How to keep one slow dependency from taking down everything."
  - title: "Distributed data: sagas, outbox and idempotency"
    page: /microservices/data-consistency/saga-pattern
    also: [/microservices/data-consistency/transactional-outbox, /microservices/reliability/idempotency, /microservices/data-consistency/cqrs-and-event-sourcing, /system-design/case-studies/order-management-system]
    why: "The case study shows an orchestrated saga with a transactional outbox and idempotent payments end to end."
  - title: "Observability: logs, metrics, traces and OpenTelemetry"
    page: /microservices/operations/observability-micrometer-opentelemetry
    why: "You cannot operate what you cannot see."
  - title: "Deployment: containers, orchestration and release strategies"
    page: /system-design/building-blocks/deployment-strategies
    why: "Rolling, blue–green and canary releases, and what an orchestrator does for you."
  - title: "Security between services"
    page: /spring/security/oauth2-oidc-and-jwt
    why: "Service identity, mutual TLS, token propagation and secrets management."
---

Microservices are a trade-off, not a destination. This path starts with the decision to use them and returns to it in every case study.
