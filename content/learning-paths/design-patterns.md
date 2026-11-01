---
title: "Design Patterns"
description: "From principles to the 23 GoF patterns and how to tell similar patterns apart."
weight: 2
audience: "Developers who know Java and want to design better object-oriented code"
steps:
  - title: "What are design patterns?"
    page: /design-patterns/what-are-design-patterns
    why: "Sets expectations: patterns are named trade-offs, not recipes to apply everywhere."
  - title: "SOLID principles"
    page: /design-patterns/solid-principles
    why: "Most patterns apply the Open/Closed and Dependency Inversion principles; learn the principles and the patterns become easier."
  - title: "OO design principles: composition over inheritance and more"
    page: /design-patterns/oo-design-principles
    why: "The second principle of the GoF book, and the reason most patterns are object-scoped."
  - title: "The 23 GoF patterns: overview and classification"
    page: /design-patterns/gof-overview
    why: "A map of the catalogue before diving into individual patterns."
  - title: "Strategy"
    page: /design-patterns/behavioral/strategy
    why: "The simplest pattern that demonstrates OCP, DIP and composition together."
  - title: "Factory Method"
    page: /design-patterns/creational/factory-method
    why: "Introduces creational patterns — and the important distinction from simple factories."
  - title: "Builder"
    page: /design-patterns/creational/builder
    why: "Immutable objects with many optional fields, and the GoF director form."
  - title: "Adapter"
    page: /design-patterns/structural/adapter
    why: "The first structural pattern, and the basis of ports and adapters in architecture."
  - title: "The other creational patterns"
    page: /design-patterns/creational/singleton
    also: [/design-patterns/creational/abstract-factory, /design-patterns/creational/prototype]
    why: "Singleton's pitfalls, families of products, and creating by copying."
  - title: "The other structural patterns"
    page: /design-patterns/structural/decorator
    also: [/design-patterns/structural/proxy, /design-patterns/structural/facade, /design-patterns/structural/composite, /design-patterns/structural/bridge, /design-patterns/structural/flyweight]
    why: "Wrapping, simplifying, composing and sharing objects."
  - title: "Behavioral patterns: Observer, Command, State and Template Method"
    page: /design-patterns/behavioral/observer
    also: [/design-patterns/behavioral/command, /design-patterns/behavioral/state, /design-patterns/behavioral/template-method]
    why: "The behavioral patterns you will meet most often in frameworks and domain code."
  - title: "The remaining behavioral patterns"
    page: /design-patterns/behavioral/chain-of-responsibility
    also: [/design-patterns/behavioral/iterator, /design-patterns/behavioral/mediator, /design-patterns/behavioral/memento, /design-patterns/behavioral/interpreter, /design-patterns/behavioral/visitor]
    why: "Chains, traversal, coordination, snapshots, little languages and double dispatch."
  - title: "Pattern comparisons"
    page: /comparisons/adapter-decorator-proxy-facade
    why: "Many patterns share a structure; comparing them teaches intent."
    also: [/comparisons/factory-patterns, /comparisons/strategy-state-template-method, /comparisons/observer-mediator-event-bus]
  - title: "Compound patterns and MVC"
    page: /design-patterns/compound-patterns-mvc
    why: "How patterns combine in real frameworks."
  - title: "Refactoring toward patterns"
    why: "Worked examples of introducing a pattern when the code calls for it, not before."
---

Read the first two steps in order. After that, you can read the pattern articles in any order — each lists its own prerequisites.
