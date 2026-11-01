---
title: "Unit Testing with JUnit and Mockito"
linkTitle: "Unit testing (JUnit, Mockito)"
description: "Fast, focused tests for Java and Spring code: the testing pyramid, JUnit Jupiter (JUnit 5 and 6) lifecycle, assertions with AssertJ, parameterized and nested tests, Mockito mocks, stubs, spies, verification and argument captors, BDD style, testing without Spring, test doubles vs fakes, and what makes a good unit test."
weight: 1
difficulty: beginner
status: complete
javaVersion: 21
tags: [testing, junit, mockito]
keywords: [junit 5 tutorial, junit 6, junit jupiter parameterized tests, assertj assertions, mockito tutorial, mockito mock vs spy, argumentcaptor, bddmockito given willreturn, mockitoextension, testing pyramid, unit test best practices, test doubles fakes stubs mocks]
lastReviewed: 2026-11-01
related:
  - /spring/production/integration-testing
  - /spring/core/dependency-injection
---

## Simple definition

A **unit test** checks one small piece of behaviour (a class or a few collaborating classes) **in isolation**, quickly, with no Spring context, database or network. **JUnit** runs the tests. **Mockito** creates **test doubles** for collaborators you do not want to involve.

## The problem

Without fast tests, every change needs a slow manual or end-to-end check, so refactoring becomes risky and bugs reach production. Tests that start Spring for every class are slow. Tests that mock everything verify the wiring instead of the behaviour.

## The testing pyramid

```mermaid {title="Many fast tests at the bottom, few slow ones at the top"}
flowchart TB
    E2E["End-to-end / UI<br/>few, slow, brittle"] --> INT["Integration<br/>(@SpringBootTest, slices, Testcontainers)"] --> UNIT["Unit<br/>many, fast, isolated"]
```

Aim for **most** tests as unit tests (milliseconds each), a meaningful layer of [integration tests](/spring/production/integration-testing) for wiring and infrastructure, and a few end-to-end checks of critical journeys.

## JUnit Jupiter essentials

```java
class PricingServiceTest {

    private final DiscountPolicy policy = new TieredDiscountPolicy();      // real collaborator (pure logic)
    private final PricingService pricing = new PricingService(policy, Clock.fixed(NOON, UTC));

    @Test
    @DisplayName("VIP customers get 10% off orders above 100")
    void vipDiscount() {
        Money price = pricing.priceFor(order(150), Customer.vip("c-1"));
        assertThat(price).isEqualTo(Money.eur("135.00"));                 // AssertJ
    }

    @ParameterizedTest(name = "{0} items → {1}")
    @CsvSource({ "1, 10.00", "10, 95.00", "100, 900.00" })
    void volumePricing(int qty, BigDecimal expected) {
        assertThat(pricing.priceFor(order(qty), Customer.regular("c-2")).amount()).isEqualByComparingTo(expected);
    }

    @Nested
    class WhenCustomerIsBlocked {
        @Test void pricingFails() {
            assertThatThrownBy(() -> pricing.priceFor(order(1), Customer.blocked("c-3")))
                .isInstanceOf(CustomerBlockedException.class)
                .hasMessageContaining("c-3");
        }
    }
}
```

| Feature | Use |
|---|---|
| `@BeforeEach` / `@AfterEach`, `@BeforeAll` / `@AfterAll` | Setup and cleanup (prefer final fields and factory methods) |
| `@ParameterizedTest` with `@ValueSource`, `@CsvSource`, `@MethodSource`, `@EnumSource` | Many cases, one test |
| `@Nested` | Group scenarios ("when…") |
| `@Disabled`, `@Tag`, `@Timeout` | Control execution |
| `@TempDir` | Temporary files |
| Extensions (`@ExtendWith`) | Mockito, Spring and Testcontainers integration |

> [!VERSION] JUnit 6
> **JUnit 6** (2025) unified the version numbers of Platform, Jupiter and Vintage. It requires **Java 17+**, adds JSpecify nullness annotations, improves Kotlin coroutine support and drops long-deprecated APIs. Spring Boot 4 uses JUnit 6. Most JUnit 5 tests run unchanged; the programming model is still called "Jupiter".

**Use AssertJ** (included in `spring-boot-starter-test`) for fluent, readable assertions with great failure messages.

## Mockito

```java
@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock PaymentGateway payments;              // a test double
    @Mock OrderRepository orders;
    @InjectMocks CheckoutService checkout;      // constructor injection of the mocks
    @Captor ArgumentCaptor<Order> saved;

    @Test
    void successfulCheckoutSavesPaidOrder() {
        given(payments.charge(any())).willReturn(Receipt.approved("A-1"));       // stub (BDD style)

        checkout.checkout(cart("ABC", 2));

        then(orders).should().save(saved.capture());                            // verify interaction
        assertThat(saved.getValue().status()).isEqualTo(Status.PAID);
        then(payments).shouldHaveNoMoreInteractions();
    }

    @Test
    void declinedPaymentDoesNotSaveOrder() {
        given(payments.charge(any())).willThrow(new PaymentDeclinedException("insufficient funds"));
        assertThatThrownBy(() -> checkout.checkout(cart("ABC", 1))).isInstanceOf(CheckoutFailedException.class);
        then(orders).shouldHaveNoInteractions();
    }
}
```

| Concept | Meaning |
|---|---|
| **Mock** | A fake implementation that records calls and returns stubbed answers (defaults: null, 0, empty) |
| **Stub** | Configured answers: `when(...).thenReturn(...)` or `given(...).willReturn(...)` |
| **Spy** | Wraps a real object; real methods unless stubbed. Use rarely. |
| **Verify** | Assert interactions: `verify(mock).method(args)`, `times(n)`, `never()` |
| **ArgumentCaptor** | Capture arguments for detailed assertions |
| Argument matchers | `any()`, `eq()`, `argThat(...)`; if you use one matcher, use matchers for all arguments |

- **Mockito 5** uses the **inline mock maker** by default, so `final` classes and methods can be mocked. On JDK 21+, add Mockito as a Java agent in your build to avoid dynamic-agent warnings.
- **Strict stubs** (the default with `MockitoExtension`) fail tests with unused stubs, which keeps tests honest.

## Test doubles: choose the lightest

| Double | When |
|---|---|
| **Real object** | Pure logic, value objects, domain classes: **prefer real** |
| **Fake** (in-memory implementation) | Repositories and gateways in [hexagonal](/hexagonal-architecture/spring-boot-implementation) designs. Behaves realistically. |
| **Stub** | You need a fixed answer |
| **Mock with verification** | The interaction **is** the behaviour (a payment was charged, an event was published) |

> [!KEY] Test behaviour, not implementation
> Assert **outcomes** (returned values, state, published events). Verify interactions only when they are the point. Over-mocked tests break on every refactor and prove little.

## What good unit tests look like (FIRST)

**F**ast, **I**ndependent (no order dependence), **R**epeatable (no real time, randomness or network: inject a `Clock`), **S**elf-validating (assertions, not logs) and **T**imely (written with the code). Structure them as **Arrange–Act–Assert** (or Given–When–Then) and name them after behaviour.

## Spring code without Spring

Constructor injection means you can `new` your services with fakes or mocks. **No `@SpringBootTest` is needed for unit tests.** Use Spring's test support only when the framework behaviour itself (mappings, transactions, serialisation) is under test.

## Common mistakes

> [!MISTAKE] Mocking value objects and domain logic
> Mocking `Money` or an entity tests nothing real. Use real instances.

> [!MISTAKE] `@SpringBootTest` for every class
> It adds seconds per test class. Use plain unit tests, and slices where the framework matters.

> [!MISTAKE] Non-deterministic tests
> `LocalDate.now()` and `Random` make tests flaky. Inject a `Clock` and seeds.

## Quick check

**Mock vs stub vs spy?**
A stub returns canned answers. A mock also records and verifies interactions. A spy wraps a real object and calls real methods unless stubbed.

**When should you verify interactions?**
When the interaction is the behaviour you care about (charging a payment, sending an event), not as a substitute for checking outcomes.

**What does `@InjectMocks` do?**
It creates the class under test and injects the `@Mock` fields, preferring constructor injection.

**How do you test time-dependent logic?**
Inject a `java.time.Clock` and use `Clock.fixed(...)` in tests.

**What is JUnit 6?**
The 2025 major release that unified JUnit's module versions, requires Java 17+ and is used by Spring Boot 4. The Jupiter API is largely unchanged.

## Summary

- Most tests should be fast unit tests: JUnit Jupiter plus AssertJ.
- Mockito for collaborators at boundaries; prefer real objects and fakes elsewhere.
- Test behaviour; keep tests FIRST; inject clocks; no Spring context for unit tests.

## References

- [JUnit User Guide](https://docs.junit.org/current/user-guide/).
- [Mockito documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html).
- [AssertJ](https://assertj.github.io/doc/).
- Martin Fowler, [Mocks Aren't Stubs](https://martinfowler.com/articles/mocksArentStubs.html) and [The Practical Test Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html).
