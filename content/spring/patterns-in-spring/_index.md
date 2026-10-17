---
title: "Patterns in Spring"
description: "Where Spring implements, resembles, or simply benefits from classic design patterns."
weight: 60
planned:
  - "Dependency Injection and the container"
  - "Factory Method: `@Bean` methods and `FactoryBean`"
  - "Proxy: AOP, `@Transactional` and self-invocation"
  - "Template Method: `JdbcTemplate` and callbacks"
  - "Strategy: pluggable interfaces such as `PasswordEncoder`"
  - "Observer: application events"
  - "Adapter: `HandlerAdapter` in Spring MVC"
  - "Decorator: wrapping beans with `BeanPostProcessor`"
  - "Chain of Responsibility: the servlet filter chain and Spring Security"
---

Each planned article states which of three cases applies:
- Spring *implements* the pattern.
- Spring's design *resembles* the pattern.
- The pattern is *useful in* Spring applications.

Labelling every abstraction a "GoF pattern" teaches the wrong lesson.
