---
title: "Spring MVC Architecture: DispatcherServlet, Handler Mappings and Controllers"
linkTitle: "Spring MVC architecture"
description: "How a request flows through Spring MVC: the front-controller DispatcherServlet, HandlerMapping and HandlerAdapter, argument resolvers and validation, HttpMessageConverters (Jackson 3), return value handling, interceptors vs servlet filters, exception resolvers, the threading model with virtual threads, and the components you can customise."
weight: 1
difficulty: intermediate
status: complete
javaVersion: 21
tags: [spring, spring-mvc, web]
keywords: [spring mvc architecture, dispatcherservlet flow, handlermapping handleradapter, handlermethodargumentresolver, httpmessageconverter jackson, handlerinterceptor vs filter, handlerexceptionresolver, requestmappinghandlermapping, spring mvc request lifecycle, front controller pattern]
lastReviewed: 2026-11-01
prerequisites:
  - /spring/core/applicationcontext-and-beanfactory
related:
  - /spring/web/rest-api-design-and-error-handling
  - /spring/security/security-filter-chain
---

## Simple definition

**Spring MVC** is Spring's servlet-based web framework. One servlet, the **`DispatcherServlet`** (a *front controller*), receives every request and delegates to pluggable components. They find the right controller method, convert input, call it and write the response.

## The problem

Without a framework, each servlet parses parameters, converts JSON, validates input, handles errors and chooses status codes, all by hand and inconsistently. Spring MVC centralises that pipeline and makes every step replaceable.

## The request flow

{{< interactive-diagram file="dispatcher-servlet.html" title="A request through Spring MVC" source="diagrams/archify/dispatcher-servlet.sequence.json" tool="Archify" height="720" >}}

1. **Servlet filters** run first: Spring Security, CORS, encoding, tracing. They wrap the whole servlet.
2. **`DispatcherServlet`** asks each **`HandlerMapping`** which handler serves this request. `RequestMappingHandlerMapping` matches `@RequestMapping`, `@GetMapping` and so on by path, method, headers, consumes/produces and (Spring 7) **API version**.
3. **`HandlerInterceptor.preHandle`** runs. Interceptors can stop the request.
4. A **`HandlerAdapter`** (an instance of the [Adapter pattern](/design-patterns/structural/adapter)) invokes the handler:
   - **`HandlerMethodArgumentResolver`s** build arguments (`@PathVariable`, `@RequestParam`, `@RequestHeader`, `@RequestBody` through message converters, `Principal`, `Pageable`)
   - **validation** runs (`@Valid`, and method validation built into Spring 6.1+)
   - **your controller method** runs.
5. **`HandlerMethodReturnValueHandler`s** process the result. For `@ResponseBody` and `@RestController`, an **`HttpMessageConverter`** (Jackson) serialises it according to `Accept`.
6. `postHandle` and `afterCompletion` run on the interceptors.
7. Exceptions go to **`HandlerExceptionResolver`s**: `@ExceptionHandler` and `@ControllerAdvice`, then `ResponseStatusException`, then defaults. See [REST error handling](/spring/web/rest-api-design-and-error-handling).

## A controller

```java
@RestController
@RequestMapping("/api/orders")
class OrderController {
    private final OrderService orders;
    OrderController(OrderService orders) { this.orders = orders; }

    @GetMapping("/{id}")
    OrderDto get(@PathVariable long id) { return orders.find(id); }

    @PostMapping
    ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrder cmd, UriComponentsBuilder uri) {
        OrderDto created = orders.create(cmd);
        return ResponseEntity.created(uri.path("/api/orders/{id}").build(created.id())).body(created);
    }

    @GetMapping
    Page<OrderDto> list(@RequestParam(defaultValue = "PAID") Status status, Pageable pageable) {
        return orders.list(status, pageable);
    }
}
```

## Filters vs interceptors vs AOP

| | Servlet `Filter` | `HandlerInterceptor` | AOP advice |
|---|---|---|---|
| Level | Servlet container, before the DispatcherServlet | Inside Spring MVC, around handler execution | Around bean method calls |
| Knows the handler? | No | Yes (`HandlerMethod`) | Method and arguments |
| Can modify the raw request/response | Yes (wrap streams) | Limited | No |
| Typical use | Security, CORS, compression, request logging, tracing | Locale, per-handler checks, timing | Transactions, caching, method security |

## Message converters (Jackson 3)

- Converters pick a format from `Content-Type` and `Accept`: JSON (Jackson), XML, String, byte arrays, multipart, Protobuf and so on.
- **Spring Boot 4 / Spring Framework 7 move to Jackson 3** (`tools.jackson` packages). Jackson 2 is still supported for migration. Customise through `JsonMapperBuilderCustomizer` or `spring.jackson.*` properties (Boot 4.1 adds `spring.jackson.read.*` and `spring.jackson.write.*`).
- Return **DTOs**, not entities, to control the API shape and avoid lazy-loading surprises.

## Threading model

- Classic servlet containers (Tomcat, Jetty) use **one thread per request**. Long blocking calls tie up threads.
- With **virtual threads** (`spring.threads.virtual.enabled=true`), each request runs on a virtual thread, so blocking I/O scales well. See [virtual threads](/java/advanced/virtual-threads-and-structured-concurrency).
- Async options still exist (`DeferredResult`, `Callable`, `SseEmitter`, `ResponseBodyEmitter`), mainly for streaming and long polling.

## Customising via WebMvcConfigurer

```java
@Configuration
class WebConfig implements WebMvcConfigurer {
    @Override public void addInterceptors(InterceptorRegistry r) { r.addInterceptor(new TenantInterceptor()).addPathPatterns("/api/**"); }
    @Override public void addArgumentResolvers(List<HandlerMethodArgumentResolver> rs) { rs.add(new CurrentUserResolver()); }
    @Override public void configureApiVersioning(ApiVersionConfigurer c) { c.useRequestHeader("API-Version"); }  // Spring 7
}
```

Do **not** add `@EnableWebMvc` in Spring Boot apps: it switches off Boot's MVC auto-configuration.

## Functional endpoints

As an alternative to annotations, `RouterFunction` and `HandlerFunction` (WebMvc.fn) define routes in code. That is useful for small services or dynamic routing.

## Common mistakes

> [!MISTAKE] Returning JPA entities
> Leaks internal structure, triggers lazy-loading exceptions or N+1 queries during serialisation, and risks infinite recursion. Map to DTOs.

> [!MISTAKE] `@EnableWebMvc` in Spring Boot
> It disables auto-configured converters, static resources and more. Use `WebMvcConfigurer` instead.

> [!MISTAKE] Business logic in controllers
> Controllers should translate HTTP into application calls. Put logic in services or use cases.

## Quick check

**What is the DispatcherServlet's role?**
It is the front controller: it receives all requests and delegates to handler mappings, adapters, resolvers and converters.

**HandlerMapping vs HandlerAdapter?**
The mapping finds which handler serves the request. The adapter knows how to invoke that kind of handler (resolving arguments and handling return values).

**Filter vs interceptor?**
Filters are servlet-level and run before Spring MVC (security, CORS). Interceptors run inside MVC around a known handler method.

**What converts a returned object to JSON?**
An `HttpMessageConverter`, the Jackson converter by default, chosen by content negotiation.

**Why avoid `@EnableWebMvc` in Spring Boot?**
It turns off Spring Boot's MVC auto-configuration.

## Summary

- DispatcherServlet → HandlerMapping → interceptors → HandlerAdapter (arguments, validation, controller) → converters → exception resolvers.
- Filters for servlet-level concerns, interceptors for handler-level ones, AOP for bean methods.
- Return DTOs, use Jackson 3 in Boot 4, and consider virtual threads for blocking I/O.

## References

- Spring Framework reference, [Spring Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html) and [DispatcherServlet](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet.html).
- Spring Boot reference, [Servlet web applications](https://docs.spring.io/spring-boot/reference/web/servlet.html).
- Spring blog, [Jackson 3 support in Spring](https://spring.io/blog/2025/10/07/introducing-jackson-3-support-in-spring).
