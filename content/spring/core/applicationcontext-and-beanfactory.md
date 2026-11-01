---
title: "Inversion of Control: ApplicationContext and BeanFactory"
linkTitle: "ApplicationContext & BeanFactory"
description: "How the Spring IoC container works: BeanFactory vs ApplicationContext, bean definitions, the refresh() startup sequence, BeanFactoryPostProcessor and BeanPostProcessor extension points, dependency resolution (by type, @Qualifier, @Primary, collections, ObjectProvider), circular dependencies, context hierarchies, and AOT processing."
weight: 2
difficulty: intermediate
status: complete
javaVersion: 21
tags: [spring, ioc, applicationcontext, container]
keywords: [spring ioc container, applicationcontext vs beanfactory, bean definition, beanfactorypostprocessor, beanpostprocessor, applicationcontext refresh, qualifier primary, objectprovider, circular dependency spring, spring aot]
lastReviewed: 2026-11-01
prerequisites:
  - /spring/core/dependency-injection
related:
  - /spring/core/bean-lifecycle-and-scopes
  - /spring/core/configuration-and-component-scanning
---

## Simple definition

**Inversion of Control (IoC)** means your objects do not create or look up their collaborators: a **container** creates them, wires them and manages their lifecycle. In Spring, that container is the **`ApplicationContext`**, built on the lower-level **`BeanFactory`**.

## The problem

In a large application, wiring hundreds of objects by hand is repetitive and error-prone: the correct order, singletons, configuration, proxies for transactions and security, shutdown. See [Dependency Injection in Java](/spring/core/dependency-injection) for the plain-Java version. The container automates that **composition root**.

## BeanFactory vs ApplicationContext

| | `BeanFactory` | `ApplicationContext` |
|---|---|---|
| Role | Core DI engine: definitions, creation, wiring | Full container: a `BeanFactory` **plus** enterprise features |
| Singleton creation | Lazy (on first `getBean`) | **Eager** at startup (fail fast) |
| Post-processors | Must be registered manually | Detected and applied automatically |
| Events | ❌ | ✅ `ApplicationEventPublisher` |
| i18n messages | ❌ | ✅ `MessageSource` |
| Resources, environment, profiles | Basic | ✅ `ResourceLoader`, `Environment` |
| Use | Inside frameworks | **Always in applications** |

Spring Boot creates the right context for you: `AnnotationConfigServletWebServerApplicationContext` for MVC, the reactive equivalent for WebFlux, or a plain context for batch and CLI applications.

## Bean definitions: recipes before objects

A **`BeanDefinition`** describes how to create a bean: class or factory method, scope, constructor arguments, dependencies, init and destroy methods, lazy flag and primary flag. Definitions come from:

- `@Component`-family classes found by **component scanning**
- `@Bean` methods in `@Configuration` classes
- auto-configuration (Spring Boot)
- programmatic registration (`BeanRegistrar` in Spring 7, `registerBean`).

## Startup: what `refresh()` does

{{< interactive-diagram file="spring-container-startup.html" title="How the Spring ApplicationContext starts" source="diagrams/archify/spring-container-startup.workflow.json" tool="Archify" height="660" >}}

1. **Load bean definitions** (scanning, configuration classes, auto-configuration imports).
2. Run **`BeanFactoryPostProcessor`s**. They modify **definitions** before any bean exists. Example: `ConfigurationClassPostProcessor` processes `@Configuration`, and property placeholders are resolved.
3. Register **`BeanPostProcessor`s**.
4. **Instantiate non-lazy singletons** in dependency order. Inject dependencies, call `Aware` callbacks and init methods; **`BeanPostProcessor`s** may wrap each bean, for example in AOP proxies for `@Transactional`.
5. Publish `ContextRefreshedEvent`. Spring Boot then starts the web server, runs `ApplicationRunner`s and `CommandLineRunner`s, and publishes `ApplicationReadyEvent`.

| Extension point | Works on | Typical use |
|---|---|---|
| `BeanFactoryPostProcessor` / `BeanDefinitionRegistryPostProcessor` | Definitions | Register or modify definitions, process config classes |
| `BeanPostProcessor` | Bean instances | Wrap in proxies (AOP), process annotations (`@Autowired`, `@PostConstruct`) |
| `SmartInitializingSingleton` | After all singletons exist | Post-startup wiring |
| `ApplicationListener` | Events | React to lifecycle events |

## Resolving dependencies

Spring injects **by type**, then narrows down:

```java
@Service
class CheckoutService {
    CheckoutService(PaymentGateway gateway,                      // single candidate, or @Primary
                    @Qualifier("fraudRules") List<Rule> rules,   // all beans of the type, ordered by @Order
                    ObjectProvider<AuditClient> audit) {         // optional or lazy lookup
        ...
    }
}
```

| Situation | Resolution |
|---|---|
| Exactly one bean of the type | Injected |
| Several beans | `@Primary`, `@Qualifier`, or the parameter name matching the bean name; otherwise `NoUniqueBeanDefinitionException` |
| None | `NoSuchBeanDefinitionException`, unless the parameter is `Optional<T>`, `ObjectProvider<T>` or `@Autowired(required=false)` |
| `List<T>`, `Map<String,T>` | All candidates (ordered by `@Order` or `Ordered`) |
| Generic types | `Repository<Order>` vs `Repository<Customer>` resolved by generic type |

> [!KEY] Prefer constructor injection
> It makes dependencies explicit and fields final, allows testing without Spring, and turns circular dependencies into startup failures instead of hidden runtime problems.

## Circular dependencies

`A → B → A`:

- **Constructor injection:** fails (`BeanCurrentlyInCreationException`). That is good: it reveals a design problem.
- **Spring Boot 2.6+** forbids circular references by default, even with setter or field injection.
- **Fixes:** extract a third component, invert one dependency with an event or interface, or, as a last resort, use `@Lazy` on one injection point.

## Context hierarchies

A child context can see beans in its parent, but not the reverse. This appears in classic Spring MVC (root plus servlet context), in Spring Cloud (bootstrap and child contexts), and in tests. Most Spring Boot apps use a single context.

## AOT and native images

Spring's **Ahead-of-Time (AOT)** engine runs the bean-definition phase **at build time** and generates Java code for the definitions, plus reflection hints. Benefits: faster startup, GraalVM native images, and smaller memory with **CDS / Project Leyden AOT caches**. The trade-off: conditions are evaluated at build time, so the bean graph must be fixed.

## Common mistakes

> [!MISTAKE] Calling `context.getBean()` everywhere
> That is service location, not injection. It hides dependencies. Inject through constructors.

> [!MISTAKE] Heavy work in constructors
> Network calls or long computations at bean creation slow startup and fail obscurely. Use lifecycle callbacks or lazy initialisation.

> [!MISTAKE] Multiple candidates without a decision
> Two `DataSource` beans with no `@Primary` or `@Qualifier` fails startup. Decide explicitly.

## Quick check

**BeanFactory vs ApplicationContext?**
BeanFactory is the core DI engine. ApplicationContext adds eager singletons, automatic post-processor registration, events, i18n, resources and environment, and is what applications use.

**BeanFactoryPostProcessor vs BeanPostProcessor?**
The first modifies bean definitions before any bean is created. The second processes bean instances after creation, for example by wrapping them in proxies.

**How does Spring choose among several beans of the same type?**
`@Primary`, `@Qualifier`, or the parameter name; or you inject all of them as a `List` or `Map`.

**What happens with constructor-injection cycles?**
Startup fails with `BeanCurrentlyInCreationException`. Redesign, or use `@Lazy` as a last resort.

**What does Spring AOT do?**
It processes bean definitions at build time and generates code and hints for faster startup and GraalVM native images.

## Summary

- The ApplicationContext is the IoC container: definitions first, then instances.
- Extension points: BeanFactoryPostProcessors for definitions, BeanPostProcessors for instances (proxies).
- Inject by type and narrow with `@Primary` and `@Qualifier`; use `ObjectProvider` for optional or lazy dependencies.
- Avoid cycles; prefer constructor injection; AOT moves work to build time.

## References

- Spring Framework reference, [The IoC Container](https://docs.spring.io/spring-framework/reference/core/beans.html).
- Spring Framework reference, [Container extension points](https://docs.spring.io/spring-framework/reference/core/beans/factory-extension.html).
- Spring Boot reference, [Ahead-of-Time processing](https://docs.spring.io/spring-boot/reference/packaging/aot.html).
