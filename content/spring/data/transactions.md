---
title: "Database Transactions in Spring: @Transactional, Propagation and Isolation"
linkTitle: "Transactions"
description: "How Spring manages transactions: ACID, PlatformTransactionManager, how @Transactional works through proxies, rollback rules, all seven propagation behaviours with use cases, isolation levels and anomalies, read-only transactions, timeouts, optimistic and pessimistic locking, programmatic TransactionTemplate, and transactions with messaging."
weight: 4
difficulty: intermediate
status: complete
javaVersion: 21
tags: [spring, transactions, jpa, consistency]
keywords: [spring transactional, transaction propagation required requires_new nested, isolation levels read committed repeatable read serializable, transactional rollback checked exception, transactional self invocation, readonly transaction, optimistic locking version, pessimistic locking for update, transactiontemplate, dirty read phantom read lost update]
lastReviewed: 2026-11-01
prerequisites:
  - /spring/data/spring-data-jpa-and-hibernate
  - /spring/core/aop-and-proxies
related:
  - /spring/core/application-events
  - /microservices/communication/event-driven-spring-cloud-stream
---

## Simple definition

A **transaction** groups database operations so they **all succeed or all fail** (ACID). In Spring you declare transaction boundaries with **`@Transactional`**. A proxy then begins, commits or rolls back through a **`PlatformTransactionManager`** (for example `JpaTransactionManager`).

## The problem

Without clear boundaries, a failure halfway through "debit account, credit account, record transfer" leaves money missing. Wrong **propagation** silently joins or splits transactions. Wrong **isolation** allows lost updates and double bookings. Misplaced `@Transactional` simply **does nothing**.

## ACID in one table

| Property | Meaning |
|---|---|
| **Atomicity** | All or nothing |
| **Consistency** | Constraints and invariants hold before and after |
| **Isolation** | Concurrent transactions do not see each other's intermediate states (to a configurable degree) |
| **Durability** | Committed data survives crashes |

## How @Transactional works

{{< interactive-diagram file="transactional-proxy.html" title="How @Transactional works through a proxy" source="diagrams/archify/transactional-proxy.sequence.json" tool="Archify" height="720" >}}

```java
@Service
class TransferService {
    @Transactional                                       // REQUIRED, default isolation, rollback on RuntimeException
    public void transfer(AccountId from, AccountId to, Money amount) {
        Account a = accounts.findByIdForUpdate(from);    // pessimistic lock (see below)
        Account b = accounts.findById(to).orElseThrow();
        a.debit(amount);
        b.credit(amount);
        transfers.save(new Transfer(from, to, amount));
    }                                                    // commit (flush, then COMMIT) or rollback
}
```

- The **proxy** (AOP) begins or joins a transaction, binds the connection to the **current thread**, calls the method, then commits or rolls back. See [AOP and proxies](/spring/core/aop-and-proxies).
- Put `@Transactional` on **public service methods** (the use-case boundary), not on repositories or controllers.
- **Self-invocation** (`this.otherTransactionalMethod()`) bypasses the proxy.

## Rollback rules

- By default: roll back on **`RuntimeException`** and **`Error`**, **commit on checked exceptions**.
- Change that with `@Transactional(rollbackFor = Exception.class)` or `noRollbackFor = …`.
- An exception **caught** inside the method does not roll back, unless you call `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()`.
- If an **inner** `REQUIRED` method throws and the outer method catches, the shared transaction is marked **rollback-only**, so the outer commit throws `UnexpectedRollbackException`.

## Propagation

| Propagation | If a transaction exists | If none exists | Use case |
|---|---|---|---|
| **REQUIRED** (default) | Join it | Start a new one | Almost everything |
| **REQUIRES_NEW** | **Suspend** it, start a new independent one | Start new | Audit logs or outbox entries that must persist even if the caller rolls back. Uses a second connection. |
| **NESTED** | Savepoint inside the current one (JDBC only) | Start new | Partial rollback of a sub-step |
| **SUPPORTS** | Join | Run without | Read helpers that work either way |
| **MANDATORY** | Join | **Throw** | Methods that must be called inside a transaction |
| **NOT_SUPPORTED** | Suspend, run without | Run without | Long non-transactional work (calling external APIs) |
| **NEVER** | **Throw** | Run without | Guard against transactions |

```mermaid {title="REQUIRED joins; REQUIRES_NEW suspends and starts another"}
flowchart LR
    A["placeOrder()<br/>REQUIRED: tx1"] --> B["reserveStock()<br/>REQUIRED: joins tx1"]
    A --> C["writeAudit()<br/>REQUIRES_NEW: tx2<br/>(commits even if tx1 rolls back)"]
```

> [!WARNING] REQUIRES_NEW needs a second connection
> Under load, nested REQUIRES_NEW calls can exhaust the connection pool and deadlock. Keep them rare and short.

## Isolation levels

| Level | Dirty read | Non-repeatable read | Phantom read | Notes |
|---|---|---|---|---|
| READ_UNCOMMITTED | possible | possible | possible | Rarely used |
| **READ_COMMITTED** | ✗ | possible | possible | **PostgreSQL, Oracle and SQL Server default** |
| REPEATABLE_READ | ✗ | ✗ | possible (no phantoms in PostgreSQL's snapshot implementation) | **MySQL InnoDB default** |
| SERIALIZABLE | ✗ | ✗ | ✗ | Safest; may fail with serialisation errors, so retry |

`@Transactional(isolation = Isolation.SERIALIZABLE)` overrides the database default per transaction.

**Lost updates and write skew** are the anomalies that bite business code most (two users booking the last seat). Prevent them with **locking** or **serializable** isolation.

## Locking

| Approach | How | Use when |
|---|---|---|
| **Optimistic** | `@Version` column. UPDATE ... WHERE version = ?; on conflict, `OptimisticLockException` | Low contention, web forms (pair with ETag / `If-Match`). Retry or report the conflict. |
| **Pessimistic** | `@Lock(PESSIMISTIC_WRITE)` gives `SELECT … FOR UPDATE` | High contention, short critical sections (balances, inventory) |
| **Atomic SQL** | `UPDATE stock SET qty = qty - :n WHERE sku = :s AND qty >= :n` | Counters and stock: often the simplest and fastest |
| `SKIP LOCKED` | `FOR UPDATE SKIP LOCKED` | Work queues in the database |

## Read-only and timeouts

- `@Transactional(readOnly = true)` lets Hibernate skip dirty checking and snapshots, and can route to read replicas. Put it on query services; class-level read-only with method-level writes is a common pattern.
- `@Transactional(timeout = 5)` caps long transactions.
- Keep transactions **short**: **no remote HTTP calls inside a transaction**. They hold locks and connections while waiting.

## Programmatic transactions

```java
transactionTemplate.executeWithoutResult(status -> {
    repository.save(entity);
    if (!valid) status.setRollbackOnly();
});
```

Useful for fine-grained boundaries, loops with a transaction per item, or avoiding self-invocation issues.

## Transactions and messaging

A database transaction **does not include** Kafka or RabbitMQ sends or HTTP calls. To publish events reliably with a DB change, use the **transactional outbox**, or `@TransactionalEventListener(AFTER_COMMIT)` for best-effort in-process reactions. See [application events](/spring/core/application-events) and [event-driven architecture](/microservices/communication/event-driven-spring-cloud-stream).

## Common mistakes

> [!MISTAKE] `@Transactional` on private methods or self-invoked calls
> The proxy never sees the call, so there is no transaction. Use public methods on another bean.

> [!MISTAKE] Expecting rollback on checked exceptions
> Spring commits by default. Add `rollbackFor`, or use unchecked exceptions.

> [!MISTAKE] Remote calls inside transactions
> They hold database locks and connections during network waits. Move them outside, or use an outbox.

> [!MISTAKE] Swallowing exceptions from a joined inner method
> It leads to `UnexpectedRollbackException` at commit. Let it propagate, or use REQUIRES_NEW for truly independent work.

## Quick check

**Which exceptions roll back by default?**
`RuntimeException` and `Error`. Checked exceptions commit unless `rollbackFor` is set.

**REQUIRED vs REQUIRES_NEW?**
REQUIRED joins an existing transaction or starts one. REQUIRES_NEW always suspends the current one and runs in a new, independent transaction.

**What is PostgreSQL's default isolation level?**
READ COMMITTED.

**Optimistic vs pessimistic locking?**
Optimistic detects conflicts at write time with a version column. Pessimistic locks rows (`SELECT … FOR UPDATE`) to prevent them.

**Why shouldn't you call external APIs inside a transaction?**
The transaction holds connections and locks for the whole network wait, which hurts throughput and risks deadlocks and timeouts.

**What does `readOnly = true` do with Hibernate?**
It skips dirty checking and snapshot storage and hints the database. It may route to read replicas.

## Summary

- `@Transactional` works through proxies: public methods, called from outside.
- Default rollback is on unchecked exceptions; REQUIRED joins; REQUIRES_NEW isolates (at the cost of a second connection).
- Choose isolation and locking for the anomalies you must prevent.
- Keep transactions short and local; use the outbox for messaging.

## References

- Spring Framework reference, [Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html) and [Transaction propagation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html).
- PostgreSQL, [Transaction Isolation](https://www.postgresql.org/docs/current/transaction-iso.html).
- Martin Kleppmann, *Designing Data-Intensive Applications*, chapter 7.
