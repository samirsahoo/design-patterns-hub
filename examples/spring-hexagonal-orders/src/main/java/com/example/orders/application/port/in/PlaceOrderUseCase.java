package com.example.orders.application.port.in;

import com.example.orders.domain.Order;

import java.math.BigDecimal;
import java.util.List;

/**
 * Inbound port: what the outside world can ask the application to do.
 * Expressed in application terms — no HTTP, no JSON, no Spring types.
 */
public interface PlaceOrderUseCase {

    Order placeOrder(PlaceOrderCommand command);

    record PlaceOrderCommand(String customerId, String currency, List<Line> lines) {

        public PlaceOrderCommand {
            lines = List.copyOf(lines);
        }

        public record Line(String productId, int quantity, BigDecimal unitPrice) {
        }
    }
}
