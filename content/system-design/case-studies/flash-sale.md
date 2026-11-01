---
title: "Designing a Flash Sale System"
linkTitle: "Flash Sale"
description: "A system for limited-stock sales that attract millions of users at the same second, designed with the 18-step framework: shielding the core with CDNs, waiting rooms and rate limits, atomic stock reservation in Redis, asynchronous order creation through a queue, reservation expiry, preventing overselling and bots, and keeping the user experience honest."
weight: 17
difficulty: advanced
status: complete
tags: [system-design, case-study, flash-sale, high-concurrency, redis]
keywords: [flash sale system design, high concurrency inventory, prevent overselling, redis decrement lua stock, virtual waiting room, black friday system design, seckill architecture, queue based order processing]
lastReviewed: 2026-11-01
prerequisites:
  - /system-design/introduction
  - /system-design/case-studies/rate-limiter
related:
  - /system-design/building-blocks/caching-strategies
  - /system-design/building-blocks/message-queues-and-delivery-semantics
  - /data-systems/transactions-and-isolation
---

## Overview

A **flash sale** puts a small amount of stock (say 10,000 discounted consoles) on sale at a precise moment. Millions of users press "Buy" within seconds — traffic hundreds of times higher than normal, almost all of it doomed to fail. The system must stay up, **never oversell**, and treat users fairly. ByteByteGo's flash sale design lists the principles; this page applies them with the [18-step framework](/system-design/introduction).

## Design principles (from ByteByteGo)

1. **Less is more:** fewer page elements, fewer database queries, fewer requests, fewer dependencies.
2. **Short critical path:** fewer hops between services.
3. **Async:** use message queues to absorb high TPS.
4. **Isolation:** separate static from dynamic content; isolate processes and databases for the sale items.
5. **Overselling is bad:** decide carefully *when* inventory is decremented.
6. **User experience matters:** never tell users they succeeded and later cancel for lack of stock.

## Requirements and estimates

- 5 million users in the first minute; peak ~**1 million requests/s** at T0.
- 10,000 units; each user may buy one.
- Successful buyers must pay within 10 minutes or the unit returns to stock.

Only 1% of requests can succeed, so the design's main job is to **reject the other 99% cheaply** and as early as possible.

## High-level architecture

{{< interactive-diagram file="flash-sale.html" title="Flash sale architecture" source="diagrams/archify/flash-sale.architecture.json" tool="Archify" >}}

```mermaid {title="Funnel: reject early, reserve atomically, process asynchronously"}
flowchart LR
    U[Millions of users] --> CDN[CDN: static sale page,<br/>countdown, product data]
    U --> GW[Gateway: rate limits,<br/>bot checks, waiting room]
    GW -- "admitted few %" --> SS[Sale service]
    SS -- "Lua: check + DECR" --> R[(Redis stock counter)]
    SS -- "reserved" --> Q[(Kafka order queue)]
    Q --> OW[Order workers]
    OW --> DB[(Orders DB)]
    OW --> PAY[Payment, 10-minute window]
```

## Layer 1: keep traffic away from the core

- **Static everything:** the product page, images and countdown come from the CDN; the "Buy" button activates client-side at T0 (with server-side time checks).
- **Waiting room / token issuance:** admit users in controlled batches; others see a queue position. This converts a spike into a steady flow.
- **Rate limiting and bot defence** at the gateway: per user, per IP, per device; CAPTCHA or proof-of-work challenges; require login and verified accounts in advance.
- **Early sold-out flag:** once stock reaches zero, the gateway and even the CDN-served page switch to "sold out", so requests stop reaching services.

## Layer 2: reserve stock atomically

The database cannot take a million `UPDATE stock SET qty = qty - 1` per second on one row — that row would be the hottest lock in the world. Keep the sale's stock in **Redis** and decrement it with a Lua script, which runs atomically:

```lua
-- KEYS[1] = stock key, KEYS[2] = buyers set; ARGV[1] = userId
if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then return -2 end   -- one per user
local left = tonumber(redis.call('GET', KEYS[1]))
if left <= 0 then return -1 end                                          -- sold out
redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])
return left - 1
```

- Successful reservations equal the stock exactly: **no overselling**.
- To spread load further, split stock into several keys (10 shards × 1,000 units) and pick a shard at random, falling back to others when one is empty.
- Redis must be replicated; consider the risk of losing a reservation on failover and reconcile against the database afterwards.

## Layer 3: create orders asynchronously

A successful reservation publishes a message to Kafka and immediately tells the user "You got one — complete payment within 10 minutes" (honest, because stock is already reserved). Order workers consume at a rate the database can handle and create orders with an idempotency key `(saleId, userId)`.

## Reservation expiry

If payment is not completed within 10 minutes, a delayed job (or a TTL plus a scheduler) cancels the order and **returns the unit** to the Redis counter and makes it available again. Payment and cancellation race: use the order's state machine with conditional updates so a unit cannot be both paid and released.

## Database and isolation

The sale uses a dedicated database (or at least dedicated tables and connection pools), so normal shopping is unaffected. Final stock in the database is reconciled with Redis after the sale.

## Failure handling

- Redis unavailable: stop the sale (fail closed) — overselling is worse than a pause.
- Queue lag: users see "processing"; orders are durable in Kafka.
- Duplicate messages: idempotent order creation.

## Fairness and abuse

Limit one per account, require purchase history or verified phone numbers, detect scripted traffic, and randomise admission from the waiting room rather than rewarding the fastest bots.

## Trade-offs

- Redis-first reservation trades strict durability for throughput; reconciliation closes the gap.
- Waiting rooms add latency but make load predictable.
- Rejecting 99% of users is unavoidable; make rejection fast and clear.

## Interview follow-up questions

**Why not use database row locks?** One hot row serialises all buyers; throughput collapses to a few thousand per second and timeouts cascade.

**How do you ensure exactly 10,000 sales?** Atomic check-and-decrement in one place (Lua in Redis), plus idempotent order creation and reconciliation.

**What if a user pays after their reservation expired?** The order state machine rejects or refunds; payment success after expiry is handled as an exception path.

## Summary

- Reject the 99% early: CDN, waiting room, rate limits, sold-out flags.
- Reserve stock atomically in memory (Redis Lua); never oversell.
- Create orders asynchronously through a queue; expire unpaid reservations.
- Isolate the sale's resources from the rest of the shop.

## Related topics

- [Designing a Rate Limiter](/system-design/case-studies/rate-limiter)
- [Caching Strategies: Redis](/system-design/building-blocks/caching-strategies)
- [Message Queues and Delivery Semantics](/system-design/building-blocks/message-queues-and-delivery-semantics)
- [Transactions and Isolation Levels: lost updates](/data-systems/transactions-and-isolation)

## References

- Alex Xu. *ByteByteGo System Design Handbook*: design a flash sale system.
- Alex Xu. *ByteByteGo System Design Handbook*: optimistic locking; cache miss attack.
