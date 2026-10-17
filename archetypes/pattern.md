---
title: "{{ replace .File.ContentBaseName "-" " " | title }} Pattern"
linkTitle: "{{ replace .File.ContentBaseName "-" " " | title }}"
description: ""
weight: 100        # use the GoF catalogue number (1–23) for ordering within the category
difficulty: intermediate
status: draft
draft: true
javaVersion: 21
tags: [design-patterns, gof]   # add: creational | structural | behavioral, and the pattern slug
keywords: []
lastReviewed: {{ now.Format "2006-01-02" }}
prerequisites:
  - /design-patterns/solid-principles
related:
  - /design-patterns/gof-overview
---

<!-- Create with: hugo new --kind pattern design-patterns/<category>/<slug>.md
     Put the runnable example in examples/java/src/hub/patterns/<slug>/ with a
     <Name>Demo main class, then run: python scripts/verify_java.py --update
     and review examples/java/expected/hub.patterns.<slug>.<Name>Demo.txt -->

## Simple definition

GoF intent: *""*

## The problem

## Real-world analogy

## Structure

<!-- Roles, then a Mermaid classDiagram whose class and method names match the example code. -->

```mermaid {title="<Pattern> — class diagram of the example"}
classDiagram
```

<!-- Add a sequenceDiagram when the runtime collaboration is not obvious. -->

## Java implementation

{{ "{{<" }} code file="examples/java/src/hub/patterns/<slug>/<File>.java" {{ ">}}" }}

### Running it

{{ "{{<" }} code file="examples/java/expected/hub.patterns.<slug>.<Name>Demo.txt" lang="text" title="Output" {{ ">}}" }}

## Step-by-step walkthrough

## Advantages and disadvantages

| Advantages | Disadvantages |
|---|---|

## When to use it

## When not to use it

## Common implementation mistakes

## Relationship to SOLID

## Comparison with similar patterns

## Real-world uses

## Interview questions and answers

## Summary

## Related topics

## References

- Gamma, Helm, Johnson, Vlissides. *Design Patterns*. Addison-Wesley, 1994.
