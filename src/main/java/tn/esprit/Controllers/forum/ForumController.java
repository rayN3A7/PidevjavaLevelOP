package tn.esprit.Controllers.forum;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Models.Question;
import tn.esprit.Services.QuestionService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;


public class ForumController implements Initializable {
    @FXML
    private VBox questionCardContainer; // Remains private

    private final QuestionService questionService = new QuestionService();
    @FXML
    private Button addQuestionButton;
    @FXML
    private TextField searchField;
    private NavbarController navbarController;
    private UtilisateurService us = new UtilisateurService();
    private int userId = SessionManager.getInstance().getUserId();
    static final Map<String, javafx.scene.image.Image> imageCache = new HashMap<>(); // Image cache

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadQuestions();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterQuestionsByGameName(newValue);
        });

        searchField.setOnKeyPressed(this::handleSearchEnter);
    }

    private void handleSearchEnter(KeyEvent event) {
        if (event.getCode().toString().equals("ENTER")) {
            String gameName = searchField.getText().trim();
            filterQuestionsByGameName(gameName);
        }
    }

    public void loadQuestions() {
        List<Question> questions = questionService.getAll();
        System.out.println("Questions loaded: " + questions.size());

        questionCardContainer.getChildren().clear();
        for (Question question : questions) {
            if (question != null) {
                System.out.println("Adding question: " + question.getTitle() + " | " + question.getContent());
                addQuestionCard(question);
            } else {
                System.out.println("Found null question in database!");
            }
        }
    }

    public void refreshQuestions() {
        loadQuestions();
    }

    public void filterQuestionsByGameName(String gameName) {
        System.out.println("Filtering questions for game: " + gameName);
        List<Question> filteredQuestions = questionService.getQuestionsByGameName(gameName);

        Platform.runLater(() -> {
            questionCardContainer.getChildren().clear();
            for (Question question : filteredQuestions) {
                System.out.println("Filtered question: Title=" + question.getTitle() + ", Image Path=" + question.getMediaPath());
                addQuestionCard(question);
            }
            questionCardContainer.requestLayout(); // Force layout update
            System.out.println("Layout updated for filtered question cards.");
        });
    }


    public void addQuestionCard(Question question) {
        try {
            System.out.println("Adding question card for: " + question.getTitle() + " with image path: " + question.getMediaPath());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/QuestionCard.fxml"));
            Parent questionCard = loader.load();

            QuestionCardController cardController = loader.getController();
            cardController.setQuestionData(question, this);

            Platform.runLater(() -> {
                questionCardContainer.getChildren().add(questionCard);
                questionCardContainer.requestLayout(); // Force layout update to ensure image loads
                System.out.println("Question card added and layout updated for: " + question.getTitle());
            });
            UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(userId);
            if (change.isChanged()) {
                showPrivilegeAlert(change);
                refreshQuestions();
            }
        } catch (Exception e) {
            System.err.println("Error adding question card: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public void handleUpvote(Question question, Label votesLabel, Button downvoteButton) {
        questionService.upvoteQuestion(question.getQuestion_id());
        int updatedVotes = questionService.getVotes(question.getQuestion_id());
        question.setVotes(updatedVotes);



        Platform.runLater(() -> {
            UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(question.getUser().getId());
            if (change.isChanged()) {
                showPrivilegeAlert(change);
                refreshQuestions();
            }
            votesLabel.setText("Votes: " + updatedVotes);
            if (updatedVotes > 0) {
                downvoteButton.setDisable(false);
            }
        });
    }

    public void handleDownvote(Question question, Label votesLabel, Button downvoteButton) {
        if (question.getVotes() > 0) {
            questionService.downvoteQuestion(question.getQuestion_id());
            int updatedVotes = questionService.getVotes(question.getQuestion_id());
            question.setVotes(updatedVotes);

            Platform.runLater(() -> {
                UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(question.getUser().getId());
                if (change.isChanged()) {
                    showPrivilegeAlert(change);
                    refreshQuestions();
                }
                votesLabel.setText("Votes: " + updatedVotes);
                downvoteButton.setDisable(updatedVotes == 0);
            });
        }
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
    public void updateQuestion(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/UpdateQuestionForm.fxml"));
            Parent root = loader.load();

            UpdateQuestionController updateController = loader.getController();
            updateController.loadQuestionData(question);

            Stage stage = (Stage) questionCardContainer.getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteQuestion(Question question) {
        questionService.delete(question);
        System.out.println("Deleted question: " + question.getTitle());
        UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(question.getUser().getId());
        if (change.isChanged()) {
            showPrivilegeAlert(change);
            refreshQuestions();
        }
    }

    @FXML
    private void navigateToAddQuestion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/QuestionForm.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) questionCardContainer.getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleReaction(Question question, String emojiUrl) {
        int userId = SessionManager.getInstance().getUserId(); // Use actual user ID from session
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
        question.setUserReaction(emojiUrl); // Set the user's specific reaction (image URL)
    }
}