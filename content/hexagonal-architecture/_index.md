---
title: "Hexagonal Architecture"
description: "Ports and Adapters: keeping business rules independent of web, database and external services."
weight: 50
planned:
  - "The problem Hexagonal Architecture addresses"
  - "The application core: domain and use cases"
  - "Inbound ports and inbound adapters"
  - "Outbound ports and outbound adapters"
  - "Dependency direction"
  - "Domain logic versus infrastructure logic"
  - "Unit testing the core"
  - "Integration testing adapters"
  - "Common implementation mistakes"
  - "Hexagonal versus Clean Architecture"
  - "Hexagonal versus Onion Architecture"
---

Hexagonal Architecture, also called **Ports and Adapters**, was described by Alistair Cockburn (2005). The application core defines *ports* — interfaces stated in the application's own terms. Technology-specific *adapters* plug into those ports. Business rules can then be developed and tested without a web server, a database or a network.

The two published articles form a complete first pass:

1. [Layered versus Hexagonal Architecture](/hexagonal-architecture/layered-vs-hexagonal) covers the problem, the vocabulary, dependency direction, and when a simpler layered design is enough.
2. [Hexagonal Architecture with Spring Boot](/hexagonal-architecture/spring-boot-implementation) walks through a runnable order-management service. Its use cases are tested without Spring or a database.

The planned articles go deeper on each concept.
