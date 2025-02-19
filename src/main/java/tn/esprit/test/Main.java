package tn.esprit.test;


import tn.esprit.Models.Client;
import tn.esprit.Models.Coach;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

public class Main {

    public static void main(String[] args) {

        UtilisateurService us = new UtilisateurService();


        Utilisateur u1 =new Client(
                "hazem@gmail.com",
                "luffy@1235",
                "hazem",
                "hazem",
                69696969,
                ".D",
                Role.CLIENT);

        //us.add(u1);
        SessionManager.getInstance().logout();
       // System.out.println(us.getByEmail("luffy@gmail.com"));
        System.out.println(us.loginUser("hazem@gmail.com","luffy@1235",true));
        System.out.println(SessionManager.getInstance().isLoggedIn());
    }
}
