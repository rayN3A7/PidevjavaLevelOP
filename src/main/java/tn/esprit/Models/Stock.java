package tn.esprit.Models;

public class Stock {
    private int id;
    private int produitId;
    private int gamesId;
    private int quantity;
    private int prixProduit;
    private String image;

    // Constructeur par défaut
    public Stock() {}

    // Constructeur avec paramètres
    public Stock(int id, int produitId, int gamesId, int quantity, int prixProduit, String image) {
        this.id = id;
        this.produitId = produitId;
        this.gamesId = gamesId;
        this.quantity = quantity;
        this.prixProduit = prixProduit;
        this.image = image;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProduitId() {
        return produitId;
    }

    public void setProduitId(int produitId) {
        this.produitId = produitId;
    }

    public int getGamesId() {
        return gamesId;
    }

    public void setGamesId(int gamesId) {
        this.gamesId = gamesId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrixProduit() {
        return prixProduit;
    }

    public void setPrixProduit(int prixProduit) {
        this.prixProduit = prixProduit;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + id +
                ", produitId=" + produitId +
                ", gamesId=" + gamesId +
                ", quantity=" + quantity +
                ", prixProduit=" + prixProduit +
                ", image='" + image + '\'' +
                '}';
    }
}
