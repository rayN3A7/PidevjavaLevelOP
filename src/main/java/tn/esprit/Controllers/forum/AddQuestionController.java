package tn.esprit.Controllers.forum;

import tn.esprit.Models.Games;
import tn.esprit.Models.Question;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.GamesService;
import tn.esprit.Services.QuestionService;
import javafx.event.ActionEvent;
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
import tn.esprit.Models.Role;

import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class AddQuestionController implements Initializable {
    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentField;
    @FXML
    private Button submitButton;
    @FXML
    private VBox questionCardContainer;
    @FXML
    private ComboBox<String> gameComboBox; // Dropdown for selecting games

    private GamesService gamesService = new GamesService();
    private QuestionService questionService = new QuestionService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("AddQuestionController initialized");
        loadGames();

    }

    private void loadGames() {

            List<Games> gamesList = gamesService.getAll();
        gameComboBox.getItems().setAll(
                gamesService.getAll().stream().map(Games::getGame_name).toList()
        );

    }

    @FXML
    private void handleSubmit(ActionEvent event) {
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

            Utilisateur user = new Utilisateur(2, "yami", "sellami", "hsouna@gmail.com", "Yamimato", 1256969, "hsouna@1235", Role.COACH);
            Question question = new Question(title, content, selectedGameObj, user, 0, new Timestamp(System.currentTimeMillis()));

            // Debug print
            System.out.println("Creating Question: " + question.getTitle() + " | " + question.getContent());

            // Add question to the database
            questionService.add(question);

        showSuccessAlert("Succès", "Question ajoutée avec succès !");
            clearForm();

            // Refresh forum page
            navigateToForumPage(question);

        }



    private void navigateToForumPage(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/forumUI/Forum.fxml"));
            Parent root = loader.load();

            ForumController forumController = loader.getController();
            forumController.refreshQuestions(); // Ensure the questions are refreshed

            // Get the current stage (main window)
            Stage stage = (Stage) submitButton.getScene().getWindow();

            // Set the new scene to the stage
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        titleField.clear();
        contentField.clear();
        gameComboBox.setValue(null);
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
    private void showSuccessAlert(String title, String message) {
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
        /*private Utilisateur getCurrentUser() {
        int userId = SessionManager.getInstance().getUserId();
        if (userId == -1) {
            return null;
        }
        UtilisateurService utilisateurService = new UtilisateurService();
        return utilisateurService.getOne(userId);
    }*/

}
