package hub.di;

public interface Notifier {

    void send(String recipient, String message);
}
