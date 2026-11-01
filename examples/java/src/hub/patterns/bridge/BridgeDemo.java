package hub.patterns.bridge;

import java.util.ArrayList;
import java.util.List;

// region implementor
/** Implementor: the "how" dimension. Channels know how to deliver, not what a notification means. */
interface MessageSender {
    String channel();
    void deliver(String recipient, String subject, String body);
}

final class EmailSender implements MessageSender {
    @Override public String channel() { return "email"; }
    @Override public void deliver(String recipient, String subject, String body) {
        System.out.printf("  [email to %s] %s%n    %s%n", recipient, subject, body.replace("\n", "\n    "));
    }
}

final class SmsSender implements MessageSender {
    private static final int LIMIT = 60;           // kept small so the demo shows truncation
    @Override public String channel() { return "sms"; }
    @Override public void deliver(String recipient, String subject, String body) {
        String text = (subject + ": " + body).replace("\n", " ");
        if (text.length() > LIMIT) {
            text = text.substring(0, LIMIT - 3) + "...";
        }
        System.out.printf("  [sms to %s] %s%n", recipient, text);
    }
}
// endregion

// region abstraction
/** Abstraction: the "what" dimension. Holds a reference to the implementor: that reference is the bridge. */
abstract class Notification {
    protected final MessageSender sender;

    protected Notification(MessageSender sender) {
        this.sender = sender;
    }

    abstract void notify(String recipient, String message);
}

/** Refined abstraction: urgent alerts are prefixed and sent twice. */
final class UrgentAlert extends Notification {
    UrgentAlert(MessageSender sender) { super(sender); }

    @Override void notify(String recipient, String message) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            sender.deliver(recipient, "URGENT (" + attempt + "/2)", message);
        }
    }
}

/** Refined abstraction: digests collect messages and send them in one go. */
final class DailyDigest extends Notification {
    private final List<String> pending = new ArrayList<>();

    DailyDigest(MessageSender sender) { super(sender); }

    @Override void notify(String recipient, String message) {
        pending.add(message);
    }

    void flush(String recipient) {
        sender.deliver(recipient, "Your daily digest (" + pending.size() + " items)",
                String.join("\n", pending.stream().map(m -> "- " + m).toList()));
        pending.clear();
    }
}
// endregion

public final class BridgeDemo {

    public static void main(String[] args) {
        // 2 abstractions x 2 implementors = 4 combinations from 4 classes, not 4 subclasses.
        for (MessageSender sender : List.of(new EmailSender(), new SmsSender())) {
            System.out.println("Channel: " + sender.channel());
            new UrgentAlert(sender).notify("ops@example.com", "Disk usage on db-1 is at 95 percent");

            DailyDigest digest = new DailyDigest(sender);
            digest.notify("ana@example.com", "3 new comments on your pull request");
            digest.notify("ana@example.com", "Build 1042 passed");
            digest.flush("ana@example.com");
        }
    }
}
