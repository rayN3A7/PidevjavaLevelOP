package tn.esprit.Models;

public class Produit {
    private int id;
    private String nomProduit;
    private String description;
    private int score;
    
    // Constructeur par défaut
    public Produit() {}
    
    // Constructeur avec paramètres
    public Produit(int id, String nomProduit, String description, int score) {
        this.id = id;
        this.nomProduit = nomProduit;
        this.description = description;
        this.score = score;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNomProduit() {
        return nomProduit;
    }
    
    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nomProduit='" + nomProduit + '\'' +
                ", description='" + description + '\'' +
                ", score=" + score +
                '}';
    }
}
