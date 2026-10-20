package com.example.orders.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Spring Data does the SQL. This interface is an implementation detail of the adapter. */
interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {
}
