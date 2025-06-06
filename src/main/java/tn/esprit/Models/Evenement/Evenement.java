package tn.esprit.Models.Evenement;

import java.sql.Date;
import java.sql.Timestamp;

public class Evenement {
    private int id,categorie_id,max_places_event;
    private String nom_event,lieu_event;
    private Timestamp date_event;
    private String photo_event;

    public Evenement() {
    }

    public Evenement(int id, int categorie_id, int max_places_event, String nom_event, String lieu_event,Timestamp date_event,String photo_event) {
        this.id = id;
        this.categorie_id = categorie_id;
        this.max_places_event = max_places_event;
        this.nom_event = nom_event;
        this.lieu_event = lieu_event;
        this.date_event = date_event;
        this.photo_event = photo_event;
    }

    public Evenement(int categorie_id, int max_places_event, String nom_event, String lieu_event,Timestamp date_event,String photo_event) {
        this.categorie_id = categorie_id;
        this.max_places_event = max_places_event;
        this.nom_event = nom_event;
        this.lieu_event = lieu_event;
        this.date_event = date_event;
        this.photo_event = photo_event;
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

    public Timestamp getDate_event() {
        return date_event;
    }

    public void setDate_event(Timestamp date_event) {
        this.date_event = date_event;
    }
    public String getPhoto_event() {
        return photo_event;
    }

    public void setPhoto_event(String photo_event) {
        this.photo_event = photo_event;
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
