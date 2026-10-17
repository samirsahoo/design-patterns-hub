---
title: "Glossary"
description: "Short, precise definitions of the terms used across the handbook, with links to the articles that explain them in depth."
toc: false
status: complete
lastReviewed: 2026-10-17
tags: [reference]
---

Definitions are deliberately short. Follow the links for the full explanation, examples and trade-offs.

### A

**ADR (Architecture Decision Record)**
: A short document recording one significant architecture decision: its context, the options, the decision and its consequences. See [Introduction to Software Architecture](/software-architecture/introduction#architecture-decision-records).

**Adapter**
: A structural GoF pattern that converts one interface into another that clients expect. See [Adapter Pattern](/design-patterns/structural/adapter).

**Aggregate / aggregate root**
: In DDD, a cluster of objects changed as one unit through a single entry point (the root), which enforces the cluster's invariants. See [DDD Fundamentals](/domain-driven-design/fundamentals#aggregate-and-aggregate-root).

**Anti-Corruption Layer (ACL)**
: A translation layer that protects a bounded context's model from another context's model. See [What Is a Bounded Context?](/domain-driven-design/strategic/bounded-context#the-anti-corruption-layer).

**At-least-once delivery**
: A messaging guarantee that every message is delivered, possibly more than once. Consumers must be idempotent.

### B

**Bounded context**
: An explicit boundary within which a domain model and its language are consistent. See [What Is a Bounded Context?](/domain-driven-design/strategic/bounded-context).

**Builder**
: A creational GoF pattern that constructs a complex object step by step. See [Builder Pattern](/design-patterns/creational/builder).

### C

**Cache-aside**
: A caching strategy in which the application reads from the cache, and on a miss loads from the database and populates the cache. See [Diagram Building Blocks](/system-design/diagram-patterns#cache-cache-aside).

**Circuit breaker**
: A reliability pattern that stops calling a failing dependency for a period, failing fast instead of waiting for timeouts.

**Cohesion**
: How strongly the elements inside a module belong together. High cohesion is desirable.

**Composition root**
: The single place where an application's object graph is assembled. See [Dependency Injection in Java](/spring/core/dependency-injection#the-composition-root).

**Context map**
: A diagram and description of bounded contexts and the relationships between them. See [What Is a Bounded Context?](/domain-driven-design/strategic/bounded-context#context-mapping).

**Coupling**
: The degree to which one module depends on another. Low coupling is desirable.

**CQRS (Command Query Responsibility Segregation)**
: Using separate models for updating data (commands) and reading it (queries).

### D

**Dependency injection (DI)**
: Supplying an object's dependencies from outside instead of having it create them. See [Dependency Injection in Java](/spring/core/dependency-injection).

**Dependency Inversion Principle (DIP)**
: High-level policy should not depend on low-level details; both should depend on abstractions. See [SOLID Principles](/design-patterns/solid-principles#d--dependency-inversion-principle).

**Distributed monolith**
: A system deployed as services that remain tightly coupled — sharing databases or deploying in lockstep. See [Microservices versus Modular Monolith](/microservices/fundamentals/microservices-vs-modular-monolith#common-mistakes).

**Domain event**
: A record of something that happened in the domain that domain experts care about, named in the past tense, such as `OrderPlaced`.

### E

**Entity**
: In DDD, an object defined by a stable identity rather than by its attributes.

**Event sourcing**
: Storing the sequence of state-changing events as the system of record, and deriving current state by replaying them.

**Eventual consistency**
: A guarantee that, if no new updates are made, all replicas or services will eventually agree on the same value.

### F

**Facade**
: A structural GoF pattern that provides one simple interface to a complex subsystem. See [the wrapper patterns comparison](/comparisons/adapter-decorator-proxy-facade).

**Factory Method**
: A creational GoF pattern in which subclasses decide which class to instantiate. Not the same as a simple factory or a static factory method. See [Factory Method Pattern](/design-patterns/creational/factory-method).

### G

**GoF (Gang of Four)**
: Gamma, Helm, Johnson and Vlissides, authors of *Design Patterns* (1994), which catalogued 23 patterns. See [the overview](/design-patterns/gof-overview).

### H

**Hexagonal Architecture (Ports and Adapters)**
: An architecture style in which the business core defines ports and technology-specific adapters connect to them; dependencies point inwards. See [Layered versus Hexagonal Architecture](/hexagonal-architecture/layered-vs-hexagonal).

### I

**Idempotency**
: The property that performing an operation several times has the same effect as performing it once. Essential for safe retries. See [the order management case study](/system-design/case-studies/order-management-system#failure-handling).

**Invariant**
: A business rule that must always hold, such as "an order has at least one line". Aggregates enforce invariants.

**Inversion of Control (IoC)**
: A principle in which a framework or caller, rather than your code, controls the flow or the choice of collaborators. DI is one form of IoC.

### M

**Modular monolith**
: A single deployable application divided into business-aligned modules with enforced boundaries. See [Microservices versus Modular Monolith](/microservices/fundamentals/microservices-vs-modular-monolith).

### O

**Open/Closed Principle (OCP)**
: Software entities should be open for extension but closed for modification. See [SOLID Principles](/design-patterns/solid-principles#o--openclosed-principle).

### P

**Port (inbound / outbound)**
: In Hexagonal Architecture, an interface owned by the application core. Inbound ports are how the outside world drives the application; outbound ports are what the application needs from the outside world.

### Q

**Quality attribute**
: A measurable property of a system, such as performance, availability or modifiability. Quality attributes drive architecture. See [Introduction to Software Architecture](/software-architecture/introduction#functional-requirements-and-quality-attributes).

### R

**Read replica**
: A copy of a database that serves reads, updated by replication from the primary.

**Repository (DDD)**
: An abstraction that gives the illusion of an in-memory collection of aggregates.

### S

**Saga**
: A sequence of local transactions across services, with compensating actions to undo earlier steps if a later step fails. See [the order management case study](/system-design/case-studies/order-management-system#data-flow).

**Sharding**
: Splitting data across several databases by a key, to scale writes and storage.

**SOLID**
: Five object-oriented design principles: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation and Dependency Inversion. See [SOLID Principles](/design-patterns/solid-principles).

**Strategy**
: A behavioral GoF pattern that puts interchangeable algorithms behind one interface. See [Strategy Pattern](/design-patterns/behavioral/strategy).

### T

**Transactional outbox**
: Writing outgoing messages to an outbox table in the same transaction as the state change, then publishing them separately, so that state and messages never diverge. See [the order management case study](/system-design/case-studies/order-management-system#the-transactional-outbox).

### U

**Ubiquitous language**
: The shared vocabulary of developers and domain experts, used consistently in conversation and in code. See [DDD Fundamentals](/domain-driven-design/fundamentals#ubiquitous-language).

### V

**Value object**
: In DDD, an immutable object defined only by its attributes, with no identity, such as `Money`.
