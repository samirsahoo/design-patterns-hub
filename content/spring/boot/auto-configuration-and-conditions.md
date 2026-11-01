---
title: "Spring Boot Auto-Configuration and @ConditionalOn* Annotations"
linkTitle: "Auto-configuration & conditions"
description: "How Spring Boot auto-configuration works: starters, @EnableAutoConfiguration, the AutoConfiguration.imports file, ordering, the full family of @ConditionalOn* annotations, back-off with @ConditionalOnMissingBean, the conditions evaluation report, Spring Boot 4 modular auto-configuration, and writing your own starter."
weight: 1
difficulty: intermediate
status: complete
javaVersion: 21
tags: [spring-boot, auto-configuration, conditions]
keywords: [spring boot auto configuration, how auto configuration works, autoconfiguration imports file, conditionalonclass, conditionalonmissingbean, conditionalonproperty, conditional annotations spring boot, conditions evaluation report debug, custom spring boot starter, spring boot 4 modularization]
lastReviewed: 2026-11-01
prerequisites:
  - /spring/core/configuration-and-component-scanning
related:
  - /spring/boot/externalized-configuration
  - /spring/boot/actuator-and-custom-endpoints
---

## Simple definition

**Auto-configuration** is Spring Boot's mechanism for registering **sensible default beans** based on what is on the classpath, which beans you have already defined, and your properties. Each default is guarded by **conditions** (`@ConditionalOn…`), so it **backs off** as soon as you configure your own.

## The problem

A plain Spring web app with JPA needs dozens of beans: `DataSource`, `EntityManagerFactory`, `TransactionManager`, `DispatcherServlet`, the Jackson `ObjectMapper`, validation, an embedded server. Boot creates all of them, but **only if you haven't** and **only if the libraries are present**.

## How it works

{{< interactive-diagram file="auto-configuration.html" title="How Spring Boot auto-configuration decides" source="diagrams/archify/auto-configuration.workflow.json" tool="Archify" height="660" >}}

1. `@SpringBootApplication` includes `@EnableAutoConfiguration`.
2. Boot loads candidate classes listed in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` from every JAR on the classpath.
3. **Your** configuration (scanned components, `@Bean`s) is processed **first**. Auto-configuration classes are deferred until after user configuration.
4. Each candidate's **conditions** are evaluated. Classes and `@Bean` methods whose conditions fail are skipped.
5. Ordering between auto-configurations uses `@AutoConfiguration(before = …, after = …)`.

```java
@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@ConditionalOnClass(JdbcTemplate.class)                     // library present?
@ConditionalOnSingleCandidate(DataSource.class)              // exactly one DataSource?
@EnableConfigurationProperties(JdbcProperties.class)
public class JdbcTemplateAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JdbcOperations.class)          // back off if the user defined one
    JdbcTemplate jdbcTemplate(DataSource dataSource, JdbcProperties properties) {
        var t = new JdbcTemplate(dataSource);
        t.setFetchSize(properties.getTemplate().getFetchSize());
        return t;
    }
}
```

**Starters** (`spring-boot-starter-web` and so on) are dependency bundles. They bring the libraries, and the libraries' presence triggers the matching auto-configuration.

> [!VERSION] Spring Boot 4: modular auto-configuration
> Spring Boot 4 split the big `spring-boot-autoconfigure` JAR into **focused modules** (for example `spring-boot-webmvc`, `spring-boot-data-jpa`, `spring-boot-kafka`), each with its own starter. Applications that use starters see little change. Builds pull in less code, and modules are clearer. Check the migration guide if you depended on auto-configure classes directly.

## The conditions

| Annotation | Matches when… |
|---|---|
| `@ConditionalOnClass` / `@ConditionalOnMissingClass` | A class is (or is not) on the classpath |
| `@ConditionalOnBean` / `@ConditionalOnMissingBean` | A bean of a type or name exists (or does not) **at that point** |
| `@ConditionalOnSingleCandidate` | Exactly one bean of the type, or one primary |
| `@ConditionalOnProperty` | A property has a value (`havingValue`, `matchIfMissing`) |
| `@ConditionalOnBooleanProperty` (Boot 3.5+) | A boolean property is true or false |
| `@ConditionalOnResource` | A resource exists (`classpath:banner.txt`) |
| `@ConditionalOnWebApplication` / `@ConditionalOnNotWebApplication` | Servlet or reactive web app (`type = SERVLET`) |
| `@ConditionalOnExpression` | A SpEL expression is true (prefer specific conditions) |
| `@ConditionalOnJava` | The Java version is in a range |
| `@ConditionalOnCloudPlatform` | Running on Kubernetes, Cloud Foundry and so on |
| `@ConditionalOnThreading` | Virtual or platform threads are enabled |
| `@ConditionalOnCheckpointRestore` | CRaC checkpoint/restore |
| `@Conditional(MyCondition.class)` | Custom logic implementing `Condition` |

> [!KEY] Back-off is the core idea
> `@ConditionalOnMissingBean` makes your bean **win**: define your own `ObjectMapper`, `DataSource` or `SecurityFilterChain`, and Boot's default disappears. Customisers (`Jackson2ObjectMapperBuilderCustomizer`, `RestClientCustomizer`) let you **tweak** defaults instead of replacing them.

**`@ConditionalOnBean` caveat:** it depends on the beans registered **so far**. Use it in auto-configuration classes (ordered after user configuration), not in your own regular configuration.

## Seeing what happened

- Run with `--debug` (or `debug=true`) to print the **Conditions Evaluation Report**: positive matches, negative matches and the reason for each.
- Actuator's `/actuator/conditions` endpoint shows the same at runtime.
- Exclude an auto-configuration with `@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)` or `spring.autoconfigure.exclude=…`.

```text
DataSourceAutoConfiguration matched:
   - @ConditionalOnClass found required classes 'javax.sql.DataSource', 'EmbeddedDatabaseType'
JmsAutoConfiguration:
   Did not match:
      - @ConditionalOnClass did not find required class 'jakarta.jms.ConnectionFactory'
```

## Writing your own starter

For a shared company concern (audit client, standard security, tracing conventions):

```text
acme-audit-spring-boot-autoconfigure/
  src/main/java/.../AcmeAuditAutoConfiguration.java       # @AutoConfiguration + conditions
  src/main/java/.../AcmeAuditProperties.java              # @ConfigurationProperties("acme.audit")
  src/main/resources/META-INF/spring/
     org.springframework.boot.autoconfigure.AutoConfiguration.imports   # one class name per line
acme-audit-spring-boot-starter/                           # pom only: depends on autoconfigure + libraries
```

**Rules:**

- Every bean gets `@ConditionalOnMissingBean`.
- Guard optional libraries with `@ConditionalOnClass`.
- Expose properties with metadata (the `spring-boot-configuration-processor`).
- Never component-scan inside auto-configuration.
- Test with `ApplicationContextRunner`.

```java
new ApplicationContextRunner()
    .withConfiguration(AutoConfigurations.of(AcmeAuditAutoConfiguration.class))
    .withPropertyValues("acme.audit.enabled=true")
    .run(ctx -> assertThat(ctx).hasSingleBean(AuditClient.class));
```

## Common mistakes

> [!MISTAKE] Fighting auto-configuration with exclusions everywhere
> First check whether a property or a customiser solves it. Exclusions remove whole feature sets.

> [!MISTAKE] `@ConditionalOnBean` in user configuration
> Your configuration is processed before auto-configuration, so the bean you test for may not exist yet.

> [!MISTAKE] Component scanning a library's packages
> It bypasses conditions and ordering. Libraries should provide auto-configuration.

## Quick check

**Where does Spring Boot find auto-configuration classes?**
In `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` files on the classpath.

**Why does your own bean replace Boot's default?**
Boot's beans are guarded by `@ConditionalOnMissingBean`, and user configuration is processed first.

**How do you see why an auto-configuration did not apply?**
Run with `--debug` to get the Conditions Evaluation Report, or use `/actuator/conditions`.

**Name four conditional annotations.**
`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`, `@ConditionalOnWebApplication`, `@ConditionalOnSingleCandidate`, `@ConditionalOnResource`.

**What changed in Spring Boot 4 auto-configuration packaging?**
Auto-configuration was split into focused modules with matching starters, instead of one large autoconfigure JAR.

## Summary

- Auto-configuration = candidate classes + conditions + back-off.
- Your beans are processed first and win; customisers tweak the defaults.
- Debug with the conditions report; write starters with conditions, properties and `ApplicationContextRunner` tests.

## References

- Spring Boot reference, [Auto-configuration](https://docs.spring.io/spring-boot/reference/using/auto-configuration.html) and [Creating your own auto-configuration](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html).
- Spring Boot wiki, [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide) and [4.1 release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.1-Release-Notes).
