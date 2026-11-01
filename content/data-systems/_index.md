---
title: "Data-Intensive Systems"
description: "How databases, replication, partitioning, transactions, consensus, batch and stream processing really work — the foundations behind every system design decision about data."
weight: 85
---

Most backend systems are limited by data, not by CPU: how much of it there is, how fast it changes, and how many copies must agree. This section explains the machinery under the databases, caches, queues and stream processors that the rest of the handbook uses as building blocks. It follows the structure of Martin Kleppmann's *Designing Data-Intensive Applications*, with each idea connected back to Java, Spring and system design practice.

**Foundations: one machine**

1. [Reliability, Scalability and Maintainability](/data-systems/reliability-scalability-maintainability) — what the goals mean and how to measure load and performance.
2. [Data Models and Query Languages](/data-systems/data-models-and-query-languages) — relational, document and graph models.
3. [Storage Engines: LSM-Trees, B-Trees and Column Stores](/data-systems/storage-engines) — how databases store and find data.
4. [Encoding and Schema Evolution](/data-systems/encoding-and-schema-evolution) — JSON, Protocol Buffers, Avro, and compatibility.

**Distributed data: many machines**

5. [Replication](/data-systems/replication) — leaders, followers, lag, multi-leader and leaderless quorums.
6. [Partitioning (Sharding)](/data-systems/partitioning) — splitting data by key or hash, secondary indexes, rebalancing and routing.
7. [Transactions and Isolation Levels](/data-systems/transactions-and-isolation) — ACID, anomalies, snapshot isolation and serializability.
8. [The Trouble with Distributed Systems](/data-systems/distributed-systems-faults) — partial failures, unreliable networks and clocks, fencing.
9. [Consistency and Consensus](/data-systems/consistency-and-consensus) — linearizability, ordering, two-phase commit, Raft and Paxos.

**Derived data: systems of systems**

10. [Batch Processing](/data-systems/batch-processing) — MapReduce, dataflow engines and joins at scale.
11. [Stream Processing](/data-systems/stream-processing) — logs, change data capture, event sourcing and windows.

The [system design building blocks](/system-design/building-blocks) apply these ideas to caches, load balancers, queues and ID generators, and the [case studies](/system-design/case-studies) apply them end to end.
