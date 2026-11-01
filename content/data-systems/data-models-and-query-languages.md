---
title: "Data Models and Query Languages"
linkTitle: "Data models"
description: "Relational, document and graph data models compared: the object-relational mismatch, one-to-many versus many-to-many relationships, schema-on-read versus schema-on-write, data locality, declarative versus imperative queries, and property graphs and triple stores — with guidance on choosing a model."
weight: 2
difficulty: intermediate
status: complete
tags: [data-systems, databases, data-modeling, nosql, sql]
keywords: [relational vs document database, object relational impedance mismatch, schema on read vs schema on write, document database joins, graph database property graph, cypher sparql, declarative query language, ddia chapter 2]
lastReviewed: 2026-11-01
prerequisites:
  - /data-systems/reliability-scalability-maintainability
related:
  - /system-design/building-blocks/choosing-a-database
  - /spring/data/entity-mappings-and-fetch-strategies
  - /data-systems/storage-engines
---

## Simple definition

A **data model** is the shape in which an application stores and thinks about its data: tables of rows, nested documents, or a graph of vertices and edges. It is one of the most important design decisions because it shapes not only how the software is written but **how we think about the problem**. Each layer of a system represents data in terms of the layer below (objects → JSON or tables → bytes → electrical signals), and each layer hides the complexity beneath it.

## The three main models

| | Relational | Document | Graph |
|---|---|---|---|
| Unit | Rows in tables, fixed columns | Self-contained documents (JSON), nested | Vertices and edges, each with properties |
| Relationships | Foreign keys and **joins** | Nesting for one-to-many; references for the rest | First-class edges, traversed directly |
| Schema | Schema-on-write (enforced by the database) | Schema-on-read (implicit, interpreted by the app) | Usually flexible |
| Best for | Many-to-many relationships, ad-hoc queries, strong integrity | Tree-shaped data loaded as a whole | Highly connected data, variable-length paths |
| Examples | PostgreSQL, MySQL, Oracle | MongoDB, Couchbase, DynamoDB items | Neo4j, Amazon Neptune, JanusGraph |

## The object-relational mismatch

Application code uses objects; relational databases use tables. A translation layer is needed, and the awkward fit is called the **impedance mismatch**. ORMs such as Hibernate reduce the boilerplate but cannot hide it completely (lazy loading, N+1 queries, see [Entity Mappings and Fetch Strategies](/spring/data/entity-mappings-and-fetch-strategies)).

Consider a résumé: one person, many positions, many education entries, contact details. Relationally this is a `users` table plus `positions`, `education` and `contact_info` tables joined by `user_id`. As a document, it is one JSON object with nested arrays. The document has **better locality**: one read fetches everything, with no joins, and the tree structure is explicit.

## One-to-many, many-to-one and many-to-many

The document model is excellent for **one-to-many** trees. It struggles as soon as data has **many-to-one** and **many-to-many** relationships:

- Many people live in the same region or work for the same company. Storing the region as a free-text string in every document duplicates it; storing an **ID** that refers to a regions collection normalises it — but then you need a join.
- If companies become entities with their own pages, and recommendations link users to other users, the data becomes a **graph**.

Document databases often have weak join support, so the join moves into application code (several queries). Relational databases were created precisely to solve this, after the hierarchical (IMS) and network (CODASYL) models of the 1970s made applications hard-code **access paths** through the data.

> [!KEY] Normalisation versus duplication
> Storing an ID instead of a human-readable value means the meaning lives in one place: update it once, and every reference sees the change. Duplicating it is faster to read but needs every copy updated. That trade-off, normalised writes versus denormalised reads, recurs throughout data systems (caches, search indexes, materialised views).

## Schema-on-read versus schema-on-write

"Schemaless" is misleading: the reading code always assumes some structure. The real distinction is **when** the structure is checked.

| | Schema-on-write (relational) | Schema-on-read (document) |
|---|---|---|
| Analogy | Static type checking | Dynamic type checking |
| Changing the format | `ALTER TABLE` + migration (often fast in modern databases, except full-table `UPDATE`s) | New code handles both old and new shapes |
| Good when | All records share a structure; integrity matters | Records are heterogeneous, or the structure is defined by external systems |

```java
// Schema-on-read: the application handles documents written before the change
String firstName = doc.containsKey("first_name")
        ? doc.getString("first_name")
        : doc.getString("name").split(" ")[0];
```

Relational databases have converged with documents: PostgreSQL and MySQL support JSON columns, and some document databases now support joins. A **hybrid** is often the pragmatic choice. See [Auditing and Migrations](/spring/data/auditing-and-migrations) for managing schema-on-write changes.

## Query languages: declarative versus imperative

- **Imperative** code tells the computer *how*: loop over records, test, accumulate.
- **Declarative** languages (SQL, CSS, Cypher) say *what* result is wanted; the engine chooses the algorithm, indexes and join order, and can **parallelise** it.

```sql
SELECT region, count(*) FROM orders WHERE status = 'PAID' GROUP BY region;
```

Declarative queries let the database improve without application changes and are easier to optimise and parallelise. **MapReduce** sits in between: you write map and reduce functions, and the framework runs them in parallel, see [Batch Processing](/data-systems/batch-processing).

## Graph-like data models

When many-to-many relationships dominate, model data as a graph.

**Property graph** (Neo4j): each vertex and edge has an ID, a label and properties; edges have a direction. Any vertex can connect to any other, and you can traverse in both directions efficiently.

```cypher
// Who was born in the United States and now lives in Europe?
MATCH (p:Person)-[:BORN_IN]->()-[:WITHIN*0..]->(:Location {name:'United States'}),
      (p)-[:LIVES_IN]->()-[:WITHIN*0..]->(:Location {name:'Europe'})
RETURN p.name
```

The `*0..` means "follow `WITHIN` edges any number of times" — a **variable-length path**. In SQL this needs a recursive common table expression (`WITH RECURSIVE`), which is far longer.

**Triple stores** (RDF, SPARQL) model everything as `(subject, predicate, object)` triples and are used for linked data and knowledge graphs. **Datalog** is the older logic-programming foundation of such query languages.

## Choosing a model

| If your data... | Prefer |
|---|---|
| Is a tree loaded and saved as a whole (a profile, an order with lines) | Document, or JSON columns in a relational table |
| Has many-to-many relationships and needs ad-hoc queries and integrity | Relational |
| Is about connections: social networks, fraud rings, recommendations, network topology | Graph |
| Needs flexible attributes per record | Document, or relational with a JSON column |

For a deeper decision guide including key-value, wide-column, time-series and search, see [Choosing a Database](/system-design/building-blocks/choosing-a-database).

## Common mistakes

> [!MISTAKE] Choosing a document store for relational data
> Data that starts as self-contained documents tends to grow references over time. Rebuilding joins in application code is slower and loses integrity checks. Ask "will we need to query across documents?" before choosing.

- **Huge documents** that are rewritten in full on every small update; keep documents small or split them.
- **"Schemaless" with no schema discipline**, so every reader handles a dozen historical shapes.
- **Graph databases for simple lookups**, adding a niche technology without a traversal-heavy workload.

## Quick check

**What is the object-relational impedance mismatch?**
The awkward translation between objects in application code and rows in relational tables.

**Why do document databases struggle with many-to-many relationships?**
They have weak or no join support, so relationships must be resolved in application code or denormalised with duplication.

**Is a document database schemaless?**
No. It is schema-on-read: structure is implicit and interpreted by the reading code, rather than enforced on write.

**What is an advantage of declarative query languages?**
The engine can choose and change the execution strategy (indexes, join order, parallelism) without changing the query.

**When is a graph model the right choice?**
When the data is dominated by many-to-many relationships and queries traverse paths of variable length.

## Summary

- Relational, document and graph models suit different relationship shapes.
- Document models give locality for trees; relational models handle many-to-many with joins.
- Schema-on-read versus schema-on-write is about when structure is checked, not whether it exists.
- Declarative queries let databases optimise; graphs make traversals natural.

## Related topics

- [Choosing a Database](/system-design/building-blocks/choosing-a-database)
- [Storage Engines](/data-systems/storage-engines)
- [Entity Mappings and Fetch Strategies](/spring/data/entity-mappings-and-fetch-strategies)

## References

- Martin Kleppmann. *Designing Data-Intensive Applications*. O'Reilly, 2017. Chapter 2: Data Models and Query Languages.
- E. F. Codd. "A Relational Model of Data for Large Shared Data Banks." *Communications of the ACM* 13(6), 1970.
