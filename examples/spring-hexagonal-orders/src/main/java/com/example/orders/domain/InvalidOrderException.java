package com.example.orders.domain;

/** Thrown when an operation would break a business rule of the Order aggregate. */
public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
