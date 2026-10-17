package hub.patterns.adapter;

import java.math.BigDecimal;

public final class AdapterDemo {

    public static void main(String[] args) {
        PaymentGateway gateway = new LegacyPayAdapter(new LegacyPayClient());
        CheckoutService checkout = new CheckoutService(gateway);

        System.out.println(checkout.pay("tok_visa", new BigDecimal("49.90")));
        System.out.println(checkout.pay("tok_visa", new BigDecimal("899.00")));
        System.out.println(checkout.pay("expired_tok", new BigDecimal("10.00")));
        try {
            checkout.pay("", new BigDecimal("10.00"));
        } catch (LegacyPayAdapter.PaymentGatewayException e) {
            System.out.println("error: " + e.getMessage() + " (cause: " + e.getCause().getMessage() + ")");
        }
    }
}
