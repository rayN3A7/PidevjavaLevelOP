package tn.esprit.Controllers.forum;

import tn.esprit.Models.Question;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class QuestionCardController {
    @FXML
    private Label commentAuthor;
    @FXML
    private Label titleLabel;
    @FXML
    private Label contentLabel;
    @FXML
    private Label votesLabel;
    @FXML
    private Button upvoteButton;
    @FXML
    private Button downvoteButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private ImageView gameIcon;

    private Question question;
    private ForumController forumController;

    public void setQuestionData(Question question, ForumController forumController) {
        this.question = question;
        this.forumController = forumController;
        commentAuthor.setText(question.getUser().getNickname());
        titleLabel.setText(question.getTitle());
        titleLabel.setOnMouseClicked(event -> openQuestionDetails(question));
        titleLabel.setCursor(Cursor.HAND);

        contentLabel.setText(question.getContent());
        votesLabel.setText("Votes: " + question.getVotes());

        upvoteButton.setOnAction(e -> forumController.handleUpvote(question, votesLabel, downvoteButton));
        downvoteButton.setOnAction(e -> forumController.handleDownvote(question, votesLabel, downvoteButton));

        // Disable downvote button if votes are 0
        downvoteButton.setDisable(question.getVotes() == 0);

        updateButton.setOnAction(e -> forumController.updateQuestion(question));
        deleteButton.setOnAction(e -> forumController.deleteQuestion(question));

        // Set game icon
        setGameIcon(question.getGame().getGame_name());
    }

    private void setGameIcon(String gameName) {
        if (gameName != null && !gameName.isEmpty()) {
            String formattedGameName = gameName.toLowerCase().replace(" ", "_");
            String filePath = "/forumUI/icons/" + formattedGameName + ".png";

            System.out.println("Looking for game icon at: " + filePath); // Debugging line

            try {
                Image image = new Image(getClass().getResourceAsStream(filePath));
                if (image.isError()) {
                    throw new Exception("Image load error");
                }
                gameIcon.setImage(image);

                // Custom size per game (adjust as needed)
                switch (formattedGameName) {
                    case "valorant":
                        gameIcon.setFitWidth(100);
                        gameIcon.setFitHeight(80);
                        break;
                    case "overwatch":
                        gameIcon.setFitWidth(100);
                        gameIcon.setFitHeight(100);
                        break;
                    case "league_of_legends":  // Ensure this matches the expected file name
                        gameIcon.setFitWidth(100);
                        gameIcon.setFitHeight(100);
                        break;
                    default:
                        gameIcon.setFitWidth(100); // Default size
                        gameIcon.setFitHeight(100);
                        break;
                }
            } catch (Exception e) {
                System.out.println("Game icon not found for: " + gameName + " at path: " + filePath);
            }
        }
    }

    private void openQuestionDetails(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/QuestionDetails.fxml"));
            Parent root = loader.load();

            QuestionDetailsController controller = loader.getController();
            controller.loadQuestionDetails(question);

            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
