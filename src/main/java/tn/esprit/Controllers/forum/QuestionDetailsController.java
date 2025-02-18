package tn.esprit.Controllers.forum;

import tn.esprit.Models.Commentaire;
import tn.esprit.Models.Question;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.CommentaireService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
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
            // Check if comment.getQuestion() is not null before accessing its question_id
            if (comment.getQuestion() != null && comment.getQuestion().getQuestion_id() == currentQuestion.getQuestion_id()) {
                createCommentCard(comment);
            }
        }
    }

    @FXML
    private void createCommentCard(Commentaire comment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/CommentCard.fxml"));
            VBox commentCard = loader.load();

            // Get the controller of the comment card and initialize it
            CommentCardController commentCardController = loader.getController();
            commentCardController.init(comment, this);

            // Add the comment card to the container (VBox)
            commentContainer.getChildren().add(commentCard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    public void postComment() {
        Utilisateur utilisateur = new Utilisateur(2, "yami", "sellami", "hsouna@gmail.com", "Yamimato", 1256969, "hsouna@1235", Role.COACH);

        if (utilisateur == null) {
            System.out.println("User is not logged in. Please log in first.");
            return;
        }

        String commentText = commentInput.getText(); // Use the correct reference here

        if (commentText == null || commentText.trim().isEmpty()) {
            showAlert("Erreur", "vous ne pouvez pas ajouter un commentaire vide.");
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

        // After adding the comment to the database, add it to the UI
        createCommentCard(commentaire);

        // Clear the input field
        commentInput.clear();
        loadComments();

    }
    public void handleUpvoteC(Commentaire commentaire, Label votesLabel, Button downvoteButton) {
        commentaireService.upvoteComment(commentaire.getCommentaire_id());

        int updatedVotes = commentaireService.getVotes(commentaire.getCommentaire_id());
        commentaire.setVotes(updatedVotes);

        Platform.runLater(() -> {
            votesLabel.setText("Votes: " + updatedVotes);

            // Reactivate downvote button if votes > 0
            if (updatedVotes > 0) {
                downvoteButton.setDisable(false);
            }
        });
    }



    public void handleDownvoteC(Commentaire commentaire, Label votesLabel, Button downvoteButton) {
        if (commentaire.getVotes() > 0) {
            commentaireService.downvoteComment(commentaire.getCommentaire_id());

            int updatedVotes = commentaireService.getVotes(commentaire.getCommentaire_id());
            commentaire.setVotes(updatedVotes);

            Platform.runLater(() -> {
                votesLabel.setText("Votes: " + updatedVotes);

                // Disable downvote button if votes reach 0
                downvoteButton.setDisable(updatedVotes == 0);
            });
        }
    }
    public void deleteComment(Commentaire commentaire) {
        commentaireService.delete(commentaire);
        System.out.println("Deleted question: " + commentaire.getContenu());
        refreshQuestions();
    }

    public void refreshQuestions() {
        loadComments();
    }
    /*private Utilisateur getCurrentUser() {
        int userId = SessionManager.getInstance().getUserId();
        if (userId == -1) {
            return null;
        }
        UtilisateurService utilisateurService = new UtilisateurService();
        return utilisateurService.getOne(userId);
    }*/

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(new Image(getClass().getResource("/forumUI/icons/alert.png").toExternalForm()));
        icon.setFitHeight(80);
        icon.setFitWidth(80);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/alert.png").toString()));

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        alert.showAndWait();
    }


}
