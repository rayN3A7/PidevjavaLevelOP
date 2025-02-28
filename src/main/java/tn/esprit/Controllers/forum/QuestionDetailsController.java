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
import tn.esprit.utils.ProfanityChecker;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private GamesService gamesService = new GamesService();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private static final Map<String, Image> imageCache = new HashMap<>();

    @FXML
    public void loadQuestionDetails(Question question) {
        this.currentQuestion = question;
        questionTitle.setText(question.getTitle());
        questionContent.setText(question.getContent());
        questionVotes.setText("Votes: " + question.getVotes());

        executorService.submit(() -> {
            Games game = gamesService.getByName(question.getGame().getGame_name());
            if (game != null && game.getImagePath() != null && !game.getImagePath().isEmpty()) {
                File file = new File(game.getImagePath());
                if (file.exists()) {
                    String cacheKey = game.getImagePath();
                    Image image = imageCache.computeIfAbsent(cacheKey, k -> new Image(file.toURI().toString(), 200, 150, true, true));
                    if (!image.isError()) {
                        Platform.runLater(() -> gameImageView.setImage(image));
                    } else {
                        System.err.println("Failed to load game image: " + game.getImagePath());
                    }
                }
            }
        });

        loadComments();
    }

    @FXML
    private void loadComments() {
        executorService.submit(() -> {
            List<Commentaire> comments = commentaireService.getAll();
            Platform.runLater(() -> {
                commentContainer.getChildren().clear();
                for (Commentaire comment : comments) {
                    if (comment.getQuestion() != null && comment.getQuestion().getQuestion_id() == currentQuestion.getQuestion_id()) {
                        createCommentCard(comment);
                    }
                }
            });
        });
    }

    @FXML
    public void postComment() {
        Utilisateur utilisateur = us.getOne(userId);
        if (utilisateur == null) return;

        String commentText = commentInput.getText().trim();
        if (commentText.isEmpty()) {
            showAlert("Erreur", "Vous ne pouvez pas ajouter un commentaire vide.");
            return;
        }

        executorService.submit(() -> {
            try {
                if (ProfanityChecker.containsProfanity(commentText)) {
                    final boolean[] proceed = {false}; // Use an array to update from within lambda
                    Platform.runLater(() -> {
                        proceed[0] = showProfanityWarningAlert("Avertissement",
                                "Votre commentaire contient des mots inappropriés. Vous risquez d'être banni ou signalé. Voulez-vous ajouter quand même?");
                    });
                    // Wait for the UI thread to complete (simple polling, could be improved with a semaphore or callback)
                    while (!Thread.currentThread().isInterrupted() && !proceed[0] && !Platform.isFxApplicationThread()) {
                        Thread.yield(); // Avoid busy-waiting, but this is a simple solution
                    }
                    if (!proceed[0]) return;
                }

                Commentaire commentaire = new Commentaire();
                commentaire.setContenu(commentText);
                commentaire.setUtilisateur(utilisateur);
                commentaire.setCreation_at(new java.sql.Timestamp(System.currentTimeMillis()));
                commentaire.setQuestion(currentQuestion);

                commentaireService.add(commentaire);
                Platform.runLater(() -> {
                    createCommentCard(commentaire);
                    commentInput.clear();
                    UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(userId);
                    if (change.isChanged()) {
                        showPrivilegeAlert(change);
                        updatePrivilegeUI(userId);
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Erreur", "Erreur lors de la vérification du contenu: " + e.getMessage()));
            }
        });
    }

    @FXML
    private void createCommentCard(Commentaire comment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/CommentCard.fxml"));
            VBox commentCard = loader.load();
            CommentCardController commentCardController = loader.getController();
            commentCardController.setCommentData(comment, this);
            commentCard.setUserData(commentCardController);
            Platform.runLater(() -> commentContainer.getChildren().add(commentCard));
        } catch (IOException e) {
            System.err.println("Error creating comment card: " + e.getMessage());
        }
    }

    public void handleUpvoteC(Commentaire commentaire, Label votesLabel, Button downvoteButton) {
        executorService.submit(() -> {
            try {
                String currentVote = commentaireService.getUserVote(commentaire.getCommentaire_id(), userId);
                if ("UP".equals(currentVote)) {
                    Platform.runLater(() -> showAlert("Erreur", "Vous avez déjà upvoté ce commentaire."));
                    return;
                }
                // Allow upvote if no vote or downvote exists
                commentaireService.upvoteComment(commentaire.getCommentaire_id(), userId);
                int updatedVotes = commentaireService.getVotes(commentaire.getCommentaire_id());
                commentaire.setVotes(updatedVotes);
                Platform.runLater(() -> {
                    votesLabel.setText("Votes: " + updatedVotes);
                    votesLabel.setVisible(true);
                    if (updatedVotes > 0) downvoteButton.setDisable(false);
                    UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(commentaire.getUtilisateur().getId());
                    if (change.isChanged() && userId == commentaire.getUtilisateur().getId()) {
                        showPrivilegeAlert(change);
                    }
                    updatePrivilegeUI(commentaire.getUtilisateur().getId());
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Erreur", "Erreur lors de l'upvote : " + e.getMessage()));
            }
        });
    }

    public void handleDownvoteC(Commentaire commentaire, Label votesLabel, Button downvoteButton) {
        executorService.submit(() -> {
            try {
                String currentVote = commentaireService.getUserVote(commentaire.getCommentaire_id(), userId);
                if ("DOWN".equals(currentVote)) {
                    Platform.runLater(() -> showAlert("Erreur", "Vous avez déjà downvoté ce commentaire."));
                    return;
                }
                // Allow downvote if no vote or upvote exists
                commentaireService.downvoteComment(commentaire.getCommentaire_id(), userId);
                int updatedVotes = commentaireService.getVotes(commentaire.getCommentaire_id());
                commentaire.setVotes(updatedVotes);
                Platform.runLater(() -> {
                    votesLabel.setText("Votes: " + updatedVotes);
                    votesLabel.setVisible(true);
                    downvoteButton.setDisable(updatedVotes == 0);
                    UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(commentaire.getUtilisateur().getId());
                    if (change.isChanged() && userId == commentaire.getUtilisateur().getId()) {
                        showPrivilegeAlert(change);
                    }
                    updatePrivilegeUI(commentaire.getUtilisateur().getId());
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Erreur", "Erreur lors du downvote : " + e.getMessage()));
            }
        });
    }

    public void deleteComment(Commentaire commentaire) {
        if (commentaire.getUtilisateur().getId() != userId) {
            showAlert("Erreur", "Vous ne pouvez supprimer que vos propres commentaires.");
            return;
        }
        executorService.submit(() -> {
            try {
                for (Node node : commentContainer.getChildren()) {
                    if (node instanceof VBox) {
                        CommentCardController controller = (CommentCardController) node.getUserData();
                        if (controller != null && controller.getCommentaire().getCommentaire_id() == commentaire.getCommentaire_id()) {
                            commentaireService.delete(commentaire);
                            Platform.runLater(() -> {
                                commentContainer.getChildren().remove(node);
                                commentContainer.requestLayout();
                                UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(userId);
                                if (change.isChanged()) {
                                    showPrivilegeAlert(change);
                                    updatePrivilegeUI(userId);
                                }
                            });
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error deleting comment: " + e.getMessage());
            }
        });
    }

    public void refreshQuestions() {
        loadComments();
    }

    private void showPrivilegeAlert(UtilisateurService.PrivilegeChange change) {
        if (!change.isChanged()) return;

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setHeaderText(null);
            String oldPrivilege = change.getOldPrivilege();
            String newPrivilege = change.getNewPrivilege();
            boolean isPromotion = getPrivilegeRank(newPrivilege) > getPrivilegeRank(oldPrivilege);

            alert.setTitle(isPromotion ? "Félicitations!" : "Mise à jour de privilège");
            alert.setContentText(isPromotion ?
                    (newPrivilege.equals("top_contributor") ? "Vous êtes passé de Regular à Top Contributor ! Bravo pour votre contribution !" :
                            "Vous êtes maintenant un Top Fan depuis " + oldPrivilege + " ! Votre passion est récompensée !") :
                    (oldPrivilege.equals("top_contributor") ? "Désolé, vous êtes redescendu de Top Contributor à Regular." :
                            "Désolé, vous êtes passé de Top Fan à " + newPrivilege + "."));

            ImageView icon = new ImageView(new Image(getClass().getResource(
                    isPromotion ? (newPrivilege.equals("top_contributor") ? "/forumUI/icons/silver_crown.png" : "/forumUI/icons/crown.png") :
                            "/forumUI/icons/alert.png").toExternalForm()));
            icon.setFitHeight(60);
            icon.setFitWidth(60);
            alert.setGraphic(icon);

            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResource(isPromotion ? "/forumUI/icons/sucessalert.png" : "/forumUI/icons/alert.png").toString()));

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
        });
    }

    private int getPrivilegeRank(String privilege) {
        return switch (privilege) {
            case "regular" -> 0;
            case "top_contributor" -> 1;
            case "top_fan" -> 2;
            default -> -1;
        };
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
        executorService.submit(() -> {
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
            Platform.runLater(() -> {
                for (Node node : commentContainer.getChildren()) {
                    if (node instanceof VBox) {
                        CommentCardController controller = (CommentCardController) node.getUserData();
                        if (controller != null && controller.getCommentaire().equals(commentaire)) {
                            controller.displayReactions();
                            controller.displayUserReaction();
                        }
                    }
                }
            });
        });
    }

    private boolean showProfanityWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(new Image(getClass().getResource("/forumUI/icons/alert.png").toExternalForm()));
        icon.setFitHeight(60);
        icon.setFitWidth(60);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType addAnywayButton = new ButtonType("Ajouter quand même", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton, addAnywayButton);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/alert.png").toString()));

        return alert.showAndWait()
                .filter(response -> response == addAnywayButton)
                .isPresent();
    }

    private void updatePrivilegeUI(int affectedUserId) {
        Platform.runLater(() -> {
            for (Node node : commentContainer.getChildren()) {
                if (node instanceof VBox) {
                    CommentCardController controller = (CommentCardController) node.getUserData();
                    if (controller != null && controller.getCommentaire().getUtilisateur().getId() == affectedUserId) {
                        Utilisateur user = us.getOne(affectedUserId);
                        if (user != null) {
                            controller.updatePrivilegeUI(user);
                        }
                    }
                }
            }
            commentContainer.requestLayout();
        });
    }
}