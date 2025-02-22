package tn.esprit.Controllers.forum;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Platform;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
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
    private Button uploadImageButton;
    @FXML
    private ImageView uploadedImageView;
    @FXML
    private ComboBox<String> gameComboBox;

    private UtilisateurService us = new UtilisateurService();
    private int userId = SessionManager.getInstance().getUserId();
    private GamesService gamesService = new GamesService();
    private QuestionService questionService = new QuestionService();
    private String lastImagePath;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("AddQuestionController initialized");
        loadGames();
    }

    private void loadGames() {
        List<Games> gamesList = gamesService.getAll();
        gameComboBox.getItems().setAll(
                gamesList.stream().map(Games::getGame_name).toList()
        );
    }

    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Question Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) uploadImageButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Define the destination directory
                String destinationDir = "C:\\xampp\\htdocs\\img";
                Path destinationPath = Paths.get(destinationDir);

                // Create directory if it doesn’t exist
                if (!Files.exists(destinationPath)) {
                    Files.createDirectories(destinationPath);
                }

                // Generate a unique filename (e.g., question_<timestamp>_<original_name>)
                String fileName = "question_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = destinationPath.resolve(fileName);

                // Copy the file to the destination
                Files.copy(selectedFile.toPath(), targetPath);

                // Store the absolute file path for database and loading
                lastImagePath = targetPath.toString();

                // Display the uploaded image immediately in uploadedImageView
                Image image = new Image(selectedFile.toURI().toString(), 200, 150, true, true);
                if (!image.isError()) {
                    uploadedImageView.setImage(image);
                    showSuccessAlert("Succès", "Image uploaded successfully to: " + lastImagePath);
                } else {
                    showAlert("Erreur", "Failed to load image preview: " + image.getException().getMessage());
                }
            } catch (IOException e) {
                showAlert("Erreur", "Failed to upload image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Rest of the methods remain unchanged
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

        Utilisateur utilisateur = us.getOne(userId);
        if (utilisateur == null) {
            showAlert("Erreur", "Utilisateur non trouvé pour ID: " + userId);
            return;
        }

        String imagePath = lastImagePath != null ? lastImagePath : null;
        Question question = new Question(title, content, selectedGameObj, utilisateur, 0, new Timestamp(System.currentTimeMillis()), imagePath);
        System.out.println("Creating Question: Title=" + question.getTitle() + ", Content=" + question.getContent() + ", Game ID=" + question.getGame().getGame_id() + ", User ID=" + question.getUser().getId() + ", Image Path=" + question.getImagePath());

        try {
            questionService.add(question);
            showSuccessAlert("Succès", "Question ajoutée avec succès !");
            clearForm();
            navigateToForumPage(question);
        } catch (RuntimeException e) {
            showAlert("Erreur", "Failed to add question: " + e.getMessage());
            System.err.println("Detailed error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToForumPage(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/Forum.fxml"));
            Parent root = loader.load();

            ForumController forumController = loader.getController();
            forumController.refreshQuestions();

            Stage stage = (Stage) submitButton.getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.show();

            Platform.runLater(() -> {
                forumController.loadQuestions();
                forumController.forceRefreshUI();
                System.out.println("Forced UI update after adding question: " + question.getTitle() + " with image: " + question.getImagePath());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        titleField.clear();
        contentField.clear();
        gameComboBox.setValue(null);
        lastImagePath = null;
        uploadedImageView.setImage(null);
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
}