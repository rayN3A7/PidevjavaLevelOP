package tn.esprit.test;


import tn.esprit.Models.*;
import tn.esprit.Services.ReportService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

public class Main {

    public static void main(String[] args) {

        UtilisateurService us = new UtilisateurService();
        ReportService rs=new ReportService();

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
       // System.out.println(us.loginUser("luffy@gmail.com","luffy@1235",true));
       // SessionManager.getInstance().logout();

       // us.loginUser("luffy@gmail.com","luffy@1235",true);
       // System.out.println(us.getByRole("COACH"));
        System.out.println(us.getByEmail("hsouna.sellami1@gmail.com"));



    }
}
