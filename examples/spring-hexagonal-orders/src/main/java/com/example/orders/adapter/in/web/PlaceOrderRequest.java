package com.example.orders.adapter.in.web;

import com.example.orders.application.port.in.PlaceOrderUseCase.PlaceOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.List;

/**
 * HTTP request body. Validation here is about the *shape* of the input
 * (required fields, formats). Business rules stay in the domain.
 */
public record PlaceOrderRequest(
        @NotBlank String customerId,
        @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "must be an ISO 4217 code") String currency,
        @NotEmpty List<@Valid Line> lines) {

    public record Line(
            @NotBlank String productId,
            @Min(1) int quantity,
            @NotNull @DecimalMin("0.00") BigDecimal unitPrice) {
    }

    PlaceOrderCommand toCommand() {
        return new PlaceOrderCommand(customerId, currency, lines.stream()
                .map(l -> new PlaceOrderCommand.Line(l.productId(), l.quantity(), l.unitPrice()))
                .toList());
    }
}
