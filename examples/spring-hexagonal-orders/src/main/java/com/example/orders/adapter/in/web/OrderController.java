package com.example.orders.adapter.in.web;

import com.example.orders.application.port.in.GetOrderQuery;
import com.example.orders.application.port.in.PlaceOrderUseCase;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Inbound (driving) adapter: translates HTTP into calls on inbound ports and
 * domain results back into HTTP. It contains no business rules.
 */
@RestController
@RequestMapping("/orders")
class OrderController {

    private final PlaceOrderUseCase placeOrder;
    private final GetOrderQuery getOrder;

    OrderController(PlaceOrderUseCase placeOrder, GetOrderQuery getOrder) {
        this.placeOrder = placeOrder;
        this.getOrder = getOrder;
    }

    @PostMapping
    ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request) {
        Order order = placeOrder.placeOrder(request.toCommand());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(order.id()).toUri();
        return ResponseEntity.created(location).body(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable String id) {
        return OrderResponse.from(getOrder.getOrder(OrderId.of(id)));
    }
}
