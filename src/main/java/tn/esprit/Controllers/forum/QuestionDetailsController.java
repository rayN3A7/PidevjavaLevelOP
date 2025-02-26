package tn.esprit.Controllers.forum;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;
import tn.esprit.Models.*;
import tn.esprit.Services.CommentaireService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Services.GamesService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.io.File;
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
    @FXML
    private ImageView gameImageView;
    private Question currentQuestion;
    private UtilisateurService us = new UtilisateurService();
    private CommentaireService commentaireService = new CommentaireService();
    private int userId = SessionManager.getInstance().getUserId();
    private GamesService gamesService = new GamesService(); // New service instance
    @FXML
    public void loadQuestionDetails(Question question) {
        this.currentQuestion = question;
        questionTitle.setText(question.getTitle());
        questionContent.setText(question.getContent());
        questionVotes.setText("Votes: " + question.getVotes());

        Games game = gamesService.getByName(question.getGame().getGame_name());
        if (game != null && game.getImagePath() != null && !game.getImagePath().isEmpty()) {
            File file = new File(game.getImagePath());
            if (file.exists()) {
                Image image = new Image(file.toURI().toString(), 200, 150, true, true);
                if (!image.isError()) {
                    gameImageView.setImage(image);
                }
            }
        }

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
        UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(userId);

        createCommentCard(commentaire);
        commentInput.clear();
        loadComments();
        if (change.isChanged()) {
            showPrivilegeAlert(change);
            refreshQuestions();

        }

    }

    @FXML
    private void createCommentCard(Commentaire comment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/CommentCard.fxml"));
            VBox commentCard = loader.load();

            CommentCardController commentCardController = loader.getController();
            commentCardController.setCommentData(comment, this);
            commentContainer.getChildren().add(commentCard);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void handleUpvoteC(Commentaire commentaire, Label votesLabel, Button downvoteButton) {
        CommentaireService cs = new CommentaireService();
        int updatedVotes = cs.getVotes(commentaire.getCommentaire_id());
        commentaire.setVotes(updatedVotes);
        cs.upvoteComment(commentaire.getCommentaire_id());
        commentaire.Com_upvote();

        Platform.runLater(() -> {

            votesLabel.setText("Votes: " + updatedVotes);
            votesLabel.setVisible(true);
            UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(userId);
            if (change.isChanged()) {
                showPrivilegeAlert(change);
                refreshQuestions();
            }
            if (updatedVotes > 0) {
                downvoteButton.setDisable(false);
            }
        });
    }

    private void showPrivilegeAlert(UtilisateurService.PrivilegeChange change) {
        if (!change.isChanged()) return;

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setHeaderText(null);
        String oldPrivilege = change.getOldPrivilege();
        String newPrivilege = change.getNewPrivilege();
        boolean isPromotion = getPrivilegeRank(newPrivilege) > getPrivilegeRank(oldPrivilege);

        if (isPromotion) {
            alert.setTitle("Félicitations!");
            String message = switch (newPrivilege) {
                case "top_contributor" -> "Vous êtes passé de Regular à Top Contributor ! Bravo pour votre contribution !";
                case "top_fan" -> "Vous êtes maintenant un Top Fan depuis " + oldPrivilege + " ! Votre passion est récompensée !";
                default -> "Privilege mis à jour !";
            };
            alert.setContentText(message);

            ImageView icon = new ImageView(new Image(getClass().getResource(
                    newPrivilege.equals("top_contributor") ? "/forumUI/icons/silver_crown.png" : "/forumUI/icons/crown.png"
            ).toExternalForm()));
            icon.setFitHeight(60);
            icon.setFitWidth(60);
            alert.setGraphic(icon);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/sucessalert.png").toString()));
        } else {
            alert.setTitle("Mise à jour de privilège");
            String message = switch (oldPrivilege) {
                case "top_contributor" -> "Désolé, vous êtes redescendu de Top Contributor à Regular.";
                case "top_fan" -> "Désolé, vous êtes passé de Top Fan à " + newPrivilege + ".";
                default -> "Privilege mis à jour.";
            };
            alert.setContentText(message);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/alert.png").toString()));
        }

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("privilege-alert");

        ButtonType okButton = new ButtonType(isPromotion ? "GG!" : "OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), alert.getDialogPane());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        alert.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) fadeIn.play();
        });

        alert.showAndWait();
    }

    private int getPrivilegeRank(String privilege) {
        return switch (privilege) {
            case "regular" -> 0;
            case "top_contributor" -> 1;
            case "top_fan" -> 2;
            default -> -1;
        };
    }
    public void handleDownvoteC(Commentaire commentaire, Label votesLabel, Button downvoteButton) {
        CommentaireService cs = new CommentaireService();
        int updatedVotes = cs.getVotes(commentaire.getCommentaire_id());
        commentaire.setVotes(updatedVotes);
        cs.downvoteComment(commentaire.getCommentaire_id());
        commentaire.Com_downvote();



        Platform.runLater(() -> {
            votesLabel.setText("Votes: " + updatedVotes);
            votesLabel.setVisible(true);
            UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(userId);
            if (change.isChanged()) {
                showPrivilegeAlert(change);
                refreshQuestions();
            }
            downvoteButton.setDisable(updatedVotes == 0);
        });

    }

    public void deleteComment(Commentaire commentaire) {
        commentaireService.delete(commentaire);
        System.out.println("Deleted comment: " + commentaire.getContenu());
        UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(userId);
        if (change.isChanged()) {
            showPrivilegeAlert(change);
            refreshQuestions();
        }
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

        String existingReaction = service.getUserReaction(commentaire.getCommentaire_id(), userId);
        if (existingReaction != null) {
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

        service.addReaction(commentaire.getCommentaire_id(), userId, emojiUrl);
        Map<String, Integer> updatedReactions = service.getReactions(commentaire.getCommentaire_id());
        commentaire.setReactions(updatedReactions);
        commentaire.setUserReaction(emojiUrl);
    }
}