package hub.di;

/** Stand-in for an e-mail or SMS client. */
public final class ConsoleNotifier implements Notifier {

    @Override
    public void send(String recipient, String message) {
        System.out.println("  [notify " + recipient + "] " + message);
    }
}
