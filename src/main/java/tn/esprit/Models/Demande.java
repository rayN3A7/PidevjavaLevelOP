package tn.esprit.Models;

import java.sql.Timestamp;

public class Demande {
    private int id;
    private int userId;
    private String game;
    private String description;
    private String filePath; // Store file path instead of file content
    private Timestamp date;

    // Constructors
    public Demande() {}

    public Demande(int userId, String game, String description, String filePath) {
        this.userId = userId;
        this.game = game;
        this.description = description;
        this.filePath = filePath;
    }

    public Demande(int id, int userId, String game, String description, String filePath, Timestamp date) {
        this.id = id;
        this.userId = userId;
        this.game = game;
        this.description = description;
        this.filePath = filePath;
        this.date = date;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }

    @Override
    public String toString() {
        return "Demande{" +
                "id=" + id +
                ", userId=" + userId +
                ", game='" + game + '\'' +
                ", description='" + description + '\'' +
                ", filePath='" + filePath + '\'' +
                ", date=" + date +
                '}';
    }
}
