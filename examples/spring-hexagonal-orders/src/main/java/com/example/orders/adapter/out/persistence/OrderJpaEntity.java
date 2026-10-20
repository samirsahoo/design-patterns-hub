package com.example.orders.adapter.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Persistence model, shaped for the database and owned by this adapter.
 * Keeping it separate from the domain Order means JPA's requirements
 * (no-arg constructor, mutable fields, annotations) never leak into the core.
 */
@Entity
@Table(name = "orders")
class OrderJpaEntity {

    @Id
    UUID id;

    @Column(nullable = false)
    String customerId;

    @Column(nullable = false, length = 3)
    String currency;

    @Column(nullable = false, length = 32)
    String status;

    String paymentReference;

    @Column(nullable = false)
    Instant placedAt;

    @Version
    Long version;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_lines", joinColumns = @JoinColumn(name = "order_id"))
    @OrderColumn(name = "line_no")
    List<LineEmbeddable> lines = new ArrayList<>();

    protected OrderJpaEntity() {
        // required by JPA
    }

    @Embeddable
    static class LineEmbeddable {
        @Column(nullable = false)
        String productId;

        int quantity;

        @Column(nullable = false, precision = 19, scale = 4)
        BigDecimal unitPrice;

        protected LineEmbeddable() {
        }

        LineEmbeddable(String productId, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
}
