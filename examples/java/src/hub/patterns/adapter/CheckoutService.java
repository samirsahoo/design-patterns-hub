package hub.patterns.adapter;

import java.math.BigDecimal;

/** Client: depends only on the Target interface, never on LegacyPayClient. */
public final class CheckoutService {

    private final PaymentGateway gateway;

    public CheckoutService(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    public String pay(String token, BigDecimal amount) {
        return switch (gateway.charge(token, amount, "EUR")) {
            case PaymentGateway.PaymentResult.Approved a -> "paid, transaction " + a.transactionId();
            case PaymentGateway.PaymentResult.Declined d -> "declined: " + d.reason();
        };
    }
}
