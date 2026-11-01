---
title: "Java Developer Fundamentals"
description: "From how Java runs to concurrency and the JVM — the foundation every other path builds on."
weight: 1
audience: "Developers new to Java, or experienced developers filling gaps before interviews"
steps:
  - title: "JVM, JRE and JDK; compilation and bytecode"
    why: "Everything else assumes you know what happens between `javac` and a running program."
  - title: "Classes, objects, interfaces and records"
    page: /java/fundamentals/modern-java-features
    why: "The building blocks of object-oriented Java, including modern records and sealed types."
  - title: "Object-oriented design with SOLID"
    page: /design-patterns/solid-principles
    why: "SOLID turns the OOP vocabulary into design rules you can apply and defend."
    also: [/design-patterns/what-are-design-patterns]
  - title: "The Collections Framework"
    page: /java/fundamentals/collections-framework
    why: "Choosing the right collection is a daily decision and a common interview topic."
  - title: "Generics, wildcards and type erasure"
    page: /java/fundamentals/generics-and-type-erasure
    why: "Needed to read and write any non-trivial library API."
  - title: "Exceptions: checked, unchecked and design guidelines"
    page: /java/fundamentals/exceptions-and-try-with-resources
    why: "Error handling shapes APIs; misused exceptions leak implementation details."
  - title: "Dependency injection in plain Java"
    page: /spring/core/dependency-injection
    why: "Constructor injection and a composition root make code testable before any framework is involved."
  - title: "Concurrency: threads, executors, locks and atomics"
    page: /java/advanced/concurrency-api
    also: [/java/advanced/functional-programming-and-streams]
    why: "Correctness under concurrency is where many production bugs and interview questions live."
  - title: "Designing concurrent objects and concurrency patterns"
    page: /java/advanced/concurrent-object-design
    also: [/java/advanced/state-dependent-actions, /java/advanced/concurrency-design-patterns]
    why: "Doug Lea's design principles: safety, liveness, guarded methods, worker threads, futures and cancellation."
  - title: "Virtual threads (Java 21)"
    page: /java/advanced/virtual-threads-and-structured-concurrency
    why: "Changes how blocking, thread-per-request code scales."
  - title: "The Java Memory Model and garbage collection"
    page: /java/advanced/java-memory-model
    also: [/java/advanced/garbage-collection, /java/advanced/java-module-system, /java/fundamentals/reflection-and-dynamic-proxies]
    why: "Explains visibility bugs, `volatile`, and how to reason about memory and pauses."
  - title: "Interview preparation: the design-pattern map"
    page: /design-patterns/gof-overview
    why: "A compact map of the design vocabulary interviewers expect."
---

This path covers the language and platform. Steps marked **Planned** are scheduled for upcoming batches; until then, the linked articles cover the design side of the path.
