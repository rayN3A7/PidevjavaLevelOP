package tn.esprit.Models;

public class Commande {
    private int id;
    private int utilisateurId;
    private int produitId;
    private String status;

    // Constructeur par défaut
    public Commande() {}

    // Constructeur avec paramètres
    public Commande(int id, int utilisateurId, int produitId, String status) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.produitId = produitId;
        this.status = status;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public int getProduitId() {
        return produitId;
    }

    public void setProduitId(int produitId) {
        this.produitId = produitId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", produitId=" + produitId +
                ", status='" + status + '\'' +
                '}';
    }
}
