package tn.esprit.Models;

import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;

public class Utilisateur  {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;
    private String email;
    private String motPasse;
    private String nickname;
    private String nom;
    private int numero;
    private String prenom;
    private Role role;
    private String privilege = "regular";
    private boolean ban;
    private LocalDateTime banTime;
    private int countRep;
    private String photo;

    public String getPhoto() {
        return photo;
    }

    public Utilisateur(String email, String motPasse, String nickname, String nom, int numero, String prenom, Role role, String photo) {
        this.email = email;
        this.motPasse = motPasse;
        this.nickname = nickname;
        this.nom = nom;
        this.numero = numero;
        this.prenom = prenom;
        this.role = role;
        this.photo = photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getCountRep() {
        return countRep;
    }

    public void setCountRep(int countRep) {
        this.countRep = countRep;
    }

    public LocalDateTime getBanTime() {
        return banTime;
    }

    public void setBanTime(LocalDateTime banTime) {
        this.banTime = banTime;
    }

    public Utilisateur(int id, String email, String nickname, String nom, int numero, String prenom, Role role, String privilege, boolean ban, LocalDateTime banTime) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.nom = nom;
        this.numero = numero;
        this.prenom = prenom;
        this.role = role;
        this.privilege = privilege;
        this.ban = ban;
        this.banTime = banTime;
    }

    public Utilisateur(int id, String email, String nickname, String nom, int numero, String prenom, Role role, String privilege, boolean ban) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.nom = nom;
        this.numero = numero;
        this.prenom = prenom;
        this.role = role;
        this.privilege = privilege;
        this.ban = ban;
    }

    public boolean isBan() {
        return ban;
    }

    public void setBan(boolean ban) {
        this.ban = ban;
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

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
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
                ", ban=" + ban +
                ", banTime=" + banTime +
                '}';
    }
}
