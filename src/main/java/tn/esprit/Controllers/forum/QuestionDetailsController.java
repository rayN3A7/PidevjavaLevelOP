package tn.esprit.Controllers.forum;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class QuestionDetailsController {

    @FXML
    private Label questionTitle;
    @FXML
    private Label questionContent;
    @FXML
    private Label questionVotes;
    @FXML
    private TextField commentInput;
    @FXML
    private VBox commentContainer;

    private Question currentQuestion;
    private UtilisateurService us = new UtilisateurService();
    private CommentaireService commentaireService = new CommentaireService();
    private int userId = SessionManager.getInstance().getUserId();

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
        List<Commentaire> comments = commentaireService.getAll();
        for (Commentaire comment : comments) {
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

            CommentCardController commentCardController = loader.getController();
            commentCardController.init(comment, this);

            commentContainer.getChildren().add(commentCard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void postComment() {
        Utilisateur utilisateur = us.getOne(userId);
        if (utilisateur == null) {
            System.out.println("User is not logged in. Please log in first.");
            return;
        }

        String commentText = commentInput.getText();

        if (commentText == null || commentText.trim().isEmpty()) {
            showAlert("Erreur", "Vous ne pouvez pas ajouter un commentaire vide.");
            return;
        }

        Commentaire commentaire = new Commentaire();
        commentaire.setContenu(commentText);
        commentaire.setUtilisateur(utilisateur);
        commentaire.setCreation_at(new java.sql.Timestamp(System.currentTimeMillis()));
        commentaire.setQuestion(currentQuestion);

        commentaireService.add(commentaire);
        System.out.println("Comment added successfully!");

        createCommentCard(commentaire);
        commentInput.clear();
        loadComments();
    }

    public void handleUpvoteC(Commentaire commentaire, Label votesLabel, Button downvoteButton) {
        commentaireService.upvoteComment(commentaire.getCommentaire_id());

        int updatedVotes = commentaireService.getVotes(commentaire.getCommentaire_id());
        commentaire.setVotes(updatedVotes);

        Platform.runLater(() -> {
            votesLabel.setText("Votes: " + updatedVotes);
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
                downvoteButton.setDisable(updatedVotes == 0);
            });
        }
    }

    public void deleteComment(Commentaire commentaire) {
        commentaireService.delete(commentaire);
        System.out.println("Deleted comment: " + commentaire.getContenu());
        refreshQuestions();
    }

    public void refreshQuestions() {
        loadComments();
    }

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

    @FXML
    private void goToForumPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/Forum.fxml"));
            Parent forumView = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(forumView));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleReactionC(Commentaire commentaire, String emojiUrl) {
        int userId = SessionManager.getInstance().getUserId();
        CommentaireService service = new CommentaireService();

        // Check if the user has already reacted to this comment
        String existingReaction = service.getUserReaction(commentaire.getCommentaire_id(), userId);
        if (existingReaction != null) {
            // If the user has reacted, remove the existing reaction
            service.removeReaction(commentaire.getCommentaire_id(), userId);
            commentaire.getReactions().remove(existingReaction);
            if (commentaire.getReactions().containsKey(existingReaction)) {
                int currentCount = commentaire.getReactions().get(existingReaction);
                if (currentCount > 1) {
                    commentaire.getReactions().put(existingReaction, currentCount - 1);
                } else {
                    commentaire.getReactions().remove(existingReaction);
                }
            }
        }

        // Add the new reaction
        service.addReaction(commentaire.getCommentaire_id(), userId, emojiUrl);
        // Update the comment's reactions and user reaction
        Map<String, Integer> updatedReactions = service.getReactions(commentaire.getCommentaire_id());
        commentaire.setReactions(updatedReactions);
        commentaire.setUserReaction(emojiUrl); // Set the user's specific reaction (image URL)
    }
}