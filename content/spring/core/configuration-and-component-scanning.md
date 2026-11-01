---
title: "Spring Configuration: @Configuration, @Bean, Component Scanning and Profiles"
linkTitle: "Configuration & scanning"
description: "Defining beans in Spring: stereotype annotations and component scanning, @Configuration and @Bean methods, full vs lite mode and proxyBeanMethods, @Import, profiles and @Profile, programmatic registration with BeanRegistrar (Spring 7), and how to organise configuration in large applications."
weight: 4
difficulty: beginner
status: complete
javaVersion: 21
tags: [spring, configuration, component-scanning]
keywords: [spring configuration annotation, bean annotation, component scanning, stereotype annotations service repository controller, proxybeanmethods, configuration lite mode, spring import, spring profiles, beanregistrar spring 7, java config vs xml]
lastReviewed: 2026-11-01
prerequisites:
  - /spring/core/applicationcontext-and-beanfactory
related:
  - /spring/boot/auto-configuration-and-conditions
  - /spring/boot/externalized-configuration
---

## Simple definition

Spring learns **which beans to create** in three main ways:

1. **Component scanning**: annotate your classes (`@Component`, `@Service` and so on) and Spring finds them.
2. **Java configuration**: `@Bean` factory methods inside `@Configuration` classes, ideal for third-party classes you cannot annotate.
3. **Auto-configuration**: Spring Boot's conditional defaults. See [auto-configuration](/spring/boot/auto-configuration-and-conditions).

## The problem

Without a clear configuration strategy, applications end up with duplicate beans, surprising overrides, beans scanned from the wrong packages, and environment-specific code paths scattered everywhere.

## Component scanning and stereotypes

```java
@SpringBootApplication           // = @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan
public class ShopApplication { public static void main(String[] a) { SpringApplication.run(ShopApplication.class, a); } }
```

Scanning starts from the **package of the annotated class** and covers sub-packages. Put the main class in the **root package**.

| Annotation | Meaning | Extra behaviour |
|---|---|---|
| `@Component` | Generic Spring-managed bean | — |
| `@Service` | Business logic | None (it documents intent) |
| `@Repository` | Data access | **Exception translation** to `DataAccessException` |
| `@Controller` / `@RestController` | Web handlers | MVC mapping; `@ResponseBody` for REST |
| `@Configuration` | Bean definitions | Proxied for inter-bean method calls |

Narrow scanning with `@ComponentScan(basePackages = …, excludeFilters = …)` only when needed. Scanning everything (`com`) slows startup and picks up surprises.

## Java configuration with @Bean

```java
@Configuration
class PaymentConfig {

    @Bean
    RestClient paymentRestClient(RestClient.Builder builder, PaymentProperties props) {   // dependencies as parameters
        return builder.baseUrl(props.baseUrl())
                      .defaultHeader("X-Client", "shop")
                      .build();
    }

    @Bean
    PaymentGateway paymentGateway(RestClient paymentRestClient) {
        return new HttpPaymentGateway(paymentRestClient);
    }
}
```

- The method name is the bean name (`paymentGateway`). Override it with `@Bean("name")`.
- Use `initMethod` and `destroyMethod` for third-party lifecycle hooks. Spring infers public `close()` and `shutdown()` methods automatically.
- `@Bean` methods can declare `@Scope`, `@Lazy`, `@Primary`, `@Qualifier`, `@Order` and `@Conditional*`.

### Full mode vs lite mode (proxyBeanMethods)

```java
@Configuration                          // proxyBeanMethods = true (full mode, the default)
class AppConfig {
    @Bean Clock clock() { return Clock.systemUTC(); }
    @Bean Scheduler scheduler() { return new Scheduler(clock()); }   // returns the SAME singleton Clock
}
```

- **Full mode:** Spring subclasses the configuration class with CGLIB, so calling `clock()` returns the container's singleton.
- **Lite mode** (`@Configuration(proxyBeanMethods = false)` or `@Bean` in a `@Component`): no proxy, and calling `clock()` creates a **new instance**. Spring Boot's own auto-configurations use lite mode for faster startup. Inject dependencies as **method parameters** instead of calling other `@Bean` methods.

> [!TIP] Prefer parameters over inter-bean calls
> Declaring dependencies as `@Bean` method parameters works in both modes, is explicit, and enables `proxyBeanMethods = false`.

## Composing configuration

- `@Import(SecurityConfig.class)` pulls in other configuration classes, `ImportSelector`s or registrars.
- **Enable-style annotations** (`@EnableScheduling`, `@EnableCaching`, `@EnableAsync`) are `@Import`s under the hood.
- In Spring Boot, prefer **auto-configuration** for reusable modules (your own starters), so they back off when the application defines its own beans.

## Profiles

Profiles activate beans per environment:

```java
@Configuration
@Profile("local")
class LocalStorageConfig { @Bean FileStore fileStore() { return new DiskFileStore(Path.of("/tmp/shop")); } }

@Configuration
@Profile("!local")
class CloudStorageConfig { @Bean FileStore fileStore(S3Client s3) { return new S3FileStore(s3); } }
```

- Activate them with `spring.profiles.active=prod`, the `SPRING_PROFILES_ACTIVE` environment variable, or `--spring.profiles.active`.
- Expressions: `@Profile("prod & !eu")`, `@Profile({"dev", "test"})`.
- **Profile groups:** `spring.profiles.group.prod=prod-db,prod-mq`.
- Keep profiles for **wiring differences**. Put **values** in [externalized configuration](/spring/boot/externalized-configuration), not in code branches.

## Programmatic registration (Spring Framework 7)

Spring 7 introduces **`BeanRegistrar`**, a flexible, AOT-friendly way to register beans in code. It is useful for registering a variable number of beans:

```java
class ClientsRegistrar implements BeanRegistrar {
    @Override
    public void register(BeanRegistry registry, Environment env) {
        for (String name : env.getProperty("shop.partners", String[].class, new String[0])) {
            registry.registerBean(name + "Client", PartnerClient.class,
                    spec -> spec.supplier(ctx -> new PartnerClient(name)));
        }
    }
}

@Configuration
@Import(ClientsRegistrar.class)
class PartnerConfig { }
```

## Organising configuration in large apps

- One configuration class per **concern** (security, messaging, persistence, clients), next to the code it configures.
- Keep domain classes free of Spring annotations where practical. See [hexagonal architecture](/hexagonal-architecture/spring-boot-implementation).
- Bean overriding is **disabled by default** in Spring Boot (`spring.main.allow-bean-definition-overriding=false`). Duplicate names fail fast; keep it that way.

## Common mistakes

> [!MISTAKE] Main class in a sub-package
> Scanning misses sibling packages, so beans are "not found". Put `@SpringBootApplication` in the root package.

> [!MISTAKE] Calling `@Bean` methods in lite mode
> Each call creates a new instance, so there are two "singletons". Use method parameters.

> [!MISTAKE] Business logic branching on profiles
> `if (env.acceptsProfiles("prod"))` in services spreads environment knowledge. Use profile-specific beans or properties.

## Quick check

**What does `@SpringBootApplication` combine?**
`@SpringBootConfiguration` (a `@Configuration`), `@EnableAutoConfiguration` and `@ComponentScan` from its package.

**When use `@Bean` instead of `@Component`?**
For third-party classes you cannot annotate, for objects that need construction logic, or to keep domain classes free of Spring annotations.

**What does `proxyBeanMethods = false` change?**
The configuration class is not CGLIB-proxied, so calling one `@Bean` method from another creates a new object. Inject dependencies as parameters instead.

**What extra behaviour does `@Repository` add?**
Translation of persistence exceptions into Spring's `DataAccessException` hierarchy.

**How do you activate a profile?**
With `spring.profiles.active` in properties, an environment variable or a command-line argument. `@Profile` then selects beans.

## Summary

- Scan your own classes; use `@Bean` for third-party and complex construction.
- Prefer method parameters to inter-bean calls; lite mode is faster.
- Use profiles for wiring differences and properties for values.
- Spring 7's `BeanRegistrar` handles dynamic registration.

## References

- Spring Framework reference, [Java-based Container Configuration](https://docs.spring.io/spring-framework/reference/core/beans/java.html).
- Spring Framework reference, [Classpath Scanning and Managed Components](https://docs.spring.io/spring-framework/reference/core/beans/classpath-scanning.html).
- Spring Boot reference, [Profiles](https://docs.spring.io/spring-boot/reference/features/profiles.html).
- Spring Framework wiki, [What's New in Spring Framework 7.x](https://github.com/spring-projects/spring-framework/wiki/What's-New-in-Spring-Framework-7.x).
