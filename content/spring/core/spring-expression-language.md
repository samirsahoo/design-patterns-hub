---
title: "Spring Expression Language (SpEL)"
linkTitle: "SpEL"
description: "What SpEL is and where Spring uses it: syntax (literals, properties, methods, operators, collection selection and projection, safe navigation, Elvis), bean references, @Value with ${} vs #{}, SpEL in security, caching and scheduling annotations, evaluating expressions programmatically, compilation, and security risks of user-supplied expressions."
weight: 6
difficulty: intermediate
status: complete
javaVersion: 21
tags: [spring, spel, expressions]
keywords: [spring expression language, spel syntax, value annotation spel, property placeholder vs spel, spel preauthorize, spel cacheable key, spel safe navigation elvis, spel collection selection projection, spelexpressionparser, spel injection vulnerability]
lastReviewed: 2026-11-01
prerequisites:
  - /spring/core/configuration-and-component-scanning
related:
  - /spring/security/method-level-security
  - /spring/data/caching
---

## Simple definition

**SpEL (Spring Expression Language)** is a small expression language that Spring evaluates **at runtime** against objects, beans and context. It powers dynamic values in annotations such as `@Value("#{…}")`, `@PreAuthorize("…")`, `@Cacheable(key = "…")` and `@EventListener(condition = "…")`.

## The problem

Annotations are static, but many decisions are dynamic: "allow if the user owns this order", "cache by customer ID", "listen only to large orders". SpEL lets those rules be written **declaratively**, next to the code they affect.

## `${…}` vs `#{…}`

| Syntax | What it is | Example |
|---|---|---|
| `${…}` | **Property placeholder**, resolved from the `Environment` (properties, env vars) | `@Value("${shop.page-size:20}")` |
| `#{…}` | **SpEL expression**, evaluated at runtime | `@Value("#{T(java.time.Duration).ofSeconds(${shop.timeout-seconds})}")` |

You can nest a placeholder inside SpEL, as shown. For configuration, prefer type-safe [`@ConfigurationProperties`](/spring/boot/externalized-configuration) over scattered `@Value`s.

## Syntax tour

| Feature | Example | Result |
|---|---|---|
| Literals | `'text'`, `42`, `true`, `null` | — |
| Properties and methods | `order.customer.name`, `name.toUpperCase()` | Navigation and calls |
| Operators | `total > 100 and status == 'PAID'`, `+ - * / %`, `matches` (regex) | Boolean and arithmetic |
| Ternary | `vip ? 0.1 : 0.0` | — |
| **Elvis** | `nickname ?: 'guest'` | Default if null |
| **Safe navigation** | `customer?.address?.city` | null instead of an NPE |
| Types and statics | `T(java.lang.Math).max(a, b)` | Static access |
| Bean references | `@pricingService.discountFor(#customer)` | Call a bean |
| Variables | `#order`, `#root`, `#this` | Context variables and method arguments |
| Collection **selection** | `orders.?[total > 100]` | Filter (`^[…]` first match, `$[…]` last) |
| Collection **projection** | `orders.![id]` | Map to IDs |
| Inline lists and maps | `{1, 2, 3}`, `{currency: 'EUR'}` | — |

## Where Spring uses SpEL

```java
// Method security: argument and return-value checks
@PreAuthorize("hasRole('ADMIN') or #customerId == authentication.name")
Orders listOrders(String customerId) { ... }

@PostAuthorize("returnObject.ownerId == authentication.name")
Order getOrder(long id) { ... }

// Caching: computed keys and conditions
@Cacheable(cacheNames = "prices", key = "#sku + ':' + #currency", unless = "#result == null")
Price price(String sku, String currency) { ... }

// Events: conditional listeners
@EventListener(condition = "#event.total > 1000")
void onLargeOrder(OrderPlaced event) { ... }

// Scheduling with a property
@Scheduled(cron = "${jobs.reconcile.cron}")
void reconcile() { ... }
```

Spring Data also supports SpEL in `@Query` (`:#{#entityName}`), and Spring Integration and Spring Cloud Stream use SpEL for routing and headers.

## Evaluating expressions in code

```java
ExpressionParser parser = new SpelExpressionParser();
Expression rule = parser.parseExpression("total > 500 and customer.country == 'IN'");

var context = SimpleEvaluationContext.forReadOnlyDataBinding().build();   // restricted
boolean flagged = rule.getValue(context, order, Boolean.class);
```

- Parse once and **reuse** the `Expression` objects (parsing is the costly part).
- **`SimpleEvaluationContext`** is restricted: no type references, constructors or bean references. **`StandardEvaluationContext`** allows everything.
- **Compilation:** `SpelCompilerMode.IMMEDIATE` or `MIXED` compiles hot expressions to bytecode for speed.

## Security warning: SpEL injection

> [!WARNING] Never evaluate untrusted input with StandardEvaluationContext
> An expression such as `T(java.lang.Runtime).getRuntime().exec('…')` executes arbitrary code. SpEL injection has caused real remote-code-execution CVEs in applications and libraries.
> - Never build expressions from user input.
> - If you must evaluate user-defined rules, use `SimpleEvaluationContext`, a strict allow-list, or a dedicated rules engine.

## When not to use SpEL

- Complex business logic: put it in a Java method and call it (`@authz.canEdit(#order)`). That keeps it testable and refactorable.
- Configuration values: use `@ConfigurationProperties`.

## Common mistakes

> [!MISTAKE] Confusing `${}` and `#{}`
> `${}` reads properties; `#{}` evaluates expressions. `@Value("#{shop.size}")` looks for a bean named `shop`.

> [!MISTAKE] Long expressions in annotations
> They are untyped strings that refactoring tools cannot follow. Move the logic into a bean method.

> [!MISTAKE] Parameter names not available
> `#customerId` needs parameter names, so compile with `-parameters` (Spring Boot's build plugins do).

## Quick check

**`${…}` vs `#{…}`?**
`${…}` is a property placeholder resolved from the Environment. `#{…}` is a SpEL expression evaluated at runtime.

**What does `customer?.address?.city` do?**
Safe navigation: it returns null instead of throwing NullPointerException if any part is null.

**Name three annotations that accept SpEL.**
`@PreAuthorize` and `@PostAuthorize`, `@Cacheable` (`key`, `condition`, `unless`), `@EventListener(condition)` and `@Value("#{…}")`.

**Why is evaluating user input with SpEL dangerous?**
StandardEvaluationContext allows type references and method calls, which enables arbitrary code execution.

**How do you call a bean in an expression?**
With `@beanName.method(...)`, for example `@authz.canEdit(#order)`.

## Summary

- SpEL evaluates runtime expressions; `${}` is just property lookup.
- Spring uses it in security, caching, events, scheduling and data annotations.
- Keep expressions short; delegate logic to beans; never evaluate untrusted input.

## References

- Spring Framework reference, [Spring Expression Language (SpEL)](https://docs.spring.io/spring-framework/reference/core/expressions.html).
- Spring Security reference, [Method security expressions](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html).
