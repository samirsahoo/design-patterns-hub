package hub.patterns.adapter;

/**
 * Adaptee: a third-party SDK we cannot change. Its conventions differ from
 * ours: amounts in minor units (cents), numeric status codes, a checked
 * exception, and a transaction reference that must be fetched separately.
 */
public class LegacyPayClient {

    public static final int OK = 0;
    public static final int INSUFFICIENT_FUNDS = 51;
    public static final int CARD_EXPIRED = 54;

    public static class LegacyPayException extends Exception {
        public LegacyPayException(String message) {
            super(message);
        }
    }

    private int counter = 1000;
    private String lastReference;

    public int makePayment(String cardToken, long amountInCents, String currencyIso) throws LegacyPayException {
        if (cardToken == null || cardToken.isEmpty()) {
            throw new LegacyPayException("E-001 missing token");
        }
        if (cardToken.startsWith("expired")) {
            return CARD_EXPIRED;
        }
        if (amountInCents > 50_000) {          // this simulated account has 500.00 available
            return INSUFFICIENT_FUNDS;
        }
        lastReference = "LP-" + (++counter);
        return OK;
    }

    public String getLastReference() {
        return lastReference;
    }
}
