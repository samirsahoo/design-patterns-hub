---
title: "Message Queues and Delivery Semantics"
linkTitle: "Queues and delivery semantics"
description: "Why systems put queues between components, point-to-point queues versus publish-subscribe topics, message queues versus event streaming platforms, at-most-once, at-least-once and exactly-once delivery with idempotent consumers, ordering, dead-letter queues, back-pressure, and why Kafka is fast (sequential I/O and zero copy)."
weight: 8
difficulty: intermediate
status: complete
tags: [system-design, messaging, kafka, rabbitmq, asynchronous]
keywords: [message queue system design, at most once at least once exactly once, idempotent consumer, kafka vs rabbitmq, message queue vs event streaming, dead letter queue, why is kafka fast zero copy, pull vs push consumers, back pressure]
lastReviewed: 2026-11-01
prerequisites:
  - /system-design/building-blocks/scaling-from-zero-to-millions
related:
  - /data-systems/stream-processing
  - /microservices/communication/event-driven-spring-cloud-stream
  - /system-design/case-studies/payment-system
---

## Simple definition

A **message queue** is a durable buffer between components: a **producer** puts a message on it and continues; a **consumer** takes it off and processes it later. The queue makes communication **asynchronous**.

The handwritten notes summarise why this matters: in small systems writes are fast, but in large systems a request may take a long time, and with many clients the server cannot keep up. With a queue, the client gets an acknowledgement (a receipt) immediately, the task is processed when a worker is free, and the client can check the result later.

## Why use a queue

| Benefit | Example |
|---|---|
| **Decoupling** | The order service does not know which services react to an order |
| **Absorbing bursts** (load levelling) | A flash sale's spike waits in the queue instead of overloading workers |
| **Independent scaling** | Add consumers when the queue grows |
| **Resilience** | If consumers are down, messages wait; producers keep working |
| **Asynchronous user experience** | Upload returns immediately; thumbnails are generated later |

The costs: eventual consistency, harder debugging and tracing, duplicate and out-of-order delivery to handle, and another critical component to operate.

## Queues and topics

| Model | Delivery | Use |
|---|---|---|
| **Point-to-point queue** | Each message to **one** of the competing consumers | Distributing work (jobs, tasks) |
| **Publish-subscribe topic** | Each message to **every** subscriber (group) | Broadcasting events to several services |

Kafka combines both: each **consumer group** receives every message (pub/sub), and within a group the partitions are shared among consumers (competing consumers).

## Message queues versus event streaming platforms

| | Message queue (RabbitMQ, ActiveMQ, SQS) | Event streaming platform (Kafka, Pulsar, Kinesis) |
|---|---|---|
| After consumption | Message deleted | Retained for a configured period |
| Replay | No | Yes, by resetting offsets |
| Routing | Rich (exchanges, routing keys, priorities, delays) | By topic and partition key |
| Ordering | Per queue, weakened by redelivery and competing consumers | Per partition |
| Throughput | High | Very high (millions/s) |
| Consumer model | Broker pushes, tracks acks per message | Consumers pull, track offsets |
| Best for | Task distribution, request buffering, complex routing | Event logs, stream processing, CDC, many independent consumers |

See [Stream Processing](/data-systems/stream-processing) for the log-based model in depth.

## Delivery semantics

Networks and processes fail, so messages can be lost or delivered twice. Systems choose what they guarantee:

| Semantic | Promise | How | Use cases |
|---|---|---|---|
| **At-most-once** | Never redelivered; may be lost | Acknowledge (commit offset) *before* processing; no retries | Metrics, monitoring, logs where small loss is acceptable |
| **At-least-once** | Never lost; may be duplicated | Acknowledge *after* processing; retry on failure | Most business events, with idempotent consumers |
| **Exactly-once** | Effect happens once | At-least-once delivery + idempotency or atomic commit | Payments, trading, accounting |

```mermaid {title="At-least-once: a crash after processing causes a duplicate"}
sequenceDiagram
    participant B as Broker
    participant C as Consumer
    participant DB as Database
    B->>C: message 42
    C->>DB: apply effect
    Note over C: crash before ack
    B->>C: message 42 (redelivered)
    C->>DB: apply effect again ✖ duplicate
```

### Exactly-once is at-least-once plus idempotence

ByteByteGo's payment example states it neatly: an operation is executed exactly once if it is executed **at least once** (retries guarantee that) and **at most once** at the same time (an **idempotency check** guarantees that).

Ways to make consumers idempotent:

- **Deduplication table** (inbox): store each processed message ID in the same transaction as the effect; skip IDs already seen.
- **Natural idempotence:** "set status to SHIPPED" is idempotent; "add 10 to balance" is not — use upserts and absolute values.
- **Unique constraints:** a unique index on `(order_id, event_type)` rejects the duplicate insert.
- **Version checks:** apply an update only if the stored version is older.

Kafka also offers **idempotent producers** (no duplicates from producer retries) and **transactions** (atomically write output messages and consumer offsets), which give exactly-once *within* Kafka. Effects outside Kafka still need idempotency.

## Ordering

- Ordering is guaranteed **per partition or per queue**, not globally. Choose the **message key** so that messages needing order (all events of one order) go to the same partition.
- Retries and parallel consumers can reorder messages; include a sequence number or version so consumers can detect and ignore stale events.

## Failure handling

- **Retries with backoff** for transient errors.
- **Dead-letter queue (DLQ):** after *n* failures, move the message aside so it does not block the queue (the "poison message" problem); alert and inspect.
- **Visibility timeout / redelivery delay:** a message taken but not acknowledged becomes visible again after a timeout.
- **Back-pressure:** bounded queues, consumer lag monitoring, and producer throttling when consumers cannot keep up.

## Push versus pull

| | Push (broker sends) | Pull (consumer fetches) |
|---|---|---|
| Latency | Low | Low with long polling |
| Flow control | Broker must avoid overwhelming consumers (prefetch limits) | Natural: consumers fetch at their own pace |
| Batching | Harder | Easy; consumers fetch many messages at once |
| Examples | RabbitMQ (with prefetch) | Kafka, SQS |

## Why is Kafka fast?

Two techniques, as ByteByteGo explains:

1. **Sequential I/O.** Kafka appends to log files and reads them sequentially. Sequential disk access is fast (hundreds of MB/s on HDDs, much more on SSDs) and benefits from the OS page cache and read-ahead.
2. **Zero copy.** Without it, sending data to a consumer copies it disk → OS cache → application buffer → socket buffer → network card. With `sendfile()`, the OS copies directly from the page cache to the network card, skipping the application and halving the copies (around 65% less time in their example).

Batching, compression of message batches, and partitioning across brokers add to the throughput.

## Common mistakes

> [!MISTAKE] Assuming exactly-once delivery from the broker
> Most brokers deliver at least once. If the consumer is not idempotent, retries, rebalances and redeliveries will eventually apply effects twice — double emails, double charges.

- **Publishing to the broker and writing the database separately** (dual write); use a transactional outbox.
- **No DLQ**, so one bad message blocks a partition forever.
- **Global ordering assumptions** across partitions.
- **Unbounded queues without lag alerts**, hiding a failing consumer for hours.

## Quick check

**What is the difference between at-most-once and at-least-once delivery?**
At-most-once acknowledges before processing and may lose messages; at-least-once acknowledges after processing and may deliver duplicates.

**How do you achieve exactly-once processing?**
At-least-once delivery combined with idempotent consumers (deduplication by message ID, unique constraints, idempotent writes), or atomic commit of outputs and offsets within one system.

**How does Kafka preserve order?**
Within a partition; messages with the same key go to the same partition.

**What is a dead-letter queue?**
A queue for messages that repeatedly fail processing, so they do not block others and can be inspected.

**Why is Kafka fast?**
Sequential disk I/O on append-only logs, zero-copy transfer from page cache to network, batching and compression.

## Related topics

- [Stream Processing](/data-systems/stream-processing)
- [Event-Driven Microservices with Spring Cloud Stream](/microservices/communication/event-driven-spring-cloud-stream)
- [Designing a Payment System](/system-design/case-studies/payment-system)
- [Diagram Building Blocks: message broker](/system-design/diagram-patterns#message-broker)

## References

- Alex Xu. *ByteByteGo System Design Handbook*: at-most once, at-least once, and exactly once; why is Kafka fast; avoid double charge; pull vs push models.
- System Design Notes (handwritten, LinkedIn): queues.
- [Apache Kafka documentation: message delivery semantics](https://kafka.apache.org/documentation/#semantics).
