---
title: "Scaling from Zero to Millions of Users"
linkTitle: "Scaling to millions of users"
description: "The step-by-step evolution of a web system from one server to millions of users: separating the database, vertical versus horizontal scaling, load balancers, database replication, caching, CDNs, stateless web tiers, multiple data centres, message queues, observability and sharding — with what each step fixes and costs."
weight: 1
difficulty: beginner
status: complete
tags: [system-design, scalability, architecture, interview-preparation]
keywords: [scale from zero to millions of users, stateless web tier, horizontal scaling, database replication read replicas, cdn, multi data center, message queue decoupling, sharding, system design basics]
lastReviewed: 2026-11-01
prerequisites:
  - /system-design/introduction
related:
  - /data-systems/reliability-scalability-maintainability
  - /system-design/building-blocks/load-balancing
  - /system-design/building-blocks/caching-strategies
---

## Simple definition

Large systems are not designed large on day one. They **evolve**, one bottleneck at a time. This page walks through the standard sequence of steps that take a web application from a single server to millions of users. Each step removes one limit and adds some complexity — and knowing *why* each step exists is what system design interviews test.

> [!TIP] An approach for every design problem
> The handwritten notes in this handbook's sources summarise the attitude: break the problem into simpler modules (top-down), ask about constraints and functionality first, talk about trade-offs (no solution is perfect), and calculate the impact of each choice on the system.

## The steps at a glance

| Step | Fixes | Costs |
|---|---|---|
| 1. Single server | — | Single point of failure, no scaling |
| 2. Separate database | Web and data tiers scale independently | One more component |
| 3. Load balancer + several web servers | Web tier availability and capacity | Servers must be stateless |
| 4. Database replication | Read scaling, database availability | Replication lag |
| 5. Cache | Database load, latency | Invalidation, consistency |
| 6. CDN | Static content latency worldwide | Cost, cache invalidation |
| 7. Stateless web tier | Auto-scaling, any server serves any user | Shared session store |
| 8. Multiple data centres | Regional outages, global latency | Data replication across regions, traffic routing |
| 9. Message queue | Decoupling, absorbing bursts | Asynchrony, eventual consistency |
| 10. Logging, metrics, automation | Operability at scale | Tooling effort |
| 11. Database sharding | Write scaling, data volume | Cross-shard queries, resharding |

## 1. A single server

Everything — web app, database, cache — runs on one machine. Users resolve the domain through **DNS** to its IP address and send HTTP requests. Fine for a prototype; any failure takes down everything, and the only scaling option is a bigger machine.

## 2. Separate the database

Move the database to its own server. Now the web tier (traffic) and data tier (storage) scale independently. Choose a relational or non-relational database by data shape and access pattern; see [Choosing a Database](/system-design/building-blocks/choosing-a-database).

## 3. Load balancer and horizontal scaling

**Vertical scaling** (bigger CPU, more RAM) is simple but has a ceiling and no redundancy. **Horizontal scaling** adds more servers. A **load balancer** distributes traffic across them, using a public IP while servers keep private IPs. If a server dies, the load balancer routes around it; under more load, add servers. See [Load Balancing](/system-design/building-blocks/load-balancing).

## 4. Database replication

A single database is now the bottleneck and the single point of failure. With **leader-follower replication**, the leader handles writes and **read replicas** serve reads (most applications read far more than they write). If a replica fails, reads go elsewhere; if the leader fails, a replica is promoted. Watch for **replication lag**: users may not see their own writes. See [Replication](/data-systems/replication).

```mermaid {title="After steps 1–6: load balancer, stateless web tier, cache, CDN and replicated database"}
flowchart LR
    U((Users)) --> DNS[DNS]
    U --> CDN[CDN<br/>static assets]
    U --> LB[Load balancer]
    LB --> W1[Web server 1]
    LB --> W2[Web server 2]
    W1 --> C[(Cache<br/>Redis)]
    W2 --> C
    W1 --> P[(Primary DB)]
    W2 --> P
    W1 -.reads.-> R1[(Replica)]
    W2 -.reads.-> R2[(Replica)]
    P -- replication --> R1
    P -- replication --> R2
```

## 5. Cache

A cache keeps frequently read data in memory (Redis, Memcached), cutting response time and database load. Decide the read and write strategy (cache-aside, write-through...), expiry (TTL), eviction (LRU), consistency with the database, and how to avoid a single point of failure. See [Caching Strategies](/system-design/building-blocks/caching-strategies).

## 6. Content delivery network (CDN)

A CDN is a network of geographically distributed servers caching **static content**: images, JavaScript, CSS, videos. Users fetch assets from the nearest edge. Consider cost (pay per transfer — do not cache rarely used assets), TTLs, fallback if the CDN fails, and invalidation (versioned file names such as `app.3f9a1c.js`).

## 7. A stateless web tier

To scale web servers freely, take **state out of them**. Session data in server memory forces sticky sessions (each user tied to one server), which complicates scaling and failover. Store sessions in a shared store (Redis, a database) or use self-contained tokens (JWT). Now any server can serve any request, and **auto-scaling** can add and remove servers by load. See [OAuth 2.0, OIDC and JWT](/spring/security/oauth2-oidc-and-jwt).

## 8. Multiple data centres

Serve users from several regions. **GeoDNS** routes each user to the nearest data centre; if one fails, traffic is redirected to another. Challenges: traffic redirection, **data synchronisation** across regions (asynchronous multi-region replication, conflicts), and testing and deployment across all locations.

## 9. Message queue

A durable **message queue** decouples producers from consumers: the web tier publishes "resize this photo" jobs, and a pool of workers processes them at its own pace. Producers stay fast during bursts, workers scale independently, and failures in one side do not stop the other. See [Message Queues and Delivery Semantics](/system-design/building-blocks/message-queues-and-delivery-semantics).

## 10. Logging, metrics and automation

At scale, you need **logs** (centralised and searchable), **metrics** (host: CPU, memory, disk; aggregate: tier performance; business: daily active users, revenue), **tracing**, and **automation** (CI/CD, infrastructure as code). See [Observability](/microservices/operations/observability-micrometer-opentelemetry) and [Deployment Strategies](/system-design/building-blocks/deployment-strategies).

## 11. Database sharding

Eventually the write volume or data size exceeds one database leader. **Sharding** splits the data across several databases by a **shard key** (user ID). Choose a key that spreads data evenly; handle **resharding** (consistent hashing helps), **hot keys** (the celebrity problem) and **joins across shards** (often by denormalising). Sharding is the most complex step, so do it last. See [Partitioning](/data-systems/partitioning).

> [!KEY] Estimate before you scale
> Each step should be justified by numbers. ByteByteGo's own example: a geospatial index of 200 million businesses needs about 2 GB, which fits on one server; read replicas solve the read load and sharding would only add complexity. See the estimation section of [Introduction to System Design](/system-design/introduction).

## Summary checklist

- Keep the web tier stateless.
- Build redundancy at every tier.
- Cache data as much as you can.
- Support multiple data centres.
- Host static assets in a CDN.
- Scale the data tier by sharding when replication is not enough.
- Split tiers into individual services when teams and load require it.
- Monitor everything and automate deployment.

## Common mistakes

> [!MISTAKE] Jumping to the final architecture
> Starting an interview answer with sharded databases and Kafka, without numbers, signals that you are reciting rather than designing. Start simple, identify the bottleneck, and add the step that removes it.

- **Sticky sessions** used as a substitute for a stateless tier.
- **Caching without an invalidation plan.**
- **Sharding before replication and caching are exhausted.**

## Quick check

**Why must the web tier be stateless to scale horizontally?**
So that any server can handle any request; otherwise users are tied to specific servers, which breaks load balancing, auto-scaling and failover.

**What problem do read replicas solve, and what do they introduce?**
They scale reads and add availability; they introduce replication lag.

**What does a CDN cache?**
Static content (images, scripts, stylesheets, video) at edge locations near users.

**Why add a message queue?**
To decouple producers from consumers so each can scale and fail independently, and to absorb traffic bursts.

**Why is sharding usually the last step?**
It adds the most complexity: shard key choice, resharding, hot keys and cross-shard queries.

## Related topics

- [Reliability, Scalability and Maintainability](/data-systems/reliability-scalability-maintainability)
- [Load Balancing and Proxies](/system-design/building-blocks/load-balancing)
- [Caching Strategies](/system-design/building-blocks/caching-strategies)
- [Introduction to System Design](/system-design/introduction)

## References

- Alex Xu. *ByteByteGo System Design Handbook*: how to scale a website to support millions of users; back-of-the-envelope estimation.
- Alex Xu. *System Design Interview – An Insider's Guide*. Chapter 1: Scale from zero to millions of users.
- System Design Notes (handwritten, LinkedIn): system design basics.
