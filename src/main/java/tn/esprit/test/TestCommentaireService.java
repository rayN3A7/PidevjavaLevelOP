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

            // Create instances of services
            CommentaireService commentaireService = new CommentaireService();
            UtilisateurService utilisateurService = new UtilisateurService();
            QuestionService questionService = new QuestionService();

            // Fetch an existing user and question (Assuming IDs exist in the database)
            Utilisateur utilisateur = utilisateurService.getOne(1); // Replace 1 with a valid ID
            Question question = questionService.getOne(6); // Replace 1 with a valid ID

            if (utilisateur == null || question == null) {
                System.out.println("Utilisateur or Question not found. Check your database.");
                return;
            }

            // Create a new Commentaire object
            Commentaire newComment = new Commentaire(
                    "This is a test comment",
                    0,
                    new Timestamp(System.currentTimeMillis()), // Current timestamp
                    utilisateur,
                    question,
                    null // No parent comment
            );

            // Add comment to the database
            commentaireService.add(newComment);
            System.out.println("Comment added successfully!");

            // Fetch and display all comments
            System.out.println("All Comments:");
            for (Commentaire c : commentaireService.getAll()) {
                System.out.println(c);
            }
            testGetOne(commentaireService, 1); // Replace 1 with an existing Commentaire ID in your database
           // testUpdate(commentaireService, 1); // Replace 1 with an existing Commentaire ID
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
    // Method to test update
   /* public static void testUpdate(CommentaireService commentaireService, int commentaireId) {
        try {
            // Fetch the existing comment
            Commentaire commentaire = commentaireService.getOne(commentaireId);
            if (commentaire != null) {
                // Update the content of the comment
                commentaire.setContenu("This is an updated test comment.");
                commentaire.setVotes(5); // Updating votes for demonstration purposes
                commentaire.setCreation_at(new Timestamp(System.currentTimeMillis())); // Set new timestamp

                // Update the comment in the database
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
            // Fetch the existing comment
            Commentaire commentaire = commentaireService.getOne(commentaireId);
            if (commentaire != null) {
                // Delete the comment from the database
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
