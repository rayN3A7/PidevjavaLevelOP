package tn.esprit.Controllers.forum;

import javafx.scene.control.Button;
import tn.esprit.Models.Games;
import tn.esprit.Services.GamesService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

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

    private String imagePath;

    @FXML
    public void initialize() {
        // Populate game type options
        gameTypeComboBox.getItems().addAll("FPS", "Hero Shooter", "Third Person Shooter", "Sports", "Other");
        gameTypeComboBox.setValue("Other"); // Default value
    }

    @FXML
    private void handleUploadImage(ActionEvent event) {
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

                String fileName = "game_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = destinationPath.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath);

                imagePath = targetPath.toString();
                Image image = new Image(selectedFile.toURI().toString(), 200, 150, true, true);
                gameImageView.setImage(image);
                showAlert("Succès", "Image uploaded successfully!");
            } catch (IOException e) {
                showAlert("Erreur", "Failed to upload image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAddGame(ActionEvent event) {
        String gameName = gameNameField.getText().trim();
        String gameType = gameTypeComboBox.getValue();

        if (gameName.isEmpty() || gameType == null) {
            showAlert("Erreur", "Game name and type are required.");
            return;
        }

        Games newGame = new Games(gameName, imagePath, gameType);
        gamesService.add(newGame);
        showAlert("Succès", "Game added successfully!");
        clearForm();
    }

    private void clearForm() {
        gameNameField.clear();
        gameTypeComboBox.setValue("Other");
        gameImageView.setImage(null);
        imagePath = null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}