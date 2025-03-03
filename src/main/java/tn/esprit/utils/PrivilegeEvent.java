package tn.esprit.utils;

import javafx.event.Event;
import javafx.event.EventType;

public class PrivilegeEvent extends Event {
    public static final EventType<PrivilegeEvent> PRIVILEGE_CHANGED = new EventType<>(Event.ANY, "PRIVILEGE_CHANGED");
    private final int userId;
    private final String newPrivilege;

    public PrivilegeEvent(int userId, String newPrivilege) {
        super(PRIVILEGE_CHANGED);
        this.userId = userId;
        this.newPrivilege = newPrivilege;
    }

    public int getUserId() {
        return userId;
    }

    public String getNewPrivilege() {
        return newPrivilege;
    }
}