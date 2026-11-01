---
title: "Designing Concurrent Objects: Safety, Liveness and Locking Strategies"
linkTitle: "Designing concurrent objects"
description: "Doug Lea's design forces for concurrent Java — safety, liveness, performance and reusability — and the strategies for thread-safe objects: immutability, confinement, full synchronization, containment, lock splitting, optimistic updates with CAS, deadlock avoidance through lock ordering, and documenting thread-safety policies."
weight: 9
difficulty: advanced
status: complete
javaVersion: 21
tags: [java, concurrency, thread-safety, design-principles]
keywords: [designing thread safe classes, safety vs liveness, immutability concurrency, thread confinement, lock ordering deadlock, lock splitting lock striping, optimistic concurrency cas, nested monitor lockout, guardedby annotation]
lastReviewed: 2026-11-01
prerequisites:
  - /java/advanced/java-memory-model
  - /java/advanced/concurrency-api
related:
  - /java/advanced/state-dependent-actions
  - /java/advanced/concurrency-design-patterns
  - /design-patterns/creational/singleton
---

## Simple definition

A **thread-safe** class behaves correctly when its methods are called from several threads at the same time, without callers needing extra synchronization. Designing one means balancing four forces that Doug Lea describes in *Concurrent Programming in Java*:

| Force | Question | Failure looks like |
|---|---|---|
| **Safety** | Does nothing bad ever happen? | Corrupted state, lost updates, invariants broken |
| **Liveness** | Does something good eventually happen? | Deadlock, livelock, starvation, missed signals |
| **Performance** | Is it fast enough under contention? | Lock contention, context switches, cache-line ping-pong |
| **Reusability** | Can it be safely used, extended and composed? | Subclasses or callers break its locking policy |

Safety and liveness pull in opposite directions: more locking makes objects safer but risks deadlock and slows them down. Every technique on this page is a different trade-off between them.

## Why it is hard: state inconsistency

A race needs only two threads and one shared mutable field:

```java
class Counter {
    private int value;
    void increment() { value++; }       // read, add, write: three steps, not one
}
```

Two threads can both read 41 and both write 42; one increment is lost. Other classic safety failures:

- **Read/write conflicts:** a reader sees a half-updated object (an `x` from one update and a `y` from another).
- **Write/write conflicts:** two writers interleave and the object ends in a state neither intended.
- **Visibility failures:** a thread never sees another thread's write because there is no [happens-before](/java/advanced/java-memory-model) relation.

## Strategy 1: immutability

An object whose state cannot change after construction is thread-safe **without any locking**. Lea calls this "freezing state upon construction".

```java
public record Money(BigDecimal amount, Currency currency) {      // all fields final
    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);    // new object instead of mutation
    }
}
```

Requirements: all fields `final`, no setters, mutable components defensively copied (or themselves immutable), and `this` not leaked during construction. `final` fields also get special JMM guarantees: once the constructor finishes, every thread sees their values.

**Stateless service objects** — classes with no fields, or only final references to other thread-safe objects — are the extreme case. Most Spring singleton beans should look like this.

> [!KEY] Prefer immutability first
> Each immutable class removes a whole category of bugs. Use records, `List.copyOf`, and value objects; confine mutation to a few well-guarded places.

## Strategy 2: confinement

If only one thread can ever reach an object, it needs no synchronization.

- **Stack confinement:** local variables are visible only to their thread.
- **Thread confinement:** `ThreadLocal`, or a design where one thread owns a resource (an event loop, a single writer).
- **Object confinement (containment):** a thread-safe object *owns* unsynchronized helper objects and never lets references escape. Access to the helpers is protected by the owner's lock.

```java
public final class ShoppingCart {
    private final List<Line> lines = new ArrayList<>();   // confined: never returned directly

    public synchronized void add(Line line) { lines.add(line); }
    public synchronized List<Line> lines() { return List.copyOf(lines); }   // copy, not the internal list
}
```

Lea's **containment** idea extends this to hierarchies: an outer object guards inner "parts" with its own lock (*hierarchical locking*), so the parts can be simple, non-thread-safe classes.

## Strategy 3: full synchronization

A **fully synchronized object** makes every public method `synchronized` (or guards it with one lock), and never calls out to unknown code while holding the lock. It is simple and correct for small objects, and it is what `Collections.synchronizedList` and `Vector` do.

Rules that keep it safe:

1. Guard **every** access to mutable state with the **same** lock, including reads (otherwise visibility is not guaranteed).
2. Keep compound actions (check-then-act, read-modify-write) inside one locked region.
3. **Do not call alien methods while holding the lock** (listeners, overridable methods, callbacks): they may block, or take other locks and deadlock.

```java
// Check-then-act must be atomic as a whole
synchronized (accounts) {
    if (!accounts.containsKey(id)) accounts.put(id, new Account(id));
}
// Better: let the concurrent collection do it atomically
accounts.computeIfAbsent(id, Account::new);
```

## Strategy 4: splitting objects and locks

One lock for everything limits throughput. Lea describes ways to reduce contention:

- **Lock splitting:** independent parts of state get independent locks (a queue with separate locks for head and tail, as in `LinkedBlockingQueue`).
- **Lock striping:** partition data and give each partition its own lock (`ConcurrentHashMap` locks per bin).
- **Read/write locks:** many readers or one writer (`ReentrantReadWriteLock`, `StampedLock`), useful when reads dominate and are long.
- **Isolating dependent representations:** split an object so fields that change together are guarded together, and unrelated fields are not.

```java
final class Stats {
    private final Object hitsLock = new Object();
    private final Object missesLock = new Object();
    private long hits, misses;

    void hit()  { synchronized (hitsLock)   { hits++; } }
    void miss() { synchronized (missesLock) { misses++; } }
}
```

Only split locks for state that has **no invariant spanning both parts**. If `hits + misses == total` must always hold, they need one lock.

## Strategy 5: optimistic updates

Instead of locking, read the current state, compute a new state, and **commit only if nothing changed in between**, retrying otherwise. In Java this is compare-and-set on atomics:

```java
private final AtomicReference<Range> range = new AtomicReference<>(new Range(0, 10));

void setLower(int lower) {
    while (true) {
        Range current = range.get();
        Range next = current.withLower(lower);            // immutable: compute a new value
        if (range.compareAndSet(current, next)) return;    // commit if unchanged, else retry
    }
}
```

`AtomicInteger.incrementAndGet()`, `LongAdder`, `ConcurrentHashMap.merge` and copy-on-write collections all use this idea. Optimistic updates never deadlock and scale well under low contention; under heavy contention, repeated retries waste CPU. The same idea appears in databases as **optimistic locking** with version columns, see [Transactions](/spring/data/transactions).

## Liveness failures and how to avoid them

| Failure | What happens | Prevention |
|---|---|---|
| **Deadlock** | Two threads each hold a lock the other needs | Acquire locks in a **global order**; use `tryLock` with timeout; hold fewer locks |
| **Nested monitor lockout** | A thread waits inside an inner object while holding the outer object's lock, so nobody can enter the outer to signal it | Do not call `wait()` while holding other locks; restructure so waiting and signalling use the same lock |
| **Livelock** | Threads keep reacting to each other and make no progress | Randomised back-off |
| **Starvation** | A thread never gets the lock or CPU | Fair locks (`new ReentrantLock(true)`), avoid long critical sections |
| **Missed signals** | `notify()` happens before the waiter starts waiting | Always wait in a loop on a condition, see [State-dependent actions](/java/advanced/state-dependent-actions) |

**Lock ordering** example: transferring between accounts always locks the lower ID first, whatever the transfer direction.

```java
void transfer(Account from, Account to, BigDecimal amount) {
    Account first = from.id() < to.id() ? from : to;
    Account second = first == from ? to : from;
    synchronized (first) {
        synchronized (second) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
```

## Concurrency and reusability

Lea highlights a tension: a class that is safe on its own can be broken by **subclassing** (an override that forgets to lock) or **composition** (two thread-safe calls that are not atomic together). Approaches:

- Make concurrent classes **final**, or document exactly which lock subclasses must hold.
- Offer atomic compound operations (`putIfAbsent`, `computeIfAbsent`) instead of forcing callers to combine calls.
- Use **client-side locking** only when the class documents which lock it uses.

## Documenting the policy

Thread-safety is part of a class's contract and cannot be inferred from its code by callers. Document it, for example with the annotations from *Java Concurrency in Practice* (available in libraries such as JCIP annotations or Error Prone):

```java
@ThreadSafe
public final class RateTracker {
    @GuardedBy("this") private long count;
    @GuardedBy("this") private Instant windowStart;
    ...
}
```

Levels to state explicitly: **immutable**, **unconditionally thread-safe**, **conditionally thread-safe** (some sequences need external locking, as with iterating a synchronized list), **not thread-safe**, and **thread-hostile**.

## A safety review checklist

- Which fields are mutable, and which lock guards each one?
- Are all compound actions atomic?
- Does any method call alien code while holding a lock?
- Can `this` or internal mutable objects escape?
- Is there more than one lock? If so, is the acquisition order fixed?
- Are waits done in loops, with timeouts where appropriate?
- Is the thread-safety level documented?

## Common mistakes

> [!MISTAKE] Synchronizing writes but not reads
> A getter without synchronization may never see the value written under a lock. Reads need the same lock, or the field must be `volatile` (for single-variable state) or atomic.

- **Locking on a public object** (`synchronized (this)` in a class whose instances are shared as locks elsewhere), or on interned strings and boxed integers.
- **Holding a lock during I/O or remote calls**, turning a slow dependency into system-wide contention.
- **Assuming thread-safe parts make a thread-safe whole.** Two `ConcurrentHashMap` calls are not atomic together.
- **Using `synchronized` with virtual threads for blocking I/O** on Java 21–23, which pins the carrier thread; Java 24 (JEP 491) removed most pinning. See [Virtual Threads](/java/advanced/virtual-threads-and-structured-concurrency).

## Quick check

**What are Lea's four design forces?**
Safety, liveness, performance and reusability.

**Why is an immutable object thread-safe without locks?**
Its state never changes after construction, and `final` field semantics make that state visible to all threads once construction completes.

**How do you prevent deadlock when two locks are needed?**
Always acquire them in a consistent global order, or use `tryLock` with a timeout and back off.

**What is lock splitting, and when is it unsafe?**
Using separate locks for independent parts of an object's state. It is unsafe when an invariant spans both parts.

**What is an optimistic update?**
Compute a new state from a snapshot and commit with compare-and-set only if the state has not changed, retrying otherwise.

## Summary

- Balance safety, liveness, performance and reusability.
- Prefer immutability and confinement; then full synchronization; then finer-grained or optimistic techniques when measurements justify them.
- Prevent deadlock with lock ordering and by never calling alien code under a lock.
- Document the thread-safety policy as part of the class contract.

## Related topics

- [State-Dependent Actions: Balking, Guarded Suspension and Timeouts](/java/advanced/state-dependent-actions)
- [Concurrency Design Patterns](/java/advanced/concurrency-design-patterns)
- [The Java Memory Model and Happens-Before](/java/advanced/java-memory-model)
- [The Java Concurrency API](/java/advanced/concurrency-api)

## References

- Doug Lea. *Concurrent Programming in Java: Design Principles and Patterns* (2nd edition). Addison-Wesley, 1999. Chapter 2: Exclusion (immutability, synchronization, confinement, structuring and refactoring classes, optimistic techniques).
- Brian Goetz et al. *Java Concurrency in Practice*. Addison-Wesley, 2006. Chapters 2–4 and 10.
- [JEP 491: Synchronize Virtual Threads without Pinning](https://openjdk.org/jeps/491) (Java 24).
