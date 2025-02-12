package tn.esprit.Models;

public class Client extends Utilisateur{

    public Client(String email, String motPasse, String nickname, String nom, int numero, String prenom, Role role) {
        super(email, motPasse, nickname, nom, numero, prenom, role);
    }

    public Client() {
    }
}
