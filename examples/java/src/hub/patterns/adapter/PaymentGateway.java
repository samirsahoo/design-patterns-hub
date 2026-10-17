package hub.patterns.adapter;

import java.math.BigDecimal;

/** Target: the interface our application wants to use, in our own terms. */
public interface PaymentGateway {

    PaymentResult charge(String customerToken, BigDecimal amount, String currency);

    sealed interface PaymentResult {
        record Approved(String transactionId) implements PaymentResult { }
        record Declined(String reason) implements PaymentResult { }
    }
}
