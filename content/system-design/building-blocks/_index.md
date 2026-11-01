---
title: "System Design Building Blocks"
description: "The reusable components and techniques of every large system: scaling steps, load balancers, caches, consistent hashing, CAP, database choice, ID generation, queues, real-time communication, deployment and security fundamentals."
weight: 20
---

Every system design answer is assembled from the same parts. Each article here explains one building block: the problem it solves, how it works, the options and their trade-offs, the numbers worth remembering, and the interview questions it raises. The [case studies](/system-design/case-studies) combine them, each with an interactive [Archify](https://github.com/tt-a1i/archify) diagram. The [Data-Intensive Systems](/data-systems) section goes deeper into the theory behind the data-related blocks.

**Scaling and traffic**

- [Scaling from Zero to Millions of Users](/system-design/building-blocks/scaling-from-zero-to-millions)
- [Load Balancing and Proxies](/system-design/building-blocks/load-balancing)
- [Caching Strategies](/system-design/building-blocks/caching-strategies)
- [Consistent Hashing](/system-design/building-blocks/consistent-hashing)

**Data**

- [CAP Theorem and PACELC](/system-design/building-blocks/cap-theorem-and-pacelc)
- [Choosing a Database: SQL, NoSQL and Storage Types](/system-design/building-blocks/choosing-a-database)
- [Unique ID Generation](/system-design/building-blocks/unique-id-generation)
- [Probabilistic Data Structures: Bloom Filters, HyperLogLog and Count-Min Sketch](/system-design/building-blocks/probabilistic-data-structures)

**Communication**

- [Message Queues and Delivery Semantics](/system-design/building-blocks/message-queues-and-delivery-semantics)
- [Long Polling, WebSockets and Server-Sent Events](/system-design/building-blocks/real-time-communication)
- [DNS and What Happens When You Type a URL](/system-design/building-blocks/dns-and-web-request-lifecycle)

**Operations and security**

- [Deployment Strategies](/system-design/building-blocks/deployment-strategies)
- [HTTPS, Password Storage and API Request Signing](/system-design/building-blocks/security-fundamentals)
