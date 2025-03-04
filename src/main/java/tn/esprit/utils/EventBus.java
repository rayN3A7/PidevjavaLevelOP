package tn.esprit.utils;

import javafx.application.Platform;
import javafx.event.EventHandler;
import java.util.ArrayList;
import java.util.List;

public class EventBus {
    private static final EventBus INSTANCE = new EventBus();
    private final List<EventHandler<PrivilegeEvent>> handlers = new ArrayList<>();

    private EventBus() {}

    public static EventBus getInstance() {
        return INSTANCE;
    }

    public void addHandler(EventHandler<PrivilegeEvent> handler) {
        synchronized (handlers) {
            handlers.add(handler);
        }
    }

    public void removeHandler(EventHandler<PrivilegeEvent> handler) {
        synchronized (handlers) {
            handlers.remove(handler);
        }
    }

    public void fireEvent(PrivilegeEvent event) {
        Platform.runLater(() -> {
            synchronized (handlers) {
                for (EventHandler<PrivilegeEvent> handler : handlers) {
                    handler.handle(event);
                }
            }
        });
    }
}