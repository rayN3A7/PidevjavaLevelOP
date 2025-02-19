package tn.esprit.Controllers.forum;

import tn.esprit.Models.Question;
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

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ForumController implements Initializable {
    @FXML
    private VBox questionCardContainer;

    private final QuestionService questionService = new QuestionService();
    @FXML
    private Button addQuestionButton;
    @FXML
    private TextField searchField;
    private NavbarController navbarController; //

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
            filterQuestionsByGameName(gameName); // Trigger search when Enter is pressed
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

    public void filterQuestionsByGameName(String gameName) {
        System.out.println("Filtering questions for game: " + gameName);
        List<Question> filteredQuestions = questionService.getQuestionsByGameName(gameName);

        Platform.runLater(() -> {
            questionCardContainer.getChildren().clear();
            for (Question question : filteredQuestions) {
                addQuestionCard(question);
            }
        });
    }


    public void addQuestionCard(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/QuestionCard.fxml"));
            Parent questionCard = loader.load();

            QuestionCardController cardController = loader.getController();
            cardController.setQuestionData(question, this);

            questionCardContainer.getChildren().add(questionCard);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void refreshQuestions() {
        loadQuestions();
    }
    @FXML
    private void navigateToAddQuestion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/QuestionForm.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) questionCardContainer.getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight()); // Use the same width/height as the original window
            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
