---
title: "Integration Testing with @SpringBootTest, Test Slices and Testcontainers"
linkTitle: "Integration testing"
description: "Testing Spring applications with real infrastructure: @SpringBootTest and web environments, test slices (@WebMvcTest, @DataJpaTest, @JsonTest, @RestClientTest), MockMvc and RestTestClient/WebTestClient, @MockitoBean, Testcontainers with @ServiceConnection, reusable containers and dev-time services, context caching, contract tests, and a practical test strategy."
weight: 2
difficulty: intermediate
status: complete
javaVersion: 21
tags: [testing, spring-boot, testcontainers]
keywords: [springboottest, spring boot test slices, webmvctest, datajpatest, mockmvc, webtestclient, mockitobean replaces mockbean, testcontainers spring boot, serviceconnection, testcontainers postgres kafka, spring test context caching, spring cloud contract, wiremock]
lastReviewed: 2026-11-01
prerequisites:
  - /spring/production/unit-testing-junit-mockito
related:
  - /spring/data/auditing-and-migrations
  - /spring/security/method-level-security
---

## Simple definition

An **integration test** checks that components **work together with real infrastructure and framework behaviour**: HTTP mapping and JSON, transactions and SQL, security rules, messaging. Spring Boot provides **`@SpringBootTest`** for the whole application and **test slices** for one layer. **Testcontainers** starts **real** databases and brokers in Docker for tests.

## The problem

Unit tests with mocks cannot catch wrong SQL, a missing index, a broken JSON mapping, a security rule that is too open, or a migration that fails on PostgreSQL. H2 "compatibility mode" behaves differently from production databases. You need tests against the **real thing**, fast enough to run on every build.

## Choosing the right test

| Test | Loads | Speed | Use for |
|---|---|---|---|
| Unit test | Nothing | ⚡ ms | Logic |
| **`@WebMvcTest(OrderController.class)`** | MVC layer only (controllers, advice, converters, security filters) | Fast | Request mapping, validation, status codes, JSON, security rules |
| **`@DataJpaTest`** | JPA, repositories, Flyway/Liquibase (transactional, rolled back) | Fast–medium | Queries, mappings, migrations |
| `@JsonTest` | Jackson | Fast | Serialisation contracts |
| `@RestClientTest` | `RestClient` / `RestTemplate` with a mock server | Fast | HTTP client code |
| `@DataRedisTest`, `@DataMongoTest`, `@JdbcTest`… | That store | Fast–medium | Store-specific code |
| **`@SpringBootTest`** | The whole context | Slower | End-to-end inside the service; wiring; configuration |

## Slice examples

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired MockMvcTester mvc;                 // AssertJ-friendly MockMvc (Spring 6.2+)
    @MockitoBean OrderService orders;             // replaces the deprecated @MockBean

    @Test
    void returns404AsProblemDetail() {
        given(orders.find(42)).willThrow(new OrderNotFoundException(42));
        assertThat(mvc.get().uri("/api/orders/42").with(jwt()))
            .hasStatus(HttpStatus.NOT_FOUND)
            .bodyJson().extractingPath("$.title").isEqualTo("Not Found");
    }
}

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)   // use the Testcontainers DB, not an embedded one
@Import(TestcontainersConfig.class)
class OrderRepositoryTest {
    @Autowired OrderRepository repo;
    @Test void findsPaidOrdersWithLines() { ... }
}
```

## Testcontainers with @ServiceConnection

```java
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfig {
    @Bean @ServiceConnection
    PostgreSQLContainer postgres() { return new PostgreSQLContainer("postgres:17-alpine"); }

    @Bean @ServiceConnection
    KafkaContainer kafka() { return new KafkaContainer("apache/kafka-native:4.1.0"); }
}

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
class CheckoutFlowIT {
    @Autowired RestTestClient client;            // Spring 7; or WebTestClient / TestRestTemplate

    @Test
    void placeOrderEndToEnd() {
        client.post().uri("/api/orders").body(newOrder()).exchange()
              .expectStatus().isCreated();
        // assert DB rows, published Kafka events (Awaitility for async), etc.
    }
}
```

```mermaid {title="Tests talk to real infrastructure in throwaway containers"}
flowchart LR
    T["@SpringBootTest"] --> APP["Application context"]
    APP --> PG[("PostgreSQL container")]
    APP --> K["Kafka container"]
    APP --> WM["WireMock<br/>(external HTTP APIs)"]
```

- **`@ServiceConnection`** (Boot 3.1+) auto-configures connection properties (URL, user, password, bootstrap servers) from the container. No `@DynamicPropertySource` boilerplate.
- **Testcontainers 2.x** (used with Spring Boot 4) modernised the module and package names and dropped JUnit 4 support. Check the migration notes.
- **Dev-time services:** run `TestApplication.main` with the same container config (`SpringApplication.from(App::main).with(TestcontainersConfig.class).run()`) to develop locally against real containers. Alternatively, Docker Compose support (`spring-boot-docker-compose`) starts `compose.yaml` services automatically.
- Use **WireMock** (or MockServer) for third-party HTTP APIs, and Awaitility for asynchronous assertions.

## Keep the suite fast: context caching

Spring caches application contexts **between test classes with identical configuration**. Every different `@MockitoBean` set, property or profile creates a **new context**, which means a slow startup.

- Standardise a few base configurations (an abstract `IntegrationTest` class).
- Prefer slices; avoid `@DirtiesContext`.
- Reuse containers across classes (static containers or singleton beans). Testcontainers' reuse mode can speed up local runs.
- Run integration tests in parallel where they are isolated.

## Contract tests between services

- **Consumer-driven contracts** (Spring Cloud Contract, Pact) verify that a provider still satisfies what its consumers expect, without full end-to-end environments.
- For events, test schemas against a registry (compatibility checks) and use Testcontainers Kafka for producer and consumer tests.

## A practical strategy

| Layer | Tests |
|---|---|
| Domain and services | Unit tests (most of them) |
| Controllers | `@WebMvcTest` per controller: mapping, validation, errors, security |
| Repositories and migrations | `@DataJpaTest` + Testcontainers (the real database engine) |
| Messaging | `@SpringBootTest` + Testcontainers Kafka/RabbitMQ for key flows |
| Whole service | A few `@SpringBootTest(RANDOM_PORT)` happy-path and failure flows |
| Between services | Contract tests |

## Common mistakes

> [!MISTAKE] H2 instead of the real database
> SQL dialects, locking, JSON types and migrations differ. Use Testcontainers with the production engine and version.

> [!MISTAKE] A new context in every test class
> Random `@MockitoBean` combinations defeat context caching, and the suite takes many minutes. Standardise the configurations.

> [!MISTAKE] `@SpringBootTest` for controller rules
> Slower and harder to isolate. `@WebMvcTest` covers mapping, validation and security quickly.

## Quick check

**`@WebMvcTest` vs `@SpringBootTest`?**
`@WebMvcTest` loads only the web layer for one or more controllers. `@SpringBootTest` loads the full application context.

**What does `@ServiceConnection` do?**
It derives Spring Boot connection properties automatically from a Testcontainers container bean.

**Why not test with H2?**
It differs from production databases in SQL, types, locking and migration behaviour, so tests can pass while production fails.

**What replaced `@MockBean`?**
`@MockitoBean` (and `@MockitoSpyBean`) from Spring Framework 6.2+.

**How do you keep integration tests fast?**
Use slices, reuse cached contexts by standardising configuration, share containers, and avoid `@DirtiesContext`.

## Summary

- Use slices for layers and `@SpringBootTest` for full flows.
- Test against real infrastructure with Testcontainers + `@ServiceConnection`; WireMock for external APIs.
- Protect context caching; add contract tests between services.

## References

- Spring Boot reference, [Testing](https://docs.spring.io/spring-boot/reference/testing/index.html) and [Testcontainers support](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html).
- [Testcontainers for Java](https://java.testcontainers.org/).
- Spring Framework reference, [MockMvc](https://docs.spring.io/spring-framework/reference/testing/mockmvc.html).
- [Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract) and [WireMock](https://wiremock.org/).
