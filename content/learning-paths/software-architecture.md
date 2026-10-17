---
title: "Software Architecture"
description: "From architecture fundamentals through layered and hexagonal styles to Domain-Driven Design and decision records."
weight: 4
audience: "Developers moving into senior, lead or architect roles"
steps:
  - title: "Introduction to software architecture"
    page: /software-architecture/introduction
    why: "Quality attributes, trade-offs and the vocabulary used everywhere else."
  - title: "Coupling, cohesion and modularity"
    why: "The measures behind almost every structural decision."
  - title: "Layered versus Hexagonal Architecture"
    page: /hexagonal-architecture/layered-vs-hexagonal
    why: "Dependency direction is the most important structural decision inside an application."
  - title: "Hexagonal Architecture with Spring Boot"
    page: /hexagonal-architecture/spring-boot-implementation
    why: "See the ideas as a tested, runnable service."
  - title: "Clean and Onion Architecture"
    why: "Variations on the same dependency rule, with different internal structure."
  - title: "Domain-Driven Design fundamentals"
    page: /domain-driven-design/fundamentals
    why: "How to model a complex business domain, and when the effort is worth it."
  - title: "Bounded contexts and context maps"
    page: /domain-driven-design/strategic/bounded-context
    why: "Boundaries that guide modules, services and teams."
  - title: "Modular monolith versus microservices"
    page: /microservices/fundamentals/microservices-vs-modular-monolith
    why: "Modularity and distribution are separate decisions."
  - title: "Event-driven architecture, CQRS and event sourcing"
    why: "Styles for integration, read scaling and auditability — with significant costs."
  - title: "Architecture Decision Records"
    why: "Recording decisions so that their reasons survive. The introduction article already includes a template."
---

The first step is a prerequisite for everything else. Steps 3 and 4, and steps 6 and 7, are best read as pairs.
