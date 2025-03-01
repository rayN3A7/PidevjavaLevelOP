package tn.esprit.Controllers.forum;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Models.Question;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.QuestionService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ForumController implements Initializable {
    @FXML private VBox questionCardContainer;
    private final QuestionService questionService = new QuestionService();
    @FXML private Button addQuestionButton;
    @FXML private TextField searchField;
    private UtilisateurService us = new UtilisateurService();
    private int userId = SessionManager.getInstance().getUserId();
    private static final Map<String, Image> imageCache = new HashMap<>();
    private Map<Question, Parent> questionCardMap; // Cache question cards
    private List<Question> allQuestions;
    private PauseTransition debounceTimer; // For debouncing search input

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        questionCardMap = new HashMap<>();
        debounceTimer = new PauseTransition(Duration.millis(300));
        loadQuestionsLazy();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            debounceTimer.setOnFinished(event -> filterQuestionsRealTime(newValue));
            debounceTimer.playFromStart();
        });

        questionCardContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null && newScene == null) {
                QuestionCardController.stopAllVideos();
            }
        });
    }

    public void loadQuestionsLazy() {
        new Thread(() -> {
            allQuestions = questionService.getAll();
            Platform.runLater(() -> {
                questionCardContainer.getChildren().clear();
                questionCardMap.clear();
                for (Question question : allQuestions) {
                    addQuestionCard(question);
                }
                filterQuestionsRealTime(""); // Initial display with no filter
            });
        }).start();
    }

    private void filterQuestionsRealTime(String searchText) {
        String query = searchText.trim().toLowerCase();
        Platform.runLater(() -> {
            int visibleCount = 0;
            for (Map.Entry<Question, Parent> entry : questionCardMap.entrySet()) {
                Question question = entry.getKey();
                Parent card = entry.getValue();
                String gameName = question.getGame().getGame_name() != null ? question.getGame().getGame_name().toLowerCase() : "";
                String gameType = question.getGame().getGameType() != null ? question.getGame().getGameType().toLowerCase() : "";
                boolean matches = query.isEmpty() || gameName.contains(query) || gameType.contains(query);
                card.setVisible(matches);
                card.setManaged(matches);
                if (matches) visibleCount++;
            }
            questionCardContainer.requestLayout();
        });
    }

    public void refreshQuestions() {
        QuestionCardController.stopAllVideos();
        loadQuestionsLazy();
    }

    private void addQuestionCard(Question question) {
        if (question == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/QuestionCard.fxml"));
            Parent questionCard = loader.load();
            QuestionCardController cardController = loader.getController();
            cardController.setQuestionData(question, this);
            questionCard.setUserData(cardController);
            questionCardMap.put(question, questionCard);
            Platform.runLater(() -> {
                questionCardContainer.getChildren().add(questionCard);
                questionCard.setVisible(false); // Initially hidden for lazy loading
                filterQuestionsRealTime(searchField.getText().trim().toLowerCase());
            });
        } catch (IOException e) {
            System.err.println("Error adding question card: " + e.getMessage());
        }
    }

    public void handleUpvote(Question question, Label votesLabel, Button downvoteButton) {
        new Thread(() -> {
            try {
                String currentVote = questionService.getUserVote(question.getQuestion_id(), userId);
                if ("UP".equals(currentVote)) {
                    Platform.runLater(() -> showAlert("Erreur", "Vous avez déjà upvoté cette question."));
                    return;
                }
                // Allow upvote if no vote or downvote exists
                questionService.upvoteQuestion(question.getQuestion_id(), userId);
                int updatedVotes = questionService.getVotes(question.getQuestion_id());
                question.setVotes(updatedVotes);
                Platform.runLater(() -> {
                    votesLabel.setText("Votes: " + updatedVotes);
                    if (updatedVotes > 0) downvoteButton.setDisable(false);
                    UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(question.getUser().getId());
                    if (change.isChanged() && userId == question.getUser().getId()) {
                        showPrivilegeAlert(change);
                    }
                    updatePrivilegeUI(question.getUser().getId());
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Erreur", "Erreur lors de l'upvote : " + e.getMessage()));
            }
        }).start();
    }

    public void handleDownvote(Question question, Label votesLabel, Button downvoteButton) {
        if (question.getVotes() <= 0) return;
        new Thread(() -> {
            try {
                String currentVote = questionService.getUserVote(question.getQuestion_id(), userId);
                if ("DOWN".equals(currentVote)) {
                    Platform.runLater(() -> showAlert("Erreur", "Vous avez déjà downvoté cette question."));
                    return;
                }
                // Allow downvote if no vote or upvote exists
                questionService.downvoteQuestion(question.getQuestion_id(), userId);
                int updatedVotes = questionService.getVotes(question.getQuestion_id());
                question.setVotes(updatedVotes);
                Platform.runLater(() -> {
                    votesLabel.setText("Votes: " + updatedVotes);
                    downvoteButton.setDisable(updatedVotes == 0);
                    UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(question.getUser().getId());
                    if (change.isChanged() && userId == question.getUser().getId()) {
                        showPrivilegeAlert(change);
                    }
                    updatePrivilegeUI(question.getUser().getId());
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Erreur", "Erreur lors du downvote : " + e.getMessage()));
            }
        }).start();
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

    public void updateQuestion(Question question) {
        if (question.getUser().getId() != userId) {
            showAlert("Erreur", "Vous ne pouvez modifier que vos propres questions.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/UpdateQuestionForm.fxml"));
            Parent root = loader.load();
            UpdateQuestionController updateController = loader.getController();
            updateController.loadQuestionData(question);
            Stage stage = (Stage) questionCardContainer.getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteQuestion(Question question) {
        if (question.getUser().getId() != userId) {
            showAlert("Erreur", "Vous ne pouvez supprimer que vos propres questions.");
            return;
        }
        try {
            Parent questionCard = questionCardMap.get(question);
            if (questionCard != null) {
                questionService.delete(question);
                Platform.runLater(() -> {
                    questionCardContainer.getChildren().remove(questionCard);
                    questionCardMap.remove(question);
                    allQuestions.remove(question);
                    questionCardContainer.requestLayout();
                });
                UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(userId);
                if (change.isChanged()) {
                    showPrivilegeAlert(change);
                }
                updatePrivilegeUI(userId);
            }
        } catch (Exception e) {
            System.err.println("Error deleting question: " + e.getMessage());
        }
    }

    private void updatePrivilegeUI(int affectedUserId) {
        Platform.runLater(() -> {
            for (Node node : questionCardContainer.getChildren()) {
                if (node instanceof Parent) {
                    QuestionCardController controller = (QuestionCardController) node.getUserData();
                    if (controller != null && controller.getQuestion().getUser().getId() == affectedUserId) {
                        Utilisateur user = us.getOne(affectedUserId);
                        if (user != null) {
                            controller.updatePrivilegeUI(user);
                        }
                    }
                }
            }
            questionCardContainer.requestLayout();
        });
    }

    @FXML
    private void navigateToAddQuestion() {
        QuestionCardController.stopAllVideos();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/QuestionForm.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) questionCardContainer.getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleReaction(Question question, String emojiUrl) {
        new Thread(() -> {
            int userId = SessionManager.getInstance().getUserId();
            QuestionService questionService = new QuestionService();

            String existingReaction = questionService.getUserReaction(question.getQuestion_id(), userId);
            if (existingReaction != null) {
                questionService.removeReaction(question.getQuestion_id(), userId);
                question.getReactions().remove(existingReaction);
                if (question.getReactions().containsKey(existingReaction)) {
                    int currentCount = question.getReactions().get(existingReaction);
                    if (currentCount > 1) {
                        question.getReactions().put(existingReaction, currentCount - 1);
                    } else {
                        question.getReactions().remove(existingReaction);
                    }
                }
            }

            questionService.addReaction(question.getQuestion_id(), userId, emojiUrl);
            Map<String, Integer> updatedReactions = questionService.getReactions(question.getQuestion_id());
            question.setReactions(updatedReactions);
            question.setUserReaction(emojiUrl);
            Platform.runLater(() -> {
                for (Node node : questionCardContainer.getChildren()) {
                    if (node instanceof Parent) {
                        QuestionCardController controller = (QuestionCardController) node.getUserData();
                        if (controller != null && controller.getQuestion().equals(question)) {
                            controller.displayReactions();
                            controller.displayUserReaction();
                        }
                    }
                }
            });
        }).start();
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
}