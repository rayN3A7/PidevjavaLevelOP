package tn.esprit.test;



import tn.esprit.Models.Commentaire;
import tn.esprit.Models.Question;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.CommentaireService;
import tn.esprit.Services.QuestionService;
import tn.esprit.Services.UtilisateurService;

import java.sql.Timestamp;

public class TestCommentaireService {
    public static void main(String[] args) {
            CommentaireService commentaireService = new CommentaireService();
            UtilisateurService utilisateurService = new UtilisateurService();
            QuestionService questionService = new QuestionService();
            Utilisateur utilisateur = utilisateurService.getOne(1);
            Question question = questionService.getOne(51);

            if (utilisateur == null || question == null) {
                System.out.println("Utilisateur or Question not found. Check your database.");
                return;
            }
            Commentaire newComment = new Commentaire(
                    "no it's a right one",
                    0,
                    new Timestamp(System.currentTimeMillis()),
                    utilisateur,
                    question,
                    null
            );
            commentaireService.add(newComment);
            System.out.println("Comment added successfully!");
            System.out.println("All Comments:");
            for (Commentaire c : commentaireService.getAll()) {
                System.out.println(c);
            }
            testGetOne(commentaireService, 20); // Replace 1 with an existing Commentaire ID in your database
           // testUpdate(commentaireService, 1);
         //   testDelete(commentaireService, 1);
    }
    public static void testGetOne(CommentaireService commentaireService, int commentaireId) {
            Commentaire commentaire = commentaireService.getOne(commentaireId);
            if (commentaire != null) {
                System.out.println("Fetched Commentaire: " + commentaire);
            } else {
                System.out.println("No Commentaire found with ID: " + commentaireId);
            }
    }
   /* public static void testUpdate(CommentaireService commentaireService, int commentaireId) {
        try {
            Commentaire commentaire = commentaireService.getOne(commentaireId);
            if (commentaire != null) {
                commentaire.setContenu("This is an updated test comment.");
                commentaire.setVotes(5); // Updating votes for demonstration purposes
                commentaire.setCreation_at(new Timestamp(System.currentTimeMillis())); // Set new timestamp
                commentaireService.update(commentaire);
                System.out.println("Commentaire updated successfully!");
            } else {
                System.out.println("No Commentaire found with ID: " + commentaireId);
            }
        } catch (SQLException e) {
            System.out.println("Error updating Commentaire with ID: " + commentaireId);
            e.printStackTrace();
        }
    }
   public static void testDelete(CommentaireService commentaireService, int commentaireId) {
        try {
            Commentaire commentaire = commentaireService.getOne(commentaireId);
            if (commentaire != null) {
                commentaireService.delete(commentaire);
                System.out.println("Commentaire deleted successfully!");
            } else {
                System.out.println("No Commentaire found with ID: " + commentaireId);
            }
        } catch (SQLException e) {
            System.out.println("Error deleting Commentaire with ID: " + commentaireId);
            e.printStackTrace();
        }
    }*/
}
