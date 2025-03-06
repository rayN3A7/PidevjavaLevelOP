package tn.esprit.Models;

import java.util.Date;

public class Session_game {
    private int id;
    private double prix;
    private Date date_creation;
    private String duree_session;
    private String game;
    private int coach_id;
    private String imageName; // Nouveau champ pour le nom de l'image

    public Session_game() {
    }

    public Session_game(int id, double prix, Date date_creation, String duree_session, String game, int coach_id, String imageName) {
        this.id = id;
        this.prix = prix;
        this.date_creation = date_creation;
        this.duree_session = duree_session;
        this.game = game;
        this.coach_id = coach_id;
        this.imageName = imageName;
    }

    public Session_game(double prix, Date date_creation, String duree_session, String game, int coach_id, String imageName) {
        this.prix = prix;
        this.date_creation = date_creation;
        this.duree_session = duree_session;
        this.game = game;
        this.coach_id = coach_id;
        this.imageName = imageName;
    }

    public Session_game(int id, double newPrice, Date date, String newDuration, String newGame, int i) {
    }

    // Getters et Setters
    public int getCoach_id() {
        return coach_id;
    }

    public void setCoach_id(int coach_id) {
        this.coach_id = coach_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getprix() {
        return prix;
    }

    public void setprix(double prix) {
        this.prix = prix;
    }

    public Date getdate_creation() {
        return date_creation;
    }

    public void setDateCreation(Date date_creation) {
        this.date_creation = date_creation;
    }

    public String getduree_session() {
        return duree_session;
    }

    public void setduree_session(String duree_session) {
        this.duree_session = duree_session;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", prix=" + prix +
                ", dateCreation=" + date_creation +
                ", dureeSession=" + duree_session +
                ", game='" + game + '\'' +
                ", coach_id=" + coach_id +
                ", imageName='" + imageName + '\'' +
                '}';
    }
}