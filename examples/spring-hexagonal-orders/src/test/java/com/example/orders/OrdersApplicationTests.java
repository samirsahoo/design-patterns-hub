package com.example.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end through every adapter: HTTP -> use case -> JPA/H2 and simulated payments. */
@SpringBootTest
@AutoConfigureMockMvc
class OrdersApplicationTests {

    @Autowired
    MockMvc mvc;

    @Test
    void placeThenFetchAnOrder() throws Exception {
        String location = mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerId":"c-42","currency":"EUR",
                         "lines":[{"productId":"book","quantity":2,"unitPrice":12.50}]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(25.00))
                .andExpect(jsonPath("$.lines[0].productId").value("book"));
    }

    @Test
    void ordersAboveTheSimulatedLimitAreDeclined() throws Exception {
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerId":"c-7","currency":"EUR",
                         "lines":[{"productId":"tv","quantity":1,"unitPrice":1999.00}]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAYMENT_DECLINED"));
    }
}
