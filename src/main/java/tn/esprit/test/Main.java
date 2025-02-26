package tn.esprit.test;


import tn.esprit.Models.Coach;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

public class Main {

    public static void main(String[] args) {

        UtilisateurService us = new UtilisateurService();


       /* Utilisateur u1 =new Coach(
                "luffy@gmail.com",
                "luffy@1235",
                "mugiwara",
                "luffy",
                69696969,
                ".D",
                Role.CLIENT);*/

       //us.updatePassword("luffy@gmail.com","luffy@1235");
       // System.out.println(us.getByEmail("luffy@gmail.com"));
        System.out.println(us.loginUser("hsouna.sellami@gmail.com","Hsouna999@",true));
        //SessionManager.getInstance().logout();

        System.out.println(us.getEmail(16));


    }
}
