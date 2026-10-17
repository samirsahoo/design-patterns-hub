package com.example.orders.domain;

public enum OrderStatus {
    /** Created and waiting for payment. */
    PENDING_PAYMENT,
    /** Payment captured. */
    PAID,
    /** The payment provider declined the charge. */
    PAYMENT_DECLINED,
    /** Cancelled by the customer before payment. */
    CANCELLED
}
