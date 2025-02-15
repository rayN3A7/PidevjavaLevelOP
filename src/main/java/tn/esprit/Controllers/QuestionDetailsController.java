package tn.esprit.Controllers;

import tn.esprit.Models.Commentaire;
import tn.esprit.Models.Question;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.CommentaireService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

public class QuestionDetailsController {

    @FXML
    private Label questionTitle;
    @FXML
    private Label questionContent;
    @FXML
    private Label questionVotes;
    @FXML
    private TextField commentInput; // Updated reference for the comment input
    @FXML
    private VBox commentContainer;

    private Question currentQuestion;
    private CommentaireService commentaireService = new CommentaireService();

    @FXML
    public void loadQuestionDetails(Question question) {
        this.currentQuestion = question;
        questionTitle.setText(question.getTitle());
        questionContent.setText(question.getContent());
        questionVotes.setText("Votes: " + question.getVotes());

        loadComments();
    }

    @FXML
    private void loadComments() {

            commentContainer.getChildren().clear();
            // Load all comments for the current question
            List<Commentaire> comments = commentaireService.getAll();
            for (Commentaire comment : comments) {
                if (comment.getQuestion().getQuestion_id() == currentQuestion.getQuestion_id()) {
                    createCommentCard(comment);
                }
            }

    }

    @FXML
    // Method to create a comment card dynamically
    private void createCommentCard(Commentaire comment) {
        VBox commentCard = new VBox(5);
        commentCard.setStyle("-fx-background-color: #f4f4f4; -fx-border-radius: 5px; -fx-padding: 10px;");

        Label commentAuthor = new Label(comment.getUtilisateur().getNickname());
        commentAuthor.setStyle("-fx-font-weight: bold;");
        Label commentContent = new Label(comment.getContenu());
        commentContent.setStyle("-fx-font-size: 14px;");

        commentCard.getChildren().addAll(commentAuthor, commentContent);
        commentContainer.getChildren().add(commentCard);
    }

    @FXML
    public void postComment() {
        // Ensure that the user is logged in and retrieve their 'Utilisateur'
        Utilisateur utilisateur = getCurrentUser();

        if (utilisateur == null) {
            System.out.println("User is not logged in. Please log in first.");
            return; // Prevent the comment from being posted
        }

        // Get the comment text from the TextField
        String commentText = commentInput.getText(); // Use the correct reference here

        // Check if the comment text is empty
        if (commentText == null || commentText.trim().isEmpty()) {
            System.out.println("Comment cannot be empty.");
            return;
        }

        // Create a new Commentaire object
        Commentaire commentaire = new Commentaire();
        commentaire.setContenu(commentText);
        commentaire.setUtilisateur(utilisateur); // Set the logged-in user
        commentaire.setCreation_at(new java.sql.Timestamp(System.currentTimeMillis())); // Set current timestamp

        // Set the current question (you can adjust this logic to suit your actual code)
        commentaire.setQuestion(currentQuestion);


            // Call the service to add the comment
            commentaireService.add(commentaire);
            System.out.println("Comment added successfully!");

    }

    // Placeholder method for retrieving the current logged-in user
    private Utilisateur getCurrentUser() {
        return null; // Replace with actual logic
    }
}
