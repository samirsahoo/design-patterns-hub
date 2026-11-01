---
title: "CAP Theorem and PACELC"
linkTitle: "CAP and PACELC"
description: "What the CAP theorem actually says (and what it does not), why 'pick two of three' is misleading, CP versus AP systems during a partition, the latency-consistency trade-off that PACELC adds for normal operation, tunable consistency, and how to use these ideas in a system design discussion."
weight: 5
difficulty: intermediate
status: complete
tags: [system-design, cap-theorem, consistency, availability, distributed-systems]
keywords: [cap theorem explained, consistency availability partition tolerance, cp vs ap database, pacelc theorem, latency vs consistency tradeoff, tunable consistency cassandra, eventual consistency, strong consistency]
lastReviewed: 2026-11-01
prerequisites:
  - /data-systems/replication
related:
  - /data-systems/consistency-and-consensus
  - /system-design/building-blocks/choosing-a-database
  - /data-systems/distributed-systems-faults
---

## Simple definition

The **CAP theorem** (Eric Brewer, 2000; proved by Gilbert and Lynch, 2002) concerns a distributed data store with replicas. It names three properties:

- **Consistency (C):** every read receives the most recent write or an error. Here this means **linearizability**, not the C of ACID.
- **Availability (A):** every request to a non-failed node receives a non-error response (without a guarantee that it is the latest write).
- **Partition tolerance (P):** the system keeps operating even when the network drops or delays messages between nodes.

The theorem says: **when a network partition happens, a system must choose between consistency and availability.** It cannot provide both.

## Why "pick two of three" is misleading

The popular summary "choose two of C, A and P" suggests you could choose "CA" and give up partition tolerance. But partitions are not a choice: networks *will* fail (see [The Trouble with Distributed Systems](/data-systems/distributed-systems-faults)). A system that is not partition tolerant simply breaks unpredictably when one occurs.

The useful reading is:

> **If there is a Partition, choose Consistency or Availability.** When there is no partition, you can have both.

```mermaid {title="During a partition: refuse (CP) or answer possibly stale (AP)"}
flowchart LR
    subgraph DC1["Side 1"]
        N1[(Replica 1<br/>x = 2, newest)]
    end
    subgraph DC2["Side 2"]
        N2[(Replica 2<br/>x = 1, stale)]
    end
    N1 -. "network partition ✖" .- N2
    C[Client reads x<br/>from side 2] --> N2
    N2 --> CP["CP: return an error / timeout<br/>(cannot confirm latest)"]
    N2 --> AP["AP: return x = 1<br/>(available, possibly stale)"]
```

The handwritten notes phrase it well: we cannot build a data store that is continually available, sequentially consistent and tolerant of any partition failure at the same time.

## CP versus AP

| | CP (consistency over availability) | AP (availability over consistency) |
|---|---|---|
| During a partition | Some nodes refuse requests (or the minority side stops) | All nodes answer; answers may be stale; conflicts reconciled later |
| Typical mechanisms | Single leader, consensus (Raft/Paxos), majority quorums | Leaderless or multi-leader replication, sloppy quorums, last-write-wins or CRDTs |
| Examples | ZooKeeper, etcd, HBase, Spanner, MongoDB with majority writes and reads | Cassandra and DynamoDB (default settings), Riak, CouchDB, DNS |
| Good for | Locks, leader election, balances, inventory counts, uniqueness | Shopping carts, likes, session data, product views, feeds |

Many databases are **tunable**: Cassandra with `QUORUM` reads and writes behaves more like CP for those operations; with `ONE` it is AP.

## What CAP does not say

- It considers only **one** kind of fault (network partitions) and one consistency model (linearizability). Real systems face many faults and offer many intermediate guarantees (causal, read-your-writes).
- It says nothing about **latency**, which matters all the time, not only during rare partitions.
- "Availability" in CAP is a strict, formal definition; a CP system can still be highly available in practice (a Raft cluster loses availability only on the minority side of a partition).

Because of these limits, Kleppmann suggests CAP is best treated as historically important but too narrow for designing systems. PACELC fixes the biggest gap.

## PACELC: latency versus consistency

Daniel Abadi's **PACELC** extends CAP:

> If there is a **P**artition, choose **A**vailability or **C**onsistency; **E**lse (normal operation), choose **L**atency or **C**onsistency.

Even with a perfect network, strong consistency requires coordination between replicas — waiting for a leader, a quorum or synchronous replication — and that costs latency. ByteByteGo's "trade-off between latency and consistency" makes the same point for replication: the more replicas must acknowledge before a response, the more consistent and the slower it is.

| System | Partition: A or C | Else: L or C | Class |
|---|---|---|---|
| Cassandra, DynamoDB, Riak (default) | A | L | **PA/EL** |
| Spanner, etcd, ZooKeeper, HBase | C | C | **PC/EC** |
| MongoDB (default settings) | A* | C | PA/EC |
| PNUTS (Yahoo) | C | L | PC/EL |

\* depends on read and write concern settings.

## Using CAP and PACELC in a design discussion

1. **Ask what each piece of data needs.** A payment ledger and a "likes" counter in the same system need different guarantees.
2. **For data requiring correctness under concurrency** (balances, stock, unique usernames, locks), choose a CP store or a single-leader database with linearizable operations, and accept errors or waits during failures.
3. **For data where staleness is acceptable** (feeds, recommendations, view counts), choose AP or eventually consistent replicas for latency and availability, and design the UX for staleness.
4. **State the latency cost** of strong consistency, especially across regions (a cross-continent quorum adds ~100 ms).
5. **Mention mitigations:** read-your-writes routing, idempotent retries, conflict resolution (CRDTs), and reconciliation jobs.

## Common mistakes

> [!MISTAKE] Claiming a system is "CA"
> Any system communicating over a network must handle partitions. "CA" usually means "a single-node database", which is not distributed at all.

- **Equating CAP consistency with ACID consistency.**
- **Labelling a whole product CP or AP** when behaviour depends on configuration and operation.
- **Ignoring latency** and discussing only the rare partition case.

## Quick check

**What does the CAP theorem state?**
During a network partition, a distributed data store must choose between consistency (linearizability) and availability.

**Why can't you choose CA?**
Network partitions are unavoidable in distributed systems; partition tolerance is not optional.

**What does PACELC add?**
The trade-off during normal operation: even without partitions, stronger consistency costs latency.

**Give one CP and one AP example.**
CP: ZooKeeper or etcd. AP: Cassandra or DynamoDB with default settings.

**Which data in an e-commerce system needs CP behaviour?**
Inventory decrements, payments and balances, and unique identifiers; product reviews and recommendations can be AP.

## Related topics

- [Consistency and Consensus](/data-systems/consistency-and-consensus)
- [Replication](/data-systems/replication)
- [Choosing a Database](/system-design/building-blocks/choosing-a-database)
- [The Trouble with Distributed Systems](/data-systems/distributed-systems-faults)

## References

- Seth Gilbert, Nancy Lynch. "Brewer's Conjecture and the Feasibility of Consistent, Available, Partition-Tolerant Web Services." *ACM SIGACT News* 33(2), 2002.
- Daniel Abadi. "Consistency Tradeoffs in Modern Distributed Database System Design." *IEEE Computer* 45(2), 2012.
- Martin Kleppmann. *Designing Data-Intensive Applications*. O'Reilly, 2017. Chapter 9 ("The cost of linearizability").
- Alex Xu. *ByteByteGo System Design Handbook*: trade-off between latency and consistency.
- System Design Notes (handwritten, LinkedIn): CAP theorem.
