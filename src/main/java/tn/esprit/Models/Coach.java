package tn.esprit.Models;

public class Coach extends Utilisateur{


    public Coach(String email, String motPasse, String nickname, String nom, int numero, String prenom, Role role) {
        super(email, motPasse, nickname, nom, numero, prenom, role);
    }

    public Coach() {
    }
}
