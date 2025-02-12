package tn.esprit.test;


import tn.esprit.Models.Client;
import tn.esprit.Models.Coach;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.TokenUtil;

public class Main {

    public static void main(String[] args) {

        UtilisateurService us = new UtilisateurService();


        Utilisateur u1 =new Coach(
                "hsouna@gmail.com",
                "hsouna@1235",
                "Yamimato",
                "yami",
                1256969,
                "sellami",
                Role.CLIENT);

       // us.add(u1);

        // Authenticate user and generate JWT
        String token = us.authenticateUser("hsouna@gmail.com", "hsouna@1235");
        if (token != null) {
            System.out.println("Login successful. Token: " + token);

            // Verify JWT
            String email = TokenUtil.getEmailFromToken(token);
            Role role = TokenUtil.getRoleFromToken(token);
            System.out.println("Token is valid! Email: " + email + ", Role: " + role);
        } else {
            System.out.println("Login failed!");
        }

       // System.out.println(us.emailExists("hsouna.sellami2ad@gmail.com"));

       // us.delete(u1);

    }
}
