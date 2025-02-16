package tn.esprit.utils;

import tn.esprit.Models.Role;

import java.io.*;
import java.nio.file.*;

public class SessionManager {
    private static SessionManager instance;
    private int userId;
    private Role role;
    private String email;
    private boolean rememberMe; // 🔹 NEW: Controls if session is saved

    private static final String SESSION_FILE = "session.txt"; // File for session storage

    private SessionManager() {
        this.userId = -1;
        this.role = null;
        this.email = null;
        this.rememberMe = false;
        loadSession(); // Load session if it was saved
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(int userId, Role role, String email, boolean rememberMe) {
        this.userId = userId;
        this.role = role;
        this.email = email;
        this.rememberMe = rememberMe; // 🔹 Store user choice

        if (rememberMe) {
            saveSession();
        } else {
            clearSession(); // 🔹 If not remembered, ensure no session is saved
        }
    }

    public void logout() {
        this.userId = -1;
        this.role = null;
        this.email = null;
        this.rememberMe = false;
        clearSession(); // Always clear session on logout
    }

    public boolean isLoggedIn() {
        return userId != -1;
    }

    public int getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    // ✅ Save session if "rememberMe" is true
    private void saveSession() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(SESSION_FILE))) {
            writer.write(userId + "\n" + role.name() + "\n" + email + "\n" + rememberMe);
        } catch (IOException e) {
            System.out.println("Failed to save session: " + e.getMessage());
        }
    }

    // ✅ Load session from file (only if "rememberMe" was true)
    private void loadSession() {
        if (Files.exists(Paths.get(SESSION_FILE))) {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(SESSION_FILE))) {
                this.userId = Integer.parseInt(reader.readLine());
                this.role = Role.valueOf(reader.readLine());
                this.email = reader.readLine();
                this.rememberMe = Boolean.parseBoolean(reader.readLine());

                if (!rememberMe) {
                    clearSession(); // 🔹 If rememberMe is false, clear session
                }
            } catch (IOException e) {
                System.out.println("Failed to load session: " + e.getMessage());
            }
        }
    }

    // ✅ Clear session file when logging out or when "rememberMe" is false
    private void clearSession() {
        try {
            Files.deleteIfExists(Paths.get(SESSION_FILE));
        } catch (IOException e) {
            System.out.println("Failed to clear session: " + e.getMessage());
        }
    }
}
