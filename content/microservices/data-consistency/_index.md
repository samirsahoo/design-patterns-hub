---
title: "Data and Consistency"
description: "Distributed transactions, sagas, the outbox, deduplication, CQRS, event sourcing and data ownership."
weight: 40
planned:
  - "Why distributed transactions are avoided"
  - "Saga pattern: choreography versus orchestration"
  - "Transactional Outbox"
  - "Inbox and deduplication"
  - "Eventual consistency"
  - "CQRS"
  - "Event Sourcing"
  - "Data ownership"
  - "Distributed locking trade-offs"
---

The [order management case study](/system-design/case-studies/order-management-system#data-flow) shows a saga with a transactional outbox end to end.
