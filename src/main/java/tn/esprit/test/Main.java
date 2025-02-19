package tn.esprit.test;


import tn.esprit.Models.Coach;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.EmailService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

public class Main {

    public static void main(String[] args) {

        UtilisateurService us = new UtilisateurService();


        Utilisateur u1 =new Coach(
                "luffy@gmail.com",
                "luffy@1235",
                "mugiwara",
                "luffy",
                69696969,
                ".D",
                Role.CLIENT);

        //us.add(u1);

       // System.out.println(us.getByEmail("luffy@gmail.com"));
        System.out.println(us.loginUser("luffy@gmail.com","dfkbizq6526é4^|@ZA",false));
       // System.out.println(SessionManager.getInstance().isLoggedIn());
        //System.out.println(us.nicknameExists("yamimato"));
       // EmailService emailService = new EmailService();
       // System.out.println(emailService.generateOtp());
       // emailService.sendOtpEmail("hsouna.sellami@gmail.com",emailService.generateOtp());

       // us.updatePassword(u1.getEmail(),"dfkbizq6526é4^|@ZA");
    }
}
