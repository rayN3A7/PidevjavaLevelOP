package tn.esprit.Models;

import java.util.Date;

public class Reservation {
    private int id;
    private Date date_reservation;
    private Session_game sessiongame;
    private int client_id; // Nouvel attribut

    public Reservation() {
    }

    public Reservation(int id, Date date_reservation, Session_game sessiongame, int client_id) {
        this.id = id;
        this.date_reservation = date_reservation;
        this.sessiongame = sessiongame;
        this.client_id = client_id;
    }

    public Reservation(Date date_reservation, Session_game sessiongame, int client_id) {
        this.date_reservation = date_reservation;
        this.sessiongame = sessiongame;
        this.client_id = client_id; // Initialisation du client_id
    }

    // Getters et Setters
    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getdate_reservation() {
        return date_reservation;
    }

    public void setdate_reservation(Date dateParticipation) {
        this.date_reservation = dateParticipation;
    }

    public Session_game getSession() {
        return sessiongame;
    }

    public void setSession(Session_game sessiongame) {
        this.sessiongame = sessiongame;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", dateParticipation=" + date_reservation +
                ", session=" + sessiongame +
                '}';
    }
}