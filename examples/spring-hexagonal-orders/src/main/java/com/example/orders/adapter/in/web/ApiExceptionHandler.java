package com.example.orders.adapter.in.web;

import com.example.orders.application.port.in.GetOrderQuery.OrderNotFoundException;
import com.example.orders.domain.InvalidOrderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps application and domain exceptions to RFC 9457 Problem Details.
 * The mapping is HTTP knowledge, so it belongs in the web adapter.
 */
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail notFound(OrderNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InvalidOrderException.class)
    ProblemDetail businessRuleViolated(InvalidOrderException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badInput(IllegalArgumentException e) {
        // e.g. an unknown currency code or a malformed order id
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
