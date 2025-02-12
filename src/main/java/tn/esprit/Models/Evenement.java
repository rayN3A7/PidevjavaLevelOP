package tn.esprit.Models;

import java.sql.Date;

public class Evenement {
    private int id,categorie_id,max_places_event;
    private String nom_event,lieu_event;
    private Date date_event;

    public Evenement() {
    }

    public Evenement(int id, int categorie_id, int max_places_event, String nom_event, String lieu_event,Date date_event) {
        this.id = id;
        this.categorie_id = categorie_id;
        this.max_places_event = max_places_event;
        this.nom_event = nom_event;
        this.lieu_event = lieu_event;
        this.date_event = date_event;
    }

    public Evenement(int categorie_id, int max_places_event, String nom_event, String lieu_event,Date date_event) {
        this.categorie_id = categorie_id;
        this.max_places_event = max_places_event;
        this.nom_event = nom_event;
        this.lieu_event = lieu_event;
        this.date_event = date_event;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategorie_id() {
        return categorie_id;
    }

    public void setCategorie_id(int categorie_id) {
        this.categorie_id = categorie_id;
    }

    public int getMax_places_event() {
        return max_places_event;
    }

    public void setMax_places_event(int max_places_event) {
        this.max_places_event = max_places_event;
    }

    public String getNom_event() {
        return nom_event;
    }

    public void setNom_event(String nom_event) {
        this.nom_event = nom_event;
    }

    public String getLieu_event() {
        return lieu_event;
    }

    public void setLieu_event(String lieu_event) {
        this.lieu_event = lieu_event;
    }

    public Date getDate_event() {
        return date_event;
    }

    public void setDate_event(Date date_event) {
        this.date_event = date_event;
    }

    @Override
    public String toString() {
        return "Evenement{" +
                "id=" + id +
                ", categorie_id=" + categorie_id +
                ", max_places_event=" + max_places_event +
                ", nom_event='" + nom_event + '\'' +
                ", lieu_event='" + lieu_event +
                '}';
    }
}
