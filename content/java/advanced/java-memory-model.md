---
title: "The Java Memory Model and Happens-Before"
linkTitle: "Java Memory Model"
description: "Why concurrent Java code can see stale or reordered values: CPU caches and reordering, the Java Memory Model, happens-before rules, volatile, synchronized and final semantics, safe publication, double-checked locking, data races vs race conditions, and the JVM runtime memory areas (heap, stack, metaspace)."
weight: 1
difficulty: advanced
status: complete
javaVersion: 21
tags: [java, concurrency, jmm, jvm]
keywords: [java memory model, happens before, volatile keyword java, visibility problem, instruction reordering, safe publication, double checked locking, final field semantics, data race, jvm memory areas heap stack metaspace]
lastReviewed: 2026-11-01
related:
  - /java/advanced/concurrency-api
  - /java/advanced/garbage-collection
---

## Simple definition

The **Java Memory Model (JMM)**, defined in chapter 17 of the Java Language Specification, specifies **when a write made by one thread is guaranteed to be visible to another**. It defines a **happens-before** relation: if action A happens-before action B, then B sees A's effects.

## The problem

This code can loop **forever**:

```java
class Worker {
    private boolean running = true;            // not volatile
    void stop() { running = false; }           // called from another thread
    void run() { while (running) { doWork(); } }
}
```

The JIT may hoist `running` into a register, and CPUs keep values in **caches** and **reorder** instructions for speed. Without a happens-before edge, the worker thread may never see `false`.

## Why reordering and caching happen

```mermaid {title="Each core has caches; writes are not instantly visible"}
flowchart LR
    T1["Thread 1 on core 1"] --> L1A["L1/L2 cache"] --> RAM["Main memory / L3"]
    T2["Thread 2 on core 2"] --> L1B["L1/L2 cache"] --> RAM
```

Compilers, the JIT and CPUs may reorder independent operations as long as **a single thread** cannot tell the difference. Other threads **can**. The JMM tells you which synchronisation actions forbid that.

## Happens-before rules

| Rule | Guarantee |
|---|---|
| **Program order** | Each action in a thread happens-before later actions in that same thread |
| **Monitor lock** | Unlocking a monitor happens-before every later lock of the **same** monitor |
| **volatile** | A write to a volatile field happens-before every later read of that field |
| **Thread start** | `thread.start()` happens-before any action in the started thread |
| **Thread join** | All actions in a thread happen-before another thread returns from `join()` on it |
| **Transitivity** | If A → B and B → C, then A → C |
| **Final fields** | Values set in a constructor are visible to any thread that sees the object, if `this` did not escape during construction |
| **Concurrency utilities** | Putting into a `ConcurrentHashMap` or `BlockingQueue`, `Future.get`, latch countdown and so on all create happens-before edges (documented in the package javadoc) |

Fix for the example: `private volatile boolean running = true;`

## volatile vs synchronized vs atomics

| | `volatile` | `synchronized` / `Lock` | `Atomic*` |
|---|---|---|---|
| Visibility | ✅ | ✅ | ✅ |
| Prevents reordering around it | ✅ | ✅ | ✅ |
| Atomic compound actions (`count++`) | ❌ | ✅ | ✅ (CAS) |
| Mutual exclusion | ❌ | ✅ | ❌ |
| Use for | Flags, published references | Invariants over several fields | Counters, single-reference updates |

> [!MISTAKE] `volatile int count; count++`
> Increment is read-modify-write (three steps). `volatile` gives visibility, not atomicity. Use `AtomicInteger` or a lock.

## Safe publication

An object is **safely published** when another thread is guaranteed to see it **fully constructed**. Safe ways:

1. Initialise it in a **static initialiser**.
2. Store it in a **volatile** field or an `AtomicReference`.
3. Store it in a **final** field of a properly constructed object.
4. Store it in a field guarded by a **lock**, or in a **concurrent collection**.

**Immutable objects** (all fields final, no `this` escape) are thread-safe however they are published. That is one reason records and immutable designs simplify concurrency.

### Double-checked locking

```java
private static volatile Config instance;               // volatile is REQUIRED
static Config get() {
    Config c = instance;
    if (c == null) {
        synchronized (Config.class) {
            c = instance;
            if (c == null) instance = c = load();
        }
    }
    return c;
}
```

Without `volatile`, another thread could see a non-null reference to a **partially constructed** object. Simpler alternatives are the **holder class idiom** (lazy and thread-safe through class initialisation), an `enum` singleton, or, in preview, lazy constants (formerly called stable values).

## Data race vs race condition

- A **data race** is two threads accessing the same variable, at least one writing, without happens-before ordering. The JMM gives such programs weak guarantees.
- A **race condition** is a correctness bug caused by timing, even with proper synchronisation, such as check-then-act across two synchronised calls.

You need to avoid both.

## JVM runtime memory areas

The JMM is about visibility. The **runtime data areas** are about where things live:

| Area | Contains | Shared? | Error when full |
|---|---|---|---|
| **Heap** | All objects and arrays (young and old generations) | All threads | `OutOfMemoryError: Java heap space` |
| **Thread stack** | Frames: local variables, operand stack, return addresses | Per thread (virtual thread stacks live on the heap when parked) | `StackOverflowError` |
| **Metaspace** | Class metadata (native memory since Java 8) | All | `OutOfMemoryError: Metaspace` |
| **Code cache** | JIT-compiled native code | All | JIT stops compiling |
| **Direct / native memory** | NIO buffers, FFM `MemorySegment`s, GC structures, thread stacks | All | Native OOM or container kill |

Escape analysis lets the JIT **scalar-replace** objects that never escape a method, so they effectively live in registers or on the stack. See [garbage collection](/java/advanced/garbage-collection) for how the heap is managed.

## Common mistakes

> [!MISTAKE] Assuming writes are visible "soon"
> Without happens-before there is no guarantee at all. Use volatile, locks or concurrent utilities.

> [!MISTAKE] Letting `this` escape the constructor
> Registering a listener or starting a thread in the constructor can expose a half-built object, even with final fields.

> [!MISTAKE] Double-checked locking without volatile
> It can publish a partially constructed object. Prefer the holder idiom.

## Quick check

**What does the Java Memory Model define?**
The rules, through the happens-before relation, for when one thread's writes are guaranteed to be visible to another thread's reads.

**What does `volatile` guarantee?**
Visibility and ordering: a write happens-before later reads of the same field. It does not make compound actions atomic.

**Why must the field be volatile in double-checked locking?**
Without it, another thread can see a non-null reference to an object whose constructor writes are not yet visible.

**Name three ways to publish an object safely.**
A static initialiser; a volatile or atomic reference; a final field of a properly constructed object; a lock-guarded field; or a concurrent collection.

**What lives in Metaspace?**
Class metadata, in native memory, since Java 8 replaced PermGen.

## Summary

- CPUs and compilers reorder and cache, so visibility needs happens-before.
- volatile gives visibility; locks add atomicity and exclusion; atomics give lock-free single-variable updates.
- Publish safely; prefer immutable objects; beware `this` escape.
- Heap, stacks, metaspace and native memory are distinct areas with distinct failure modes.

## References

- [Java Language Specification, chapter 17: Threads and Locks](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html).
- Aleksey Shipilёv, [JMM Pragmatics](https://shipilev.net/blog/2014/jmm-pragmatics/).
- Brian Goetz et al., *Java Concurrency in Practice*, chapter 16.
- Doug Lea, [Using JDK 9 Memory Order Modes](https://gee.cs.oswego.edu/dl/html/j9mm.html).
