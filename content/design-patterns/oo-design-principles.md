---
title: "OO Design Principles Behind the Patterns"
linkTitle: "OO design principles"
description: "The nine object-oriented design principles that the GoF patterns put into practice: encapsulate what varies, favour composition over inheritance, program to interfaces, loose coupling, open/closed, dependency inversion, least knowledge, the Hollywood Principle and single responsibility — each with the patterns that apply it."
weight: 3
difficulty: beginner
status: complete
javaVersion: 21
tags: [design-patterns, design-principles, oop, composition]
keywords: [composition over inheritance, encapsulate what varies, program to an interface, loose coupling, law of demeter, principle of least knowledge, hollywood principle, design principles head first]
lastReviewed: 2026-11-01
prerequisites:
  - /design-patterns/what-are-design-patterns
related:
  - /design-patterns/solid-principles
  - /design-patterns/gof-overview
  - /design-patterns/compound-patterns-mvc
---

## Simple definition

Design patterns are not a list of tricks to memorise. Almost all of them are **the same handful of principles applied to a particular problem**. Learn the principles and you can recognise patterns, adapt them, and know when *not* to use them.

The GoF book states two principles in its introduction: *"Program to an interface, not an implementation"* and *"Favor object composition over class inheritance."* *Head First Design Patterns* introduces nine principles, one or two per chapter, alongside the patterns that embody them. This page collects them.

## The principles at a glance

| # | Principle | In one sentence | Patterns that apply it |
|---|---|---|---|
| 1 | **Encapsulate what varies** | Separate the parts that change from the parts that stay the same | Strategy, State, Template Method, Factory Method, Bridge, Iterator |
| 2 | **Favour composition over inheritance** | Get behaviour by holding objects, not by extending classes | Strategy, Decorator, Bridge, Composite, State |
| 3 | **Program to an interface, not an implementation** | Depend on supertypes, so the concrete class can change | Almost all |
| 4 | **Strive for loosely coupled designs between objects that interact** | Objects should know as little as possible about each other | Observer, Mediator, Facade |
| 5 | **Classes should be open for extension but closed for modification** | Add behaviour with new code, not by editing tested code | Decorator, Strategy, Observer, Visitor |
| 6 | **Depend on abstractions, not concrete classes** (Dependency Inversion) | High- and low-level modules both depend on interfaces | Factory Method, Abstract Factory, Adapter |
| 7 | **Principle of Least Knowledge** (Law of Demeter) | Talk only to your immediate friends | Facade, Mediator |
| 8 | **The Hollywood Principle** | "Don't call us, we'll call you": high-level components call low-level ones | Template Method, Observer, frameworks and DI |
| 9 | **Single responsibility** | A class should have only one reason to change | Iterator, Command, Visitor, Decorator |

Principles 5, 6 and 9 are also the O, D and S of SOLID; see [SOLID Principles Explained](/design-patterns/solid-principles).

## 1. Encapsulate what varies

Find the parts of your code that change — for different customers, over time, per environment — and put them behind an interface, apart from the stable parts.

```java
// Before: the varying part (how to price shipping) is tangled with the stable part
BigDecimal shippingCost(Order o) {
    if (o.isExpress()) { ... } else if (o.isInternational()) { ... } else { ... }
}

// After: the variation is encapsulated
interface ShippingPolicy { BigDecimal cost(Order o); }
```

This is the first step of most behavioural patterns. [Strategy](/design-patterns/behavioral/strategy) encapsulates an algorithm, [State](/design-patterns/behavioral/state) encapsulates state-dependent behaviour, [Iterator](/design-patterns/behavioral/iterator) encapsulates traversal, and the factories encapsulate object creation.

## 2. Favour composition over inheritance

Inheritance is a compile-time, all-or-nothing relationship. A subclass inherits everything, including behaviour that does not fit, and is tightly coupled to its parent's implementation (the **fragile base class** problem).

Composition — holding a reference to another object and delegating to it — is chosen at runtime, can be swapped, and couples only to an interface.

| | Inheritance ("is-a") | Composition ("has-a") |
|---|---|---|
| Bound | At compile time | At runtime, can change |
| Coupling | To the parent's implementation | To an interface |
| Combinations | One class per combination (class explosion) | Combine objects freely |
| Encapsulation | Subclass sees protected internals ("white-box reuse") | Only the public interface ("black-box reuse") |

The canonical illustration is Head First's duck simulator: putting `fly()` in a `Duck` superclass makes rubber ducks fly. Pulling flying behaviour into a `FlyBehavior` object that each duck *has* fixes it, and lets a duck change how it flies at runtime.

```java
// Inheritance: every subclass of Duck inherits fly(), even RubberDuck
// Composition: behaviour is a part, not a parent
final class Duck {
    private FlyBehavior flyBehavior;
    Duck(FlyBehavior flyBehavior) { this.flyBehavior = flyBehavior; }
    void performFly() { flyBehavior.fly(); }
    void setFlyBehavior(FlyBehavior fb) { this.flyBehavior = fb; }
}
```

> [!KEY] Inheritance is still right sometimes
> Use inheritance for genuine "is-a" relationships where the subclass honours the parent's whole contract (Liskov Substitution), and where the parent is designed for extension, as in [Template Method](/design-patterns/behavioral/template-method). Effective Java's rule: *design and document for inheritance or else prohibit it*.

[Decorator](/design-patterns/structural/decorator), [Bridge](/design-patterns/structural/bridge) and [Composite](/design-patterns/structural/composite) are all composition replacing inheritance.

## 3. Program to an interface, not an implementation

"Interface" here means a **supertype**: a Java interface or an abstract class. Variables, parameters and return types should use the most abstract type that offers what you need.

```java
List<Order> orders = new ArrayList<>();          // not ArrayList<Order> orders
void notify(MessageSender sender) { ... }         // not EmailSender
```

The concrete class then appears in one place — where the object is created — which is exactly what creational patterns and [dependency injection](/spring/core/dependency-injection) manage.

## 4. Loose coupling between interacting objects

Two objects are loosely coupled when they can interact while knowing very little about each other. In [Observer](/design-patterns/behavioral/observer), the subject knows only that observers implement one method. You can add, remove or replace observers without changing the subject, and reuse either side elsewhere.

Ways to loosen coupling: depend on narrow interfaces, pass data rather than objects, use events instead of direct calls, and put coordination in a [Mediator](/design-patterns/behavioral/mediator).

## 5. Open for extension, closed for modification

You should be able to add behaviour without editing existing, tested classes. [Decorator](/design-patterns/structural/decorator) adds behaviour by wrapping; [Strategy](/design-patterns/behavioral/strategy) by plugging in a new class; [Visitor](/design-patterns/behavioral/visitor) adds operations without touching elements.

Apply it to the parts that are *likely* to change. Making everything extensible adds indirection for no benefit.

## 6. Depend on abstractions (Dependency Inversion)

High-level policy (checkout) and low-level detail (a payment SDK) should both depend on an abstraction (`PaymentGateway`) owned by the high-level side. This inverts the usual "top calls bottom" dependency and is the basis of [Hexagonal Architecture](/hexagonal-architecture/layered-vs-hexagonal).

Head First gives three guidelines that help: no variable should hold a reference to a concrete class (use a factory); no class should derive from a concrete class; no method should override an implemented method of its base class. They are guidelines, not laws — `String` and `ArrayList` are concrete classes everyone uses.

## 7. Principle of Least Knowledge (Law of Demeter)

A method should only call methods on: itself, its parameters, objects it creates, and its fields. Not on objects *returned* by those calls.

```java
// Talks to a stranger (the station's thermometer)
float temp = station.getThermometer().getTemperature();

// Talks to a friend
float temp = station.getTemperature();
```

Long call chains couple your code to the internal structure of many classes. A [Facade](/design-patterns/structural/facade) gives clients one friend to talk to. Fluent builders and streams (`list.stream().map(...).toList()`) are not violations: each call returns the same kind of object you already talk to.

## 8. The Hollywood Principle

*"Don't call us, we'll call you."* Low-level components hook into a system, but the high-level components decide when to call them. This prevents "dependency rot", where high-level and low-level components depend on each other in tangled ways.

[Template Method](/design-patterns/behavioral/template-method) calls subclass steps; [Observer](/design-patterns/behavioral/observer) calls registered listeners; Spring calls your beans' lifecycle methods and controllers. It is the principle behind **inversion of control**.

## 9. Single responsibility

A class should have one reason to change. [Iterator](/design-patterns/behavioral/iterator) takes traversal out of collections; [Command](/design-patterns/behavioral/command) takes an operation out of the invoker; [Visitor](/design-patterns/behavioral/visitor) takes operations out of a data hierarchy. See [SRP in the SOLID article](/design-patterns/solid-principles).

## How the principles become patterns

```mermaid {title="Principles applied to specific problems produce patterns"}
flowchart LR
    P1[Encapsulate what varies] --> S[Strategy]
    P2[Composition over inheritance] --> S
    P3[Program to an interface] --> S
    P2 --> D[Decorator]
    P5[Open/Closed] --> D
    P4[Loose coupling] --> O[Observer]
    P8[Hollywood Principle] --> O
    P8 --> T[Template Method]
    P7[Least Knowledge] --> F[Facade]
    P6[Dependency Inversion] --> AF[Abstract Factory]
```

## Common mistakes

> [!MISTAKE] Treating principles as absolute rules
> Every principle has a cost: more interfaces, more indirection, more files. Apply them where change is real or likely. A five-line script does not need dependency inversion.

- **Composition for "has-a" that is really "is-a"**, producing awkward delegation of every method.
- **Interfaces with a single implementation everywhere "just in case"**. Introduce an interface when there is a second implementation, a test double, or a module boundary.
- **Reading the Law of Demeter as "count the dots"**: fluent APIs and data-structure navigation are fine.

## Quick check

**What are the two principles stated in the GoF introduction?**
Program to an interface, not an implementation; and favour object composition over class inheritance.

**Why does composition usually beat inheritance?**
It is chosen at runtime, couples only to an interface, avoids class explosion and avoids the fragile base class problem.

**What does the Principle of Least Knowledge say?**
Only call methods on yourself, your parameters, objects you create and your fields, not on objects returned by other calls.

**Which pattern is the clearest example of the Hollywood Principle?**
Template Method: the base class calls the subclass's steps, never the reverse.

**Which principle is the first step of Strategy, State and the factories?**
Encapsulate what varies.

## Summary

- Patterns are principles applied to recurring problems.
- The central ideas: encapsulate what varies, compose rather than inherit, depend on interfaces.
- Loose coupling, least knowledge and the Hollywood Principle control who knows and calls whom.
- Apply principles where change is real; each has a cost.

## Related topics

- [SOLID Principles Explained](/design-patterns/solid-principles)
- [All 23 GoF Patterns: Overview](/design-patterns/gof-overview)
- [Compound patterns and MVC](/design-patterns/compound-patterns-mvc)

## References

- Gamma, Helm, Johnson, Vlissides. *Design Patterns*. Addison-Wesley, 1994. Chapter 1, "Putting reuse mechanisms to work".
- Eric Freeman, Elisabeth Robson. *Head First Design Patterns* (2nd edition). O'Reilly, 2020. The design principles introduced in chapters 1–9.
- Joshua Bloch. *Effective Java* (3rd edition). Item 18: Favor composition over inheritance; Item 19: Design and document for inheritance or else prohibit it.
