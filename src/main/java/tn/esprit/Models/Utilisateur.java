package tn.esprit.Models;

import org.mindrot.jbcrypt.BCrypt;

public class Utilisateur  {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;
    private String email;
    private String motPasse; // Store hashed password
    private String nickname;
    private String nom;
    private int numero;
    private String prenom;
    private Role role;
    private String privilege = "regular";

    // New getters and setters
    public String getPrivilege() {
        return privilege;
    }
    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public Utilisateur(int id, String email, String motPasse, String nickname, String nom, int numero, String prenom, Role role) {
        this.id = id;
        this.email = email;
        this.motPasse = motPasse;
        this.nickname = nickname;
        this.nom = nom;
        this.numero = numero;
        this.prenom = prenom;
        this.role = role;
    }



    // Constructor
    public Utilisateur(String email, String motPasse, String nickname, String nom, int numero, String prenom, Role role) {
        this.email = email;
        this.motPasse = hashPassword(motPasse);
        this.nickname = nickname;
        this.nom = nom;
        this.numero = numero;
        this.prenom = prenom;
        this.role = role;
    }

    public Utilisateur(String email, String nickname, String nom, int numero, String prenom, Role role) {
        this.email = email;
        this.nickname = nickname;
        this.nom = nom;
        this.numero = numero;
        this.prenom = prenom;
        this.role = role;
    }

    public Utilisateur(String email, String nickname, String nom, int numero, String prenom, Role role, int id) {
        this.email = email;
        this.nickname = nickname;
        this.nom = nom;
        this.numero = numero;
        this.prenom = prenom;
        this.role = role;
        this.id = id;
    }

    public Utilisateur() {
    }



    private String hashPassword(String motPasse) {
        return BCrypt.hashpw(motPasse, BCrypt.gensalt(12));
    }


    public boolean authenticate(String motPasse) {
        return BCrypt.checkpw(motPasse, this.motPasse);
    }


    public String getEmail() { return email; }
    public Role getRole() { return role; }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotPasse() {
        return motPasse;
    }

    public void setMotPasse(String motPasse) {
        this.motPasse = motPasse;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +

                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", nom='" + nom + '\'' +
                ", numero=" + numero +
                ", prenom='" + prenom + '\'' +
                ", role=" + role +
                ", privilege='" + privilege + '\'' +
                '}';
    }
}
