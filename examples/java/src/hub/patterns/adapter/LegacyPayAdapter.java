package hub.patterns.adapter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Object adapter: implements our Target interface and translates each call
 * into the adaptee's API — units, status codes, exceptions and naming.
 */
public final class LegacyPayAdapter implements PaymentGateway {

    private final LegacyPayClient client;

    public LegacyPayAdapter(LegacyPayClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public PaymentResult charge(String customerToken, BigDecimal amount, String currency) {
        long minorUnits = toMinorUnits(amount, currency);
        try {
            int status = client.makePayment(customerToken, minorUnits, currency);
            return switch (status) {
                case LegacyPayClient.OK -> new PaymentResult.Approved(client.getLastReference());
                case LegacyPayClient.INSUFFICIENT_FUNDS -> new PaymentResult.Declined("insufficient funds");
                case LegacyPayClient.CARD_EXPIRED -> new PaymentResult.Declined("card expired");
                default -> new PaymentResult.Declined("unknown status " + status);
            };
        } catch (LegacyPayClient.LegacyPayException e) {
            // Translate the vendor's checked exception into our own vocabulary.
            throw new PaymentGatewayException("Payment provider rejected the request", e);
        }
    }

    private static long toMinorUnits(BigDecimal amount, String currency) {
        int digits = Currency.getInstance(currency).getDefaultFractionDigits();
        return amount.setScale(digits, RoundingMode.UNNECESSARY).movePointRight(digits).longValueExact();
    }

    public static final class PaymentGatewayException extends RuntimeException {
        public PaymentGatewayException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
