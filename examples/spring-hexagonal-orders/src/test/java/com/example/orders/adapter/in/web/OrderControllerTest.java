package com.example.orders.adapter.in.web;

import com.example.orders.application.port.in.GetOrderQuery;
import com.example.orders.application.port.in.PlaceOrderUseCase;
import com.example.orders.domain.CustomerId;
import com.example.orders.domain.InvalidOrderException;
import com.example.orders.domain.Money;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderId;
import com.example.orders.domain.OrderLine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Adapter test: only the web layer is started. The inbound ports are mocked,
 * so this verifies HTTP mapping, validation and error translation — nothing else.
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PlaceOrderUseCase placeOrder;

    @MockitoBean
    GetOrderQuery getOrder;

    private static final String VALID_BODY = """
            {"customerId":"c-42","currency":"EUR",
             "lines":[{"productId":"book","quantity":2,"unitPrice":12.50}]}
            """;

    @Test
    void placesAnOrderAndReturns201WithLocation() throws Exception {
        Order order = Order.place(OrderId.of("00000000-0000-0000-0000-000000000007"), new CustomerId("c-42"),
                List.of(new OrderLine("book", 2, Money.of("12.50", "EUR"))), Instant.parse("2026-03-01T12:00:00Z"));
        given(placeOrder.placeOrder(any())).willReturn(order);

        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/orders/00000000-0000-0000-0000-000000000007"))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.total").value(25.00));
    }

    @Test
    void rejectsMalformedInputWith400() throws Exception {
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":"","currency":"euro","lines":[]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void translatesBusinessRuleViolationsTo422() throws Exception {
        given(placeOrder.placeOrder(any())).willThrow(new InvalidOrderException("All lines must use the same currency"));

        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("All lines must use the same currency"));
    }

    @Test
    void unknownOrderIs404() throws Exception {
        OrderId id = OrderId.of("00000000-0000-0000-0000-000000000009");
        given(getOrder.getOrder(id)).willThrow(new GetOrderQuery.OrderNotFoundException(id));

        mvc.perform(get("/orders/{id}", id.toString()))
                .andExpect(status().isNotFound());
    }
}
