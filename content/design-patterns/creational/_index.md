---
title: "Creational Patterns"
description: "Patterns that decouple code from the concrete classes it instantiates."
weight: 10
planned:
  - "Singleton — one instance, thread safety, and why global state hurts testing"
  - "Abstract Factory — families of related objects"
  - "Prototype — creating objects by copying"
---

Creational patterns control *how* objects are created, so that client code depends on abstractions rather than on `new ConcreteClass()`. The GoF catalogue contains five: Singleton, Factory Method, Abstract Factory, Builder and Prototype. The [creational patterns comparison](/comparisons/factory-patterns) explains how they differ from one another and from the common "simple factory" idiom.
