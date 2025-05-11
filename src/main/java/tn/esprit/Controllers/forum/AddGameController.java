package tn.esprit.Controllers.forum;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.Models.Games;
import tn.esprit.Services.GamesService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AddGameController {

    private GamesService gamesService = new GamesService();

    @FXML
    private TextField gameNameField;
    @FXML
    private ComboBox<String> gameTypeComboBox;
    @FXML
    private ImageView gameImageView;
    @FXML
    private Button uploadImageButton;

    private String imageFileName;

    @FXML
    public void initialize() {
        if (gameTypeComboBox != null) {
            gameTypeComboBox.getItems().addAll("FPS", "Hero Shooter", "Third Person Shooter", "Sports", "Other");
            gameTypeComboBox.setValue("Other");
        } else {
            System.err.println("gameTypeComboBox is null in AddGameController. Check FXML injection.");
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Game Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String destinationDir = "C:\\xampp\\htdocs\\img\\games";
                Path destinationPath = Paths.get(destinationDir);
                if (!Files.exists(destinationPath)) {
                    Files.createDirectories(destinationPath);
                }

                String fileName = selectedFile.getName();
                Path targetPath = destinationPath.resolve(fileName);

                if (Files.exists(targetPath)) {
                    showAlert("Warning", "A file with this name already exists. It will be overwritten.");
                }

                Files.copy(selectedFile.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                imageFileName = fileName;

                Image image = new Image(selectedFile.toURI().toString(), 200, 150, true, true);
                gameImageView.setImage(image);
                showSuccessAlert("Succès", "Image uploaded successfully!");
            } catch (IOException e) {
                showAlert("Erreur", "Failed to upload image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void handleAddGame() {
        String gameName = gameNameField.getText().trim();
        String gameType = gameTypeComboBox.getValue();

        if (gameName.isEmpty() || gameType == null) {
            showAlert("Erreur", "Game name and type are required.");
            return;
        }

        Games newGame = new Games(gameName, imageFileName, gameType);
        gamesService.add(newGame);
        showSuccessAlert("Succès", "Game added successfully!");
        clearForm();
    }

    private void clearForm() {
        gameNameField.clear();
        if (gameTypeComboBox != null) {
            gameTypeComboBox.setValue("Other");
        }
        gameImageView.setImage(null);
        imageFileName = null; // Reset to null
    }

    // showAlert and showSuccessAlert methods remain unchanged
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