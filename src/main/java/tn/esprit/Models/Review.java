package tn.esprit.Models;

import java.time.LocalDateTime;

public class Review {
    private int id;
    private Utilisateur utilisateur;
    private Produit produit;
    private String comment;
    private LocalDateTime createdAt;

    // Default constructor
    public Review() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with all fields except id
    public Review(Utilisateur utilisateur, Produit produit, String comment) {
        this.utilisateur = utilisateur;
        this.produit = produit;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with all fields including id
    public Review(int id, Utilisateur utilisateur, Produit produit, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.utilisateur = utilisateur;
        this.produit = produit;
        this.comment = comment;
        this.createdAt = createdAt;
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

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", utilisateur=" + (utilisateur != null ? utilisateur.getId() : "null") +
                ", produit=" + (produit != null ? produit.getId() : "null") +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 