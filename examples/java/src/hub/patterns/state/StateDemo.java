package hub.patterns.state;

// region state
/** State: one method per event. Each concrete state decides what the event means in that state. */
interface OrderState {
    default OrderState pay(Order order) { throw invalid("pay"); }
    default OrderState ship(Order order) { throw invalid("ship"); }
    default OrderState deliver(Order order) { throw invalid("deliver"); }
    default OrderState cancel(Order order) { throw invalid("cancel"); }

    String name();

    private IllegalStateException invalid(String event) {
        return new IllegalStateException("cannot " + event + " an order that is " + name());
    }
}
// endregion

// region concrete
final class AwaitingPayment implements OrderState {
    @Override public OrderState pay(Order o) { o.log("payment captured"); return new Paid(); }
    @Override public OrderState cancel(Order o) { o.log("cancelled before payment"); return new Cancelled(); }
    @Override public String name() { return "AWAITING_PAYMENT"; }
}

final class Paid implements OrderState {
    @Override public OrderState ship(Order o) { o.log("handed to carrier"); return new Shipped(); }
    @Override public OrderState cancel(Order o) { o.log("cancelled, refund issued"); return new Cancelled(); }
    @Override public String name() { return "PAID"; }
}

final class Shipped implements OrderState {
    @Override public OrderState deliver(Order o) { o.log("delivered"); return new Delivered(); }
    @Override public String name() { return "SHIPPED"; }      // cancel is not allowed: inherited default throws
}

final class Delivered implements OrderState {
    @Override public String name() { return "DELIVERED"; }
}

final class Cancelled implements OrderState {
    @Override public String name() { return "CANCELLED"; }
}
// endregion

// region context
/** Context: delegates every event to its current state object and stores the returned next state. */
final class Order {
    private final String id;
    private OrderState state = new AwaitingPayment();

    Order(String id) { this.id = id; }

    void pay() { state = state.pay(this); }
    void ship() { state = state.ship(this); }
    void deliver() { state = state.deliver(this); }
    void cancel() { state = state.cancel(this); }

    String status() { return state.name(); }
    void log(String message) { System.out.println("  " + id + ": " + message); }
}
// endregion

public final class StateDemo {

    public static void main(String[] args) {
        Order happy = new Order("ORD-1");
        happy.pay();
        happy.ship();
        happy.deliver();
        System.out.println("ORD-1 is " + happy.status());

        Order late = new Order("ORD-2");
        late.pay();
        late.ship();
        try {
            late.cancel();
        } catch (IllegalStateException e) {
            System.out.println("  ORD-2: rejected - " + e.getMessage());
        }
        System.out.println("ORD-2 is " + late.status());

        Order early = new Order("ORD-3");
        early.cancel();
        System.out.println("ORD-3 is " + early.status());
    }
}
