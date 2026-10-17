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
  - title: "API design and contracts"
    why: "Versioning, compatibility and consumer-driven contracts."
  - title: "Messaging: Kafka and RabbitMQ fundamentals"
    why: "Delivery guarantees, ordering, partitions and consumer groups."
  - title: "Resilience: timeouts, retries, circuit breakers, bulkheads"
    why: "How to keep one slow dependency from taking down everything."
  - title: "Distributed data: sagas, outbox and idempotency"
    page: /system-design/case-studies/order-management-system
    why: "The case study shows an orchestrated saga with a transactional outbox and idempotent payments end to end."
  - title: "Observability: logs, metrics, traces and OpenTelemetry"
    why: "You cannot operate what you cannot see."
  - title: "Deployment: containers, orchestration and release strategies"
    why: "Rolling, blue–green and canary releases, and what an orchestrator does for you."
  - title: "Security between services"
    why: "Service identity, mutual TLS, token propagation and secrets management."
---

Microservices are a trade-off, not a destination. This path starts with the decision to use them and returns to it in every case study.
