---
title: "Consistent Hashing"
linkTitle: "Consistent hashing"
description: "Why hash mod N breaks when servers change, how consistent hashing places servers and keys on a ring so only a fraction of keys move, virtual nodes for balance, replication along the ring, rendezvous hashing and jump hash, a Java TreeMap implementation, and where consistent hashing is used."
weight: 4
difficulty: intermediate
status: complete
javaVersion: 21
tags: [system-design, consistent-hashing, distributed-systems, caching, partitioning]
keywords: [consistent hashing explained, hash ring, virtual nodes, hash mod n rehashing problem, rendezvous hashing, jump consistent hash, distributed cache key distribution, consistent hashing java treemap]
lastReviewed: 2026-11-01
prerequisites:
  - /system-design/building-blocks/caching-strategies
related:
  - /data-systems/partitioning
  - /system-design/building-blocks/load-balancing
  - /data-systems/replication
---

## Simple definition

**Consistent hashing** is a way to assign keys to servers so that when a server is **added or removed, only a small fraction of keys move** — about *K / N* of *K* keys for *N* servers — instead of nearly all of them. It is the standard technique for distributing data across distributed caches and leaderless databases.

## The problem with hash mod N

The naive approach, used by a **distributed hash table**, is `server = hash(key) % N`.

| Key | hash | % 4 servers | % 5 servers |
|---|---|---|---|
| user:1 | 18 | 2 | 3 |
| user:2 | 25 | 1 | 0 |
| user:3 | 31 | 3 | 1 |
| user:4 | 44 | 0 | 4 |

Adding one server changes the result for almost every key. For a cache, that means a near-total **cache miss storm** that hits the database all at once; for a data store, it means moving almost all data. The same happens when a server fails.

## How consistent hashing works

1. Map the output range of a hash function onto a **ring** (0 to 2³² − 1, wrapping around).
2. Hash each **server** (by name or IP) to a position on the ring.
3. Hash each **key** to a position, then walk **clockwise** to the first server: that server owns the key.

```mermaid {title="A hash ring: each key belongs to the next server clockwise"}
flowchart LR
    subgraph Ring["Hash ring (clockwise)"]
        direction LR
        K1([key1]) --> A[Server A]
        A --> K2([key2])
        K2 --> B[Server B]
        B --> K3([key3])
        K3 --> C[Server C]
        C --> K4([key4])
        K4 --> A
    end
```

- **Adding** server D between C and A: only keys between C and D (previously owned by A) move to D.
- **Removing** server B: only B's keys move, to the next server clockwise (C).

Every other key stays where it was.

## Virtual nodes

With few servers, random positions divide the ring unevenly: one server may own 50% of it. And when a server leaves, its whole load lands on a single neighbour.

**Virtual nodes** fix both: each physical server is placed on the ring many times (100–200 positions, by hashing `serverA#0`, `serverA#1`...). Each server then owns many small arcs, the load evens out statistically, and a departing server's keys spread across many servers. Weights are easy too: give a bigger server more virtual nodes.

## A Java implementation

```java
public final class ConsistentHashRing<N> {
    private final NavigableMap<Long, N> ring = new TreeMap<>();
    private final int virtualNodes;
    private final ToLongFunction<String> hash;

    public ConsistentHashRing(int virtualNodes, ToLongFunction<String> hash) {
        this.virtualNodes = virtualNodes;
        this.hash = hash;              // e.g. first 8 bytes of MD5 or Murmur3 — not String.hashCode()
    }

    public void add(N node) {
        for (int i = 0; i < virtualNodes; i++) {
            ring.put(hash.applyAsLong(node + "#" + i), node);
        }
    }

    public void remove(N node) {
        for (int i = 0; i < virtualNodes; i++) {
            ring.remove(hash.applyAsLong(node + "#" + i));
        }
    }

    public N nodeFor(String key) {
        if (ring.isEmpty()) throw new IllegalStateException("no nodes");
        Map.Entry<Long, N> e = ring.ceilingEntry(hash.applyAsLong(key));   // first node clockwise
        return (e != null ? e : ring.firstEntry()).getValue();            // wrap around
    }
}
```

`TreeMap.ceilingEntry` finds the next position clockwise in O(log V) for V virtual nodes. Use a hash function with good distribution; `String.hashCode()` clusters similar strings.

## Replication along the ring

For fault tolerance, store each key on the owner **and the next *R − 1* distinct physical servers** clockwise (skipping virtual nodes of servers already chosen). This is the **preference list** of Amazon's Dynamo, used with [quorums](/data-systems/replication#leaderless-replication). Placing replicas in different racks or zones requires skipping servers in the same failure domain.

## Alternatives

| Algorithm | Idea | Trade-off |
|---|---|---|
| **Ring with virtual nodes** | Described above | Memory for virtual nodes; good balance with enough of them |
| **Rendezvous (highest random weight) hashing** | For each key, compute `hash(key, server)` for every server and pick the highest | No ring state; O(N) per lookup; minimal movement |
| **Jump consistent hash** (Google) | A few lines of arithmetic map a key to a bucket 0..N−1 | O(log N), no memory; buckets must be numbered, servers can only be added or removed at the end |
| **Fixed partitions** | Hash keys into many fixed partitions; move whole partitions between servers | Simple and common in databases (Kafka, Elasticsearch); see [Partitioning](/data-systems/partitioning) |

## Where it is used

- Distributed caches: Memcached client libraries (ketama), Redis client-side sharding. (Redis Cluster itself uses 16,384 fixed **hash slots**.)
- Leaderless databases: Amazon Dynamo, Apache Cassandra, Riak.
- Load balancers keeping requests for the same key on the same backend (cache locality): Envoy's ring hash and Maglev, NGINX `hash ... consistent`.
- CDNs and object stores choosing which server holds an object.

## Common mistakes

> [!MISTAKE] Too few virtual nodes
> With one position per server, a three-server ring is usually badly unbalanced. Use at least 100 virtual nodes per server, or a fixed-partition scheme.

- **A poor hash function** that clusters keys.
- **Replicas on virtual nodes of the same physical server**, so one machine failure loses all copies.
- **Ignoring hot keys:** consistent hashing balances key *counts*, not the traffic to one popular key.

## Quick check

**Why does hash mod N fail when N changes?**
Almost every key maps to a different server, causing massive data movement or a cache miss storm.

**How many keys move when a server is added to a consistent hash ring?**
About K/N: only the keys in the arc the new server takes over.

**What problem do virtual nodes solve?**
Uneven load from random server positions, and a single neighbour absorbing a departed server's load.

**How does consistent hashing support replication?**
Each key is stored on the next R distinct physical servers clockwise from its position.

**Name an alternative to the ring.**
Rendezvous hashing, jump consistent hash, or a fixed number of partitions assigned to servers.

## Related topics

- [Partitioning (Sharding)](/data-systems/partitioning)
- [Caching Strategies](/system-design/building-blocks/caching-strategies)
- [Load Balancing and Proxies](/system-design/building-blocks/load-balancing)
- [Replication: leaderless quorums](/data-systems/replication)

## References

- David Karger et al. "Consistent Hashing and Random Trees." STOC 1997.
- Giuseppe DeCandia et al. "Dynamo: Amazon's Highly Available Key-Value Store." SOSP 2007.
- John Lamping, Eric Veach. "A Fast, Minimal Memory, Consistent Hash Algorithm." 2014.
- System Design Notes (handwritten, LinkedIn): consistent hashing, virtual replicas.
