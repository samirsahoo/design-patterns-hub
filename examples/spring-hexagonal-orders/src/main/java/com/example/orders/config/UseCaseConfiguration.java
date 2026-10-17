package com.example.orders.config;

import com.example.orders.application.port.out.OrderRepository;
import com.example.orders.application.port.out.PaymentPort;
import com.example.orders.application.service.OrderApplicationService;
import com.example.orders.domain.OrderId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Composition root for the application core. The core has no Spring
 * annotations; this class is the only place that turns it into beans.
 */
@Configuration(proxyBeanMethods = false)
class UseCaseConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    OrderApplicationService orderApplicationService(OrderRepository orders, PaymentPort payments, Clock clock) {
        return new OrderApplicationService(orders, payments, clock, OrderId::newId);
    }
}
