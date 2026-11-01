---
title: "System Design Interview Preparation"
description: "A repeatable framework, estimation practice, building blocks and worked case studies."
weight: 6
audience: "Candidates preparing for system design interviews"
steps:
  - title: "Requirements and the 18-step framework"
    page: /system-design/introduction
    why: "A structure that keeps you from jumping straight to technologies."
  - title: "Capacity estimation"
    page: /system-design/introduction
    why: "The worked example shows how estimates drive decisions such as caching and sharding."
  - title: "API design"
    page: /system-design/case-studies/order-management-system
    why: "The case study's API section covers idempotency keys, asynchronous `202` responses and cursor pagination."
  - title: "Data modelling and storage selection"
    page: /system-design/building-blocks/choosing-a-database
    also: [/data-systems/data-models-and-query-languages, /data-systems/storage-engines]
    why: "Access patterns first, then the store that fits them."
  - title: "High-level design with reusable building blocks"
    page: /system-design/diagram-patterns
    why: "Load balancers, caches, brokers, replication and sharding, each with its failure modes."
  - title: "Scaling"
    page: /system-design/building-blocks/scaling-from-zero-to-millions
    also: [/system-design/building-blocks/load-balancing, /system-design/building-blocks/caching-strategies, /system-design/building-blocks/consistent-hashing, /data-systems/replication, /data-systems/partitioning]
    why: "Replication, partitioning, caching and asynchronous processing, chosen from the numbers."
  - title: "Consistency and distributed-systems theory"
    page: /system-design/building-blocks/cap-theorem-and-pacelc
    also: [/data-systems/transactions-and-isolation, /data-systems/consistency-and-consensus, /data-systems/distributed-systems-faults]
    why: "Interviewers probe what guarantees your design gives and what it costs."
  - title: "Communication: queues, delivery semantics and real-time push"
    page: /system-design/building-blocks/message-queues-and-delivery-semantics
    also: [/system-design/building-blocks/real-time-communication, /data-systems/stream-processing]
    why: "At-least-once delivery plus idempotency is the answer to many follow-up questions."
  - title: "Reliability"
    page: /system-design/case-studies/order-management-system
    why: "The failure-handling table shows how to reason about every dependency being slow or down."
  - title: "Trade-offs and follow-up questions"
    page: /microservices/fundamentals/microservices-vs-modular-monolith
    why: "Defending a design matters as much as drawing it; this article models trade-off reasoning."
  - title: "More case studies: URL shortener, rate limiter, payment system, proximity service"
    page: /system-design/case-studies/url-shortener
    also: [/system-design/case-studies/rate-limiter, /system-design/case-studies/payment-system, /system-design/case-studies/proximity-service, /system-design/building-blocks/unique-id-generation]
    why: "Practise the framework on systems with different bottlenecks."
  - title: "Real-time and social systems: chat, news feed, notifications, collaborative editing"
    page: /system-design/case-studies/chat-system
    also: [/system-design/case-studies/news-feed, /system-design/case-studies/notification-service, /system-design/case-studies/collaborative-editing]
    why: "Persistent connections, fan-out and convergence of concurrent edits."
  - title: "Data-heavy systems: video, crawler, autocomplete, file sync, metrics"
    page: /system-design/case-studies/video-streaming
    also: [/system-design/case-studies/web-crawler, /system-design/case-studies/search-autocomplete, /system-design/case-studies/file-storage-sync, /system-design/case-studies/metrics-monitoring, /system-design/building-blocks/probabilistic-data-structures]
    why: "Pipelines, object storage, tries, Bloom filters and time-series data."
  - title: "Infrastructure and extreme-scale systems: key-value store, stock exchange, ride sharing, flash sale"
    page: /system-design/case-studies/distributed-key-value-store
    also: [/system-design/case-studies/stock-exchange, /system-design/case-studies/ride-sharing, /system-design/case-studies/flash-sale]
    why: "Quorums, determinism, moving geospatial data and traffic spikes."
---

Practise each case study out loud with a timer, and spend the first third of the time on requirements and estimates.
