---
title: "Domain-Driven Design"
description: "Strategic and tactical DDD, taught with one running order-management example."
weight: 60
---

Domain-Driven Design (Eric Evans, 2003) is an approach to software for complex business domains. It puts the business model — and the language used to discuss it — at the centre of design. It has two halves:

- **Strategic DDD**: where the boundaries are. This covers subdomains, bounded contexts and context maps.
- **Tactical DDD**: how to model inside one boundary. This covers entities, value objects, aggregates and domain events.

Every article uses the same fictional retailer, which has an **Ordering**, **Inventory**, **Payments** and **Shipping** domain. Concepts therefore build on one another instead of resetting with each new toy example.

> [!TRADEOFF] DDD has a cost
> DDD pays off when the domain is complex enough to justify the modelling effort. For a CRUD application with few business rules, a simple layered design is usually the better choice.

Start with [Domain-Driven Design Fundamentals](/domain-driven-design/fundamentals).
