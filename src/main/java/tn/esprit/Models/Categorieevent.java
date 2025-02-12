package tn.esprit.Models;

public class Categorieevent {
    private int id;
    private String descriptioncategorie,nom;

    public Categorieevent() {
    }

    public Categorieevent(int id, String descriptioncategorie, String nom) {
        this.id = id;
        this.descriptioncategorie = descriptioncategorie;
        this.nom = nom;
    }

    public Categorieevent(String descriptioncategorie, String nom) {
        this.descriptioncategorie = descriptioncategorie;
        this.nom = nom;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getDescriptionCategorie() {
        return descriptioncategorie;
    }

    public void setDescriptioncategorie(String descriptioncategorie) {
        this.descriptioncategorie = descriptioncategorie;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


    @Override
    public String toString() {
        return "CategorieEvent{" +
                "id=" + id +
                ", descriptioncategorie='" + descriptioncategorie + '\'' +
                ", nom='" + nom + '\'' +
                '}';
    }
}
