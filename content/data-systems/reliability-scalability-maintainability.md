---
title: "Reliability, Scalability and Maintainability"
linkTitle: "Reliability, scalability, maintainability"
description: "The three goals of every data system, made measurable: faults versus failures, hardware, software and human errors; describing load with parameters such as fan-out; measuring performance with percentiles and tail latency; vertical versus horizontal scaling; and operability, simplicity and evolvability."
weight: 1
difficulty: intermediate
status: complete
tags: [data-systems, reliability, scalability, performance, system-design]
keywords: [reliability scalability maintainability, fault vs failure, tail latency p99, percentiles response time, load parameters fan-out, scale up vs scale out, operability simplicity evolvability, ddia chapter 1]
lastReviewed: 2026-11-01
prerequisites:
  - /system-design/introduction
related:
  - /system-design/building-blocks/scaling-from-zero-to-millions
  - /microservices/operations/observability-micrometer-opentelemetry
  - /microservices/reliability/circuit-breakers-and-resilience4j
---

## Simple definition

Applications today are usually **data-intensive** rather than compute-intensive: the hard problems are the amount of data, its complexity and how fast it changes. Such applications are assembled from standard building blocks — databases, caches, search indexes, stream processors, batch jobs — and the application code that glues them together.

Three properties describe whether that assembled system is good:

- **Reliability:** it keeps working correctly even when things go wrong.
- **Scalability:** it has reasonable ways to cope with growth.
- **Maintainability:** many different people can work on it productively over time.

These words are often used vaguely ("it's scalable"). This page makes each one concrete and measurable.

## Reliability

A system is reliable if it performs the function the user expects, tolerates user mistakes, performs well enough under the expected load, and prevents unauthorised access — **even when faults occur**.

> [!KEY] Fault versus failure
> A **fault** is one component deviating from its specification (a disk dies, a process crashes). A **failure** is the system as a whole stopping providing the service. Reliability engineering is about building **fault-tolerant** systems so faults do not turn into failures. You cannot prevent all faults, so you design for them.

| Kind of fault | Examples | Typical defences |
|---|---|---|
| **Hardware** | Disk crashes (a large cluster loses disks daily), RAM errors, power and network outages | Redundancy (RAID, replicas, multiple zones); design so the system tolerates losing whole machines, which also allows rolling upgrades |
| **Software** | A bug triggered by unusual input on every node at once, a runaway process, a slow dependency, cascading failures | Careful assumptions, testing, process isolation, crash-and-restart, measuring and alerting on invariants; [timeouts, retries, circuit breakers](/microservices/reliability/circuit-breakers-and-resilience4j) |
| **Human** | Configuration errors (a leading cause of outages), bad deployments | Well-designed APIs and admin tools, sandboxes, thorough tests, quick rollback and gradual rollouts, monitoring, training |

Software faults are the dangerous ones because they are **correlated**: a hardware fault hits one machine, but a bug hits every machine running the same code at the same moment.

Deliberately injecting faults — killing processes, adding latency (chaos engineering) — exercises the fault-tolerance machinery so it works when needed.

## Scalability

"Is it scalable?" is meaningless on its own. The useful question is: **if load grows in a particular way, what are our options for coping?** That requires describing load and performance.

### Describing load

Load is described with **load parameters** chosen for the system: requests per second, read/write ratio, number of concurrent users, cache hit rate, or the size and shape of data.

The classic example is a social network's home timeline. Users post about 4.6k tweets/s on average (12k at peak), and timelines are read about 300k times/s. The difficult parameter is **fan-out**: each user follows many accounts and is followed by many.

- **Approach 1, pull on read:** store posts once; when a user opens the timeline, query all followed accounts and merge. Cheap writes, expensive reads.
- **Approach 2, push on write:** keep a cached timeline per user; when someone posts, insert the post into every follower's timeline. Cheap reads, expensive writes — and a celebrity with 30 million followers turns one post into 30 million writes.
- **Hybrid:** push for most users, pull for celebrities at read time.

The right design depends on the distribution of followers per user — a load parameter.

### Describing performance

Two questions: when load increases and resources stay the same, how is performance affected? And how much must resources grow to keep performance unchanged?

For online systems the key metric is **response time**, and it is a *distribution*, not a number:

| Statistic | Meaning | Use |
|---|---|---|
| Mean | Arithmetic average | Hides outliers; poor for user experience |
| **Median (p50)** | Half of requests are faster | The "typical" request |
| **p95, p99, p999** | 95%, 99%, 99.9% are faster | **Tail latency**: what your slowest users see |

Tail latencies matter because the slowest requests often belong to the most valuable customers (those with the most data), and because of **tail latency amplification**: if a page calls 100 back ends in parallel, the page is as slow as the slowest call. With each back end at p99 = 1 s, about 63% of pages see at least one 1 s call (1 − 0.99¹⁰⁰).

> [!TIP] Measure on the client side, and aggregate correctly
> Queueing delay is often the biggest part of response time, and it is invisible to a server that measures only processing time. Never average percentiles across servers; merge histograms (Micrometer and Prometheus histograms do this). See [Observability](/microservices/operations/observability-micrometer-opentelemetry).

Response-time objectives are written as **SLOs** ("p99 < 300 ms over 30 days") and contracts as **SLAs**.

### Coping with load

| | Scale up (vertical) | Scale out (horizontal) |
|---|---|---|
| How | Bigger machine | More machines, load distributed |
| Pros | Simple; no distributed-systems problems | No hard ceiling; fault tolerance |
| Cons | Expensive at the top end; single point of failure | Complexity: partitioning, consistency, coordination |

- **Stateless services** scale out easily; **stateful data systems** are the hard part.
- **Elastic** systems add resources automatically; manual scaling is simpler and more predictable.
- There is no generic scalable architecture. A system handling 100,000 requests/s of 1 kB each looks nothing like one handling 3 requests/minute of 2 GB each. Design around the load parameters that matter, and do not build for scale you do not have.

See [Scaling from Zero to Millions of Users](/system-design/building-blocks/scaling-from-zero-to-millions) for the usual progression.

## Maintainability

Most of the cost of software is ongoing maintenance: fixing bugs, operating it, adapting to new requirements, paying down technical debt. Three design principles reduce that cost:

| Principle | Goal | Practices |
|---|---|---|
| **Operability** | Make life easy for operations | Good monitoring, automation and integration with standard tools, no dependence on individual machines, documentation and runbooks, sensible defaults with overrides, predictable behaviour |
| **Simplicity** | Make it easy for new engineers to understand | Remove **accidental complexity** (complexity from the implementation, not the problem) with good **abstractions**: hide detail behind clean interfaces |
| **Evolvability** | Make change easy | Agile practices, refactoring, tests; loose coupling and clear module boundaries (see [hexagonal architecture](/hexagonal-architecture/layered-vs-hexagonal)) |

## Common mistakes

> [!MISTAKE] Reporting average latency
> An average of 80 ms can hide a p99 of 4 seconds. Report percentiles from histograms, and set SLOs on tail latency.

- **Treating scalability as a yes/no property** instead of reasoning about specific load parameters.
- **Designing for imaginary scale**, paying the complexity of distribution with no need.
- **Ignoring correlated software faults** and testing only for single-machine failures.
- **No rollback path** for deployments and configuration changes.

## Quick check

**What is the difference between a fault and a failure?**
A fault is one component deviating from its spec; a failure is the whole system no longer providing the required service. Fault tolerance prevents faults from causing failures.

**Why are software faults often worse than hardware faults?**
They are correlated: the same bug can affect every node at the same time.

**Why use percentiles instead of the mean for response time?**
Response time is a skewed distribution; percentiles show the typical case (p50) and the tail (p99), which the mean hides.

**What is tail latency amplification?**
When a request depends on many back-end calls, the chance that at least one is slow rises quickly, so overall latency is dominated by the tail.

**What are the three maintainability principles?**
Operability, simplicity and evolvability.

## Summary

- Reliability: tolerate hardware, software and human faults so they do not become failures.
- Scalability: describe load with parameters, measure performance with percentiles, then decide how to scale.
- Maintainability: design for operators, simplicity and change.
- No architecture is universally scalable; it is scalable *for* particular load parameters.

## Related topics

- [Scaling from Zero to Millions of Users](/system-design/building-blocks/scaling-from-zero-to-millions)
- [Observability with Micrometer and OpenTelemetry](/microservices/operations/observability-micrometer-opentelemetry)
- [Circuit Breakers and Resilience4j](/microservices/reliability/circuit-breakers-and-resilience4j)
- [Introduction to System Design](/system-design/introduction)

## References

- Martin Kleppmann. *Designing Data-Intensive Applications*. O'Reilly, 2017. Chapter 1: Reliable, Scalable, and Maintainable Applications.
- Jeffrey Dean, Luiz André Barroso. "The Tail at Scale." *Communications of the ACM* 56(2), 2013.
- Betsy Beyer et al. *Site Reliability Engineering*. O'Reilly, 2016. Chapter 4: Service Level Objectives.
