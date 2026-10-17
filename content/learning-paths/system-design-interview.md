---
title: "System Design Interview Preparation"
description: "A repeatable framework, estimation practice, building blocks and worked case studies."
weight: 6
audience: "Candidates preparing for system design interviews"
steps:
  - title: "Requirements and the 18-step framework"
    page: /system-design/introduction
    why: "A structure that keeps you from jumping straight to technologies."
  - title: "Capacity estimation"
    page: /system-design/introduction
    why: "The worked example shows how estimates drive decisions such as caching and sharding."
  - title: "API design"
    page: /system-design/case-studies/order-management-system
    why: "The case study's API section covers idempotency keys, asynchronous `202` responses and cursor pagination."
  - title: "Data modelling and storage selection"
    why: "Access patterns first, then the store that fits them."
  - title: "High-level design with reusable building blocks"
    page: /system-design/diagram-patterns
    why: "Load balancers, caches, brokers, replication and sharding, each with its failure modes."
  - title: "Scaling"
    why: "Replication, partitioning, caching and asynchronous processing, chosen from the numbers."
  - title: "Reliability"
    page: /system-design/case-studies/order-management-system
    why: "The failure-handling table shows how to reason about every dependency being slow or down."
  - title: "Trade-offs and follow-up questions"
    page: /microservices/fundamentals/microservices-vs-modular-monolith
    why: "Defending a design matters as much as drawing it; this article models trade-off reasoning."
  - title: "More case studies: URL shortener, rate limiter, notification service"
    why: "Practise the framework on systems with different bottlenecks."
---

Practise each case study out loud with a timer, and spend the first third of the time on requirements and estimates.
