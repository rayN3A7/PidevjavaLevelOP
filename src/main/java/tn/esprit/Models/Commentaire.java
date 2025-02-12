package tn.esprit.Models;

import java.sql.Timestamp;

public class Commentaire {
        private int Commentaire_id;
        private String contenu;
        private int Votes;
        private Timestamp creation_at;
        private Utilisateur utilisateur;
        private Question question;
        private Commentaire parent_commentaire_id;

        // Getters and Setters


    public Commentaire() {
    }

    public Commentaire(Commentaire parent_commentaire_id, Question question, Utilisateur utilisateur, Timestamp creationAt, int votes, String contenu, int commentaireId) {
        this.parent_commentaire_id = parent_commentaire_id;
        this.question = question;
        this.utilisateur = utilisateur;
        this.creation_at = creationAt;
        this.Votes = votes;
        this.contenu = contenu;
        this.Commentaire_id = commentaireId;
    }

    public Commentaire(String contenu, int votes, Timestamp creationAt, Utilisateur utilisateur, Question question, Commentaire parent_commentaire_id) {
        this.contenu = contenu;
        this.Votes = 0;
        this.creation_at = creationAt;
        this.utilisateur = utilisateur;
        this.question = question;
        this.parent_commentaire_id = parent_commentaire_id;
    }
    public void Com_upvote() {
        this.Votes++;
    }

    // Method to downvote the question (optional)
    public void Com_downvote() {
        this.Votes--;
    }

    public int getCommentaire_id() {
        return Commentaire_id;
    }

    public void setCommentaire_id(int commentaire_id) {
        Commentaire_id = commentaire_id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public int getVotes() {
        return Votes;
    }

    public void setVotes(int votes) {
        Votes = votes;
    }

    public Timestamp getCreation_at() {
        return creation_at;
    }

    public void setCreation_at(Timestamp creation_at) {
        this.creation_at = creation_at;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Commentaire getParent_commentaire_id() {
        return parent_commentaire_id;
    }

    public void setParent_commentaire_id(Commentaire parent_commentaire_id) {
        this.parent_commentaire_id = parent_commentaire_id;
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "Commentaire_id=" + Commentaire_id +
                ", contenu='" + contenu + '\'' +
                ", Votes=" + Votes +
                ", creation_at=" + creation_at +
                ", utilisateur=" + utilisateur +
                ", question=" + question +
                ", parent_commentaire_id=" + parent_commentaire_id +
                '}';
    }
}
