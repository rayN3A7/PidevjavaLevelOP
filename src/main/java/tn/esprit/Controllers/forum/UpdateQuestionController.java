package tn.esprit.Controllers.forum;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.Models.Games;
import tn.esprit.Models.Question;
import tn.esprit.Services.GamesService;
import tn.esprit.Services.QuestionService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UpdateQuestionController implements Initializable {
    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentField;
    @FXML
    private Button updateButton;
    @FXML
    private ComboBox<String> gameComboBox;

    private GamesService gamesService = new GamesService();
    private QuestionService questionService = new QuestionService();

    private Question currentQuestion;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadGames();
    }

    public void loadQuestionData(Question question) {
        currentQuestion = question;
        titleField.setText(question.getTitle());
        contentField.setText(question.getContent());

        try {
            if (gameComboBox != null) {
                gameComboBox.setValue(question.getGame().getGame_name());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGames() {

            List<Games> gamesList = gamesService.getAll();
            gameComboBox.getItems().clear(); // Ensure no duplication
            for (Games game : gamesList) {
                gameComboBox.getItems().add(game.getGame_name());
            }

    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        String title = titleField.getText().trim();
        String content = contentField.getText().trim();
        String selectedGame = gameComboBox.getValue();

        if (title.isEmpty() || content.isEmpty() || selectedGame == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }


            Games selectedGameObj = gamesService.getByName(selectedGame);
            if (selectedGameObj == null) {
                showAlert("Erreur", "Le jeu sélectionné n'existe pas.");
                return;
            }

            currentQuestion.setTitle(title);
            currentQuestion.setContent(content);
            currentQuestion.setGame(selectedGameObj);

            questionService.update(currentQuestion);

            showAlert("Succès", "Question mise à jour avec succès !");
            navigateToForumPage();
        }


    private void navigateToForumPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/Forum.fxml"));
            Parent root = loader.load();

            ForumController forumController = loader.getController();

            Stage stage = (Stage) updateButton.getScene().getWindow();

            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(new Image(getClass().getResource("/forumUI/icons/sucessalert.png").toExternalForm()));
        icon.setFitHeight(60);
        icon.setFitWidth(80);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/sucessalert.png").toString()));

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        alert.showAndWait();
    }
}
