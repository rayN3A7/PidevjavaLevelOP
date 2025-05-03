package tn.esprit.Models;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Commande {
    private int id;
    private Utilisateur utilisateur;
    private Produit produit;
    private String status;
    private LocalDateTime createdAt;
    
    // Valid status values based on PHP entity
    public static final List<String> VALID_STATUS = Arrays.asList(
            "pending_payment", "en attente", "terminé", "annulé", "échec", "en cours", "en cours de paiement"
    );

    // Default constructor
    public Commande() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with parameters without id
    public Commande(Utilisateur utilisateur, Produit produit, String status) {
        this.utilisateur = utilisateur;
        this.produit = produit;
        setStatus(status); // Use setter to validate status
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with all parameters including id
    public Commande(int id, Utilisateur utilisateur, Produit produit, String status, LocalDateTime createdAt) {
        this.id = id;
        this.utilisateur = utilisateur;
        this.produit = produit;
        setStatus(status); // Use setter to validate status
        this.createdAt = createdAt;
    }

    // For backward compatibility
    public Commande(int id, int utilisateurId, int produitId, String status) {
        this.id = id;
        this.utilisateur = new Utilisateur();
        this.utilisateur.setId(utilisateurId);
        this.produit = new Produit();
        this.produit.setId(produitId);
        setStatus(status); // Use setter to validate status
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    // For backward compatibility
    public int getUtilisateurId() {
        return this.utilisateur != null ? this.utilisateur.getId() : 0;
    }

    // For backward compatibility
    public void setUtilisateurId(int utilisateurId) {
        if (this.utilisateur == null) {
            this.utilisateur = new Utilisateur();
        }
        this.utilisateur.setId(utilisateurId);
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    // For backward compatibility
    public int getProduitId() {
        return this.produit != null ? this.produit.getId() : 0;
    }

    // For backward compatibility
    public void setProduitId(int produitId) {
        if (this.produit == null) {
            this.produit = new Produit();
        }
        this.produit.setId(produitId);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        // Validate status against allowed values
        if (status != null && !VALID_STATUS.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status + 
                    ". Valid values are: " + String.join(", ", VALID_STATUS));
        }
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", utilisateur=" + (utilisateur != null ? utilisateur.getId() : "null") +
                ", produit=" + (produit != null ? produit.getId() : "null") +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
