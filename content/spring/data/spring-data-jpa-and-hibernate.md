---
title: "Spring Data JPA and the Hibernate ORM Lifecycle"
linkTitle: "Spring Data JPA & Hibernate"
description: "How JPA, Hibernate and Spring Data fit together: the persistence context as a unit of work and first-level cache, entity states (transient, managed, detached, removed), flush modes and dirty checking, save vs merge, repositories and derived queries, projections, open-session-in-view, batching, identifier generation, and what changed in Hibernate 7 and Jakarta Persistence 3.2."
weight: 1
difficulty: intermediate
status: complete
javaVersion: 21
tags: [spring-data, jpa, hibernate, persistence]
keywords: [spring data jpa, hibernate entity lifecycle, persistence context first level cache, entity states transient managed detached removed, dirty checking flush, save vs merge, derived query methods, spring data projections, open session in view, hibernate batch insert, hibernate 7 jakarta persistence 3.2]
lastReviewed: 2026-11-01
related:
  - /spring/data/entity-mappings-and-fetch-strategies
  - /spring/data/transactions
---

## Simple definition

- **JPA (Jakarta Persistence)** is the standard Java API for object-relational mapping: annotated **entities** are mapped to tables and managed by an **`EntityManager`**.
- **Hibernate** is the most widely used JPA implementation.
- **Spring Data JPA** sits on top and generates **repositories**, so you rarely write `EntityManager` code yourself.

## The problem

Hand-written JDBC means mapping rows to objects, tracking what changed and writing every INSERT and UPDATE yourself. ORMs automate that, but their **automatic behaviours** (caching, dirty checking, lazy loading, flush timing) surprise teams that do not understand the **persistence context**.

## The persistence context

The **persistence context** is the set of entity instances an `EntityManager` (a Hibernate `Session`) is currently managing. In Spring it usually lives **for one transaction**.

- **First-level cache:** within one context, loading the same ID twice returns the **same Java instance**, with no second SELECT.
- **Unit of work:** changes to managed entities are tracked and written at **flush**.
- **Dirty checking:** at flush, Hibernate compares each managed entity with its loaded snapshot and issues UPDATEs for changed ones. **You do not need to call `save()` to update a managed entity.**

## Entity states

{{< interactive-diagram file="jpa-entity-lifecycle.html" title="JPA entity states and the persistence context" source="diagrams/archify/jpa-entity-lifecycle.lifecycle.json" tool="Archify" height="680" >}}

| State | Meaning | How you get there |
|---|---|---|
| **Transient** | A new object, not known to JPA | `new Order()` |
| **Managed** | Tracked by the current persistence context | `persist`, `find`, a query, `merge` result |
| **Detached** | Has an ID but its context is closed | After the transaction or `clear()`; returned to a web layer |
| **Removed** | Scheduled for deletion | `remove(managed)` |

**Flush** sends pending SQL to the database (still inside the transaction). It happens **before commit**, **before queries** whose results could be affected (`FlushMode.AUTO`), or when you call `flush()` explicitly. **Commit** makes it durable.

```java
@Transactional
public void rename(long id, String name) {
    Customer c = customers.findById(id).orElseThrow();   // managed
    c.rename(name);                                      // no save() needed: dirty checking issues the UPDATE at commit
}
```

## Spring Data repositories

```java
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByCustomerIdAndStatusOrderByCreatedAtDesc(String customerId, Status status);   // derived query

    Optional<Order> findFirstByCustomerIdOrderByCreatedAtDesc(String customerId);

    @Query("select o from Order o join fetch o.lines where o.id = :id")                           // JPQL
    Optional<Order> findWithLines(long id);

    <T> List<T> findByStatus(Status status, Class<T> type);                                      // dynamic projection

    @Modifying
    @Query("update Order o set o.status = :to where o.status = :from and o.createdAt < :before")
    int bulkUpdate(Status from, Status to, Instant before);                                       // bypasses the persistence context!
}

public record OrderSummary(Long id, BigDecimal total) {}   // DTO projection: only the selected columns
```

- **Derived queries** come from method names. Keep them short; use `@Query` beyond about three conditions.
- **Projections** (interface or record/DTO) fetch only the columns you need, which is great for read endpoints.
- **`save()`** calls `persist` for new entities and **`merge`** for detached ones. Merge **copies** state into a managed instance and returns it, so use the returned object.
- Bulk `@Modifying` queries skip dirty checking, lifecycle callbacks and the first-level cache. Clear the context afterwards (`clearAutomatically = true`).

## Identifier generation

| Strategy | Notes |
|---|---|
| `SEQUENCE` (with `allocationSize` 50) | **Best with Hibernate**: allows JDBC batching and pooled IDs |
| `IDENTITY` (auto-increment) | Simple, but **disables insert batching** |
| `UUID` (`@UuidGenerator`, time-ordered UUIDv7 in recent Hibernate) | Good for distributed systems; prefer time-ordered UUIDs for index locality |

## Performance essentials

- **JDBC batching:** `spring.jpa.properties.hibernate.jdbc.batch_size=50`, `order_inserts=true` and `order_updates=true`.
- **Read-only transactions:** `@Transactional(readOnly = true)` skips dirty checking and snapshots.
- **Large result sets:** stream (`Stream<T>` with a fetch size), paginate with keyset pagination, or use projections.
- **Open Session in View:** Spring Boot enables `spring.jpa.open-in-view=true` by default and logs a warning. It keeps the session open during view rendering, which hides lazy-loading problems and holds connections longer. **Set it to `false`** for APIs and fetch what you need explicitly.
- See the [N+1 problem and fetch strategies](/spring/data/entity-mappings-and-fetch-strategies).

> [!VERSION] Hibernate 7 and Jakarta Persistence 3.2
> Spring Boot 4 ships **Hibernate ORM 7**, implementing **Jakarta Persistence 3.2**. Notable changes:
> - type-safe criteria and `EntityGraph` improvements
> - **`Session#findMultiple`**
> - the new **Jakarta Data** repositories (an alternative standard repository API)
> - stricter and cleaner deprecations
> - the `hibernate-*` artifacts moved to the `org.hibernate.orm` group.
>
> Check the migration guide for removed legacy APIs.

## Spring Data JPA vs alternatives

| Option | When |
|---|---|
| **Spring Data JPA** | Rich domain models, CRUD plus moderate queries |
| **Spring Data JDBC** | Simpler aggregates, no lazy loading or persistence-context magic |
| **jOOQ / JdbcClient** | SQL-first, complex reporting queries, full control |
| **R2DBC** | Reactive stacks |

## Common mistakes

> [!MISTAKE] Calling `save()` on managed entities
> It is unnecessary, and for detached entities `merge` returns a **different** instance. Ignoring the returned object loses changes.

> [!MISTAKE] Leaving open-in-view on
> It hides N+1 queries and holds database connections during JSON serialisation. Disable it and use DTOs or fetch joins.

> [!MISTAKE] IDENTITY IDs with bulk inserts
> Hibernate cannot batch them. Use sequences with an allocation size.

## Quick check

**What is the persistence context?**
The set of managed entity instances for an EntityManager or Session, usually one per transaction. It is a first-level cache and a unit of work with dirty checking.

**Why does modifying a managed entity update the database without `save()`?**
At flush, dirty checking compares managed entities with their snapshots and issues UPDATE statements.

**What are the four entity states?**
Transient, managed, detached and removed.

**`persist` vs `merge`?**
`persist` makes a new instance managed. `merge` copies a detached instance's state onto a managed instance and returns that managed copy.

**Why disable open-session-in-view?**
It extends database sessions into the web layer, hides lazy-loading and N+1 problems, and holds connections longer.

## Summary

- Spring Data repositories sit on JPA and Hibernate; the persistence context is the key concept.
- Managed entities are dirty-checked and flushed automatically; know your entity states.
- Use projections, batching, sequences and read-only transactions; disable open-in-view.
- Spring Boot 4 brings Hibernate 7 and Jakarta Persistence 3.2.

## References

- Spring Data JPA, [reference documentation](https://docs.spring.io/spring-data/jpa/reference/).
- Hibernate ORM, [User Guide](https://docs.jboss.org/hibernate/orm/7.0/userguide/html_single/Hibernate_User_Guide.html) and [Hibernate 7 migration guide](https://docs.jboss.org/hibernate/orm/7.0/migration-guide/migration-guide.html).
- Vlad Mihalcea, *High-Performance Java Persistence*.
