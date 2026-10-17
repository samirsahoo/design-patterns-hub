---
title: "About this handbook"
linkTitle: "About"
description: "Purpose, editorial conventions, versions, diagram strategy and how content status is tracked."
status: complete
lastReviewed: 2026-10-17
excludeFromSearch: false
tags: [reference]
---

## Purpose

The **Java & Software Architecture Knowledge Hub** is an open, maintained handbook for two audiences:

1. Developers preparing for technical interviews or keeping their knowledge current.
2. Anyone looking for a precise, practical reference on Java, design patterns, architecture, Domain-Driven Design, microservices and system design.

It favours depth over breadth: an article is published only when it is complete. Planned topics are listed on each section page as plain text, never as links to empty pages.

## Editorial conventions

Every substantial article follows the same structure:

1. Simple definition
2. The problem
3. Why it matters
4. Core concept, with a diagram
5. Practical example and implementation walkthrough
6. Real-world applications
7. Advantages and disadvantages
8. Alternatives and comparisons
9. Common mistakes
10. Interview questions and answers
11. Summary, related topics and references

Architecture articles add requirements and assumptions, component responsibilities, data flow, failure modes, and operational and security considerations.

Callouts mark special content:

> [!KEY] Key concept
> The idea to remember if you remember nothing else.

> [!INTERVIEW] Interview tip
> How to use the idea in an interview.

> [!MISTAKE] Common mistake
> What goes wrong in practice.

> [!TRADEOFF] Trade-off
> What you give up, and when that is acceptable.

> [!VERSION] Version-specific behaviour
> Behaviour that depends on a Java or Spring version.

## Versions

| Technology | Version used in examples |
|---|---|
| Java | {{< param "javaVersion" >}} (LTS) |
| Spring Boot | {{< param "springBootVersion" >}} (Spring Framework 7) |
| Mermaid | {{< param "mermaidVersion" >}} (bundled with the site, no CDN) |

Features finalised after Java 21 are labelled with the release that finalised them. Spring-specific behaviour links to the official reference documentation.

## Code examples

Code shown in pattern and architecture articles is **included from source files** in the repository's `examples/` directory, not pasted into the Markdown. The build pipeline:

- compiles every plain-Java example for Java 21 with compiler warnings treated as errors, runs each demo and compares its output with the expected output stored in the repository;
- runs `mvn verify` for the Spring Boot example, including its unit, slice and end-to-end tests.

An article therefore cannot show code that does not compile, or output that the code does not produce.

## Diagrams

- **Mermaid** is the default. Diagrams are written as text inside the articles, reviewed in pull requests, and rendered in the browser by a copy of Mermaid served from this site.
- **Every diagram is explained in the text.** UML class and sequence diagrams match the example code they describe.
- **Standalone interactive diagrams** may be added for complex architectures. They are generated with [Archify](https://github.com/tt-a1i/archify), an open-source (MIT-licensed) command-line tool that renders a JSON specification into a self-contained HTML file. The JSON source is committed under `diagrams/archify/` and the generated HTML under `static/diagrams/`. The site does not depend on Archify at build time; it is an authoring tool only, and every interactive diagram has a Mermaid equivalent in its article.

## Content status

Each article's front matter has a `status` field. Only complete articles are published as pages. The repository's `CONTENT-STATUS.md` tracks progress for every topic in the curriculum.

## Accuracy and corrections

The articles are written carefully and reviewed, but they are not a substitute for official documentation. Each article lists references; where behaviour depends on a version, follow the linked official documentation. If you find an error, use the "Edit it on GitHub" or "open an issue" links at the bottom of each article.

## Licence

Text content is licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). Code examples are licensed under the MIT License. See the `LICENSE` file in the repository for details. Mermaid is distributed under the MIT License; its licence file is served alongside it.
