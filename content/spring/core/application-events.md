---
title: "Spring Application Events and Listeners"
linkTitle: "Application events"
description: "Decoupling with Spring's event mechanism: publishing events, @EventListener, ordering and conditions, synchronous vs @Async listeners, @TransactionalEventListener phases, built-in lifecycle events, error handling, Spring Modulith's event publication registry, and when to use a message broker instead."
weight: 8
difficulty: intermediate
status: complete
javaVersion: 21
tags: [spring, events, observer, decoupling]
keywords: [spring application events, eventlistener annotation, applicationeventpublisher, transactionaleventlistener after commit, async event listener, spring lifecycle events applicationreadyevent, spring modulith events, observer pattern spring, event propagation spring]
lastReviewed: 2026-11-01
prerequisites:
  - /spring/core/bean-lifecycle-and-scopes
related:
  - /spring/data/transactions
  - /microservices/communication/event-driven-spring-cloud-stream
---

## Simple definition

Spring's **application events** let one bean **publish** a message ("an order was placed") and any number of other beans **listen** and react, without the publisher knowing them. It is the Observer pattern, implemented by the `ApplicationContext`, **inside one application**.

## The problem

`OrderService` should not have to call `EmailService`, `LoyaltyService`, `AnalyticsService` and `InventoryService` directly. Every new reaction would mean changing and re-testing the order code, and the service would accumulate unrelated dependencies.

## Publishing and listening

```java
public record OrderPlaced(String orderId, String customerId, BigDecimal total) {}   // any object can be an event

@Service
class OrderService {
    private final ApplicationEventPublisher events;
    OrderService(ApplicationEventPublisher events) { this.events = events; }

    @Transactional
    public Order place(PlaceOrder cmd) {
        Order order = repository.save(Order.from(cmd));
        events.publishEvent(new OrderPlaced(order.id(), order.customerId(), order.total()));
        return order;
    }
}

@Component
class LoyaltyListener {
    @EventListener
    @Order(10)
    void on(OrderPlaced event) { loyalty.addPoints(event.customerId(), event.total()); }

    @EventListener(condition = "#event.total > 1000")      // SpEL condition
    void onLarge(OrderPlaced event) { alerts.notifyVipTeam(event); }
}
```

A listener can also **return** a new event (or a collection of them), which Spring publishes in turn.

## Synchronous by default

```mermaid {title="Default: listeners run in the publisher's thread and transaction"}
sequenceDiagram
    participant S as OrderService (@Transactional)
    participant P as ApplicationEventMulticaster
    participant L1 as LoyaltyListener
    participant L2 as EmailListener
    S->>P: publishEvent(OrderPlaced)
    P->>L1: on(event) (same thread, same tx)
    P->>L2: on(event)
    L2--xP: exception
    P--xS: exception propagates and the transaction rolls back
```

- Listeners run **one after another** in the publisher's thread, **inside its transaction**.
- A listener exception **propagates to the publisher**.
- That is simple and consistent, but slow listeners slow the request, and failures in side effects (email) roll back business data.

## @TransactionalEventListener

Often you want to react **only if the transaction commits**:

```java
@Component
class EmailListener {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)   // default phase
    @Async
    void sendConfirmation(OrderPlaced event) { mail.sendOrderConfirmation(event.orderId()); }
}
```

| Phase | Runs | Use |
|---|---|---|
| `BEFORE_COMMIT` | Just before commit, inside the transaction | Last-moment checks or writes |
| `AFTER_COMMIT` (default) | After a successful commit | Emails, cache eviction, notifications |
| `AFTER_ROLLBACK` | After a rollback | Compensation and alerts |
| `AFTER_COMPLETION` | After either | Cleanup |

> [!WARNING] AFTER_COMMIT listeners can be lost
> If the application crashes after the commit but before the listener runs, the event is gone; it was only in memory. If the reaction **must** happen, use Spring Modulith's event publication registry, or an outbox with a broker.

## Asynchronous listeners

- Add `@Async` (and `@EnableAsync`) to run a listener on a task executor. With `spring.threads.virtual.enabled=true`, that executor uses **virtual threads**.
- Async listeners do not share the publisher's transaction or thread-locals (security context, MDC), so propagate context explicitly if needed.
- Exceptions go to an `AsyncUncaughtExceptionHandler`. Log and alert.

## Built-in lifecycle events

| Event | When |
|---|---|
| `ApplicationStartingEvent` … `ApplicationPreparedEvent` | Early Boot startup stages (register these via `spring.factories` or `SpringApplication`) |
| `ContextRefreshedEvent` | The context is initialised or refreshed |
| `ApplicationStartedEvent` | Context refreshed, before the runners |
| `ApplicationReadyEvent` | **Ready to serve**: the good place for warm-up tasks |
| `AvailabilityChangeEvent` | Liveness and readiness changes (Kubernetes probes) |
| `ContextClosedEvent` | Shutdown begins |

## Spring Modulith: reliable in-process events

**Spring Modulith** adds an **event publication registry**. Events for `@ApplicationModuleListener`s are stored in the database **in the same transaction** as the business change, then delivered after commit and marked complete. Incomplete publications are **retried** on restart. It can also **externalise** events to Kafka, RabbitMQ or SQS. This is the bridge from in-process events to a [message broker](/microservices/communication/event-driven-spring-cloud-stream).

```java
@ApplicationModuleListener          // = @Async + @Transactional(REQUIRES_NEW) + @TransactionalEventListener
void on(OrderPlaced event) { inventory.reserve(event.orderId()); }
```

## Application events vs a message broker

| | Spring application events | Broker (Kafka, RabbitMQ) |
|---|---|---|
| Scope | One JVM | Across services |
| Durability | In-memory, unless you use the Modulith registry | Durable |
| Use for | Decoupling modules **inside** an app (a modular monolith) | Integration **between** services |

## Common mistakes

> [!MISTAKE] Side effects in synchronous listeners
> Sending email in a plain `@EventListener` happens before commit. If the transaction rolls back, the customer gets an email for an order that does not exist.

> [!MISTAKE] Assuming events are durable
> In-memory events vanish on crash. Use the Modulith registry or an outbox for must-happen reactions.

> [!MISTAKE] Events as hidden call chains
> Long chains of listeners publishing more events become impossible to follow. Keep flows shallow and documented.

## Quick check

**Are Spring events synchronous by default?**
Yes. Listeners run in the publisher's thread, in its transaction, and exceptions propagate to the publisher.

**What does `@TransactionalEventListener` add?**
It ties the listener to a transaction phase, by default running it only after a successful commit.

**Why can AFTER_COMMIT reactions be lost?**
The event exists only in memory; a crash between commit and listener execution loses it.

**What does Spring Modulith's event publication registry provide?**
Events are persisted in the same transaction as the business change, then delivered and retried until completed.

**When use a broker instead of application events?**
When events must cross service or process boundaries, or need durable, replayable delivery.

## Summary

- Application events decouple beans inside one app: the Observer pattern by the container.
- They are synchronous and transactional by default; use `@TransactionalEventListener(AFTER_COMMIT)` plus `@Async` for side effects.
- For guaranteed delivery, use the Spring Modulith registry or an outbox; use brokers between services.

## References

- Spring Framework reference, [Standard and Custom Events](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events) and [Transaction-bound events](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html).
- Spring Boot reference, [Application Events and Listeners](https://docs.spring.io/spring-boot/reference/features/spring-application.html#features.spring-application.application-events-and-listeners).
- Spring Modulith reference, [Working with Application Events](https://docs.spring.io/spring-modulith/reference/events.html).
