package tn.esprit.Models;

import java.sql.Timestamp;

public class Demande {
    private int id;
    private int userId;
    private String game;
    private String description;
    private byte[] file; // Store file content as a byte array
    private Timestamp date;

    // Constructors
    public Demande() {
    }

    public Demande(int userId, String game, String description, byte[] file) {
        this.userId = userId;
        this.game = game;
        this.description = description;
        this.file = file;
    }

    public Demande(int id, int userId, String game, String description, byte[] file, Timestamp date) {
        this.id = id;
        this.userId = userId;
        this.game = game;
        this.description = description;
        this.file = file;
        this.date = date;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Demande{" +
                "id=" + id +
                ", userId=" + userId +
                ", game='" + game + '\'' +
                ", description='" + description + '\'' +
                ", file=" + (file != null ? "file uploaded" : "no file") +
                ", date=" + date +
                '}';
    }
}