package hub.patterns.mediator;

// region mediator
/** Mediator: components report events here instead of talking to each other. */
interface FormMediator {
    void changed(Widget source);
}
// endregion

// region colleagues
/** Colleague base class: knows only the mediator, never the other widgets. */
abstract class Widget {
    protected final String name;
    protected FormMediator mediator;
    private boolean enabled = true;

    protected Widget(String name) { this.name = name; }

    void attach(FormMediator mediator) { this.mediator = mediator; }
    boolean enabled() { return enabled; }
    void setEnabled(boolean enabled) { this.enabled = enabled; }
}

final class Checkbox extends Widget {
    private boolean checked;

    Checkbox(String name) { super(name); }

    void click() {
        checked = !checked;
        System.out.println("user toggles '" + name + "' -> " + checked);
        mediator.changed(this);
    }
    boolean checked() { return checked; }
}

final class TextField extends Widget {
    private String text = "";

    TextField(String name) { super(name); }

    void type(String value) {
        text = value;
        System.out.println("user types '" + value + "' into " + name);
        mediator.changed(this);
    }
    void setText(String value) { text = value; }
    String text() { return text; }
}

final class Button extends Widget {
    Button(String name) { super(name); }
}
// endregion

// region concrete
/** Concrete mediator: all interaction rules for the checkout form live in one place. */
final class CheckoutForm implements FormMediator {
    final TextField billing = new TextField("billing address");
    final Checkbox sameAsBilling = new Checkbox("ship to billing address");
    final TextField shipping = new TextField("shipping address");
    final Button submit = new Button("Place order");

    CheckoutForm() {
        for (Widget w : new Widget[] {billing, sameAsBilling, shipping, submit}) {
            w.attach(this);
        }
        submit.setEnabled(false);
    }

    @Override public void changed(Widget source) {
        if (source == sameAsBilling || (source == billing && sameAsBilling.checked())) {
            shipping.setEnabled(!sameAsBilling.checked());
            if (sameAsBilling.checked()) {
                shipping.setText(billing.text());
            }
        }
        submit.setEnabled(!billing.text().isBlank() && !shipping.text().isBlank());
        System.out.printf("  shipping=[%s, enabled=%s]  submit enabled=%s%n",
                shipping.text(), shipping.enabled(), submit.enabled());
    }
}
// endregion

public final class MediatorDemo {

    public static void main(String[] args) {
        CheckoutForm form = new CheckoutForm();
        form.billing.type("1 Main St");
        form.sameAsBilling.click();
        form.billing.type("2 High St");
        form.sameAsBilling.click();
        form.shipping.type("9 Dock Rd");
    }
}
