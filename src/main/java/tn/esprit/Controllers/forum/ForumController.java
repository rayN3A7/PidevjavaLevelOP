package tn.esprit.Controllers.forum;

import tn.esprit.Models.Question;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.QuestionService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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
                System.out.println("Filtered question: Title=" + question.getTitle() + ", Image Path=" + question.getImagePath());
                addQuestionCard(question);
            }
            questionCardContainer.requestLayout(); // Force layout update
            System.out.println("Layout updated for filtered question cards.");
        });
    }


    public void addQuestionCard(Question question) {
        try {
            System.out.println("Adding question card for: " + question.getTitle() + " with image path: " + question.getImagePath());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/QuestionCard.fxml"));
            Parent questionCard = loader.load();

            QuestionCardController cardController = loader.getController();
            cardController.setQuestionData(question, this);

            Platform.runLater(() -> {
                questionCardContainer.getChildren().add(questionCard);
                questionCardContainer.requestLayout(); // Force layout update to ensure image loads
                System.out.println("Question card added and layout updated for: " + question.getTitle());
            });
        } catch (Exception e) {
            System.err.println("Error adding question card: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // New public method to force UI refresh from other controllers
    public void forceRefreshUI() {
        Platform.runLater(() -> {
            questionCardContainer.requestLayout();
            System.out.println("Forced UI refresh for question cards.");
        });
    }

    public void handleUpvote(Question question, Label votesLabel, Button downvoteButton) {
        questionService.upvoteQuestion(question.getQuestion_id());

        int updatedVotes = questionService.getVotes(question.getQuestion_id());
        question.setVotes(updatedVotes);

        Platform.runLater(() -> {
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
                votesLabel.setText("Votes: " + updatedVotes);
                downvoteButton.setDisable(updatedVotes == 0);
            });
        }
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
        refreshQuestions();
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

        // Check if the user has already reacted to this question
        String existingReaction = questionService.getUserReaction(question.getQuestion_id(), userId);
        if (existingReaction != null) {
            // If the user has reacted, remove the existing reaction
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

        // Add the new reaction
        questionService.addReaction(question.getQuestion_id(), userId, emojiUrl);
        // Update the question's reactions and user reaction
        Map<String, Integer> updatedReactions = questionService.getReactions(question.getQuestion_id());
        question.setReactions(updatedReactions);
        question.setUserReaction(emojiUrl); // Set the user's specific reaction (image URL)
    }
}