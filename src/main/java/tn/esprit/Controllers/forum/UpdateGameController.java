package tn.esprit.Controllers.forum;

import javafx.event.ActionEvent;
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

public class UpdateGameController extends AddGameController {

    private Games game;
    private AdminDashboardController dashboardController;

    @FXML
    private TextField gameNameField;
    @FXML
    private ComboBox<String> gameTypeComboBox;
    @FXML
    private ImageView gameImageView;
    @FXML
    private Button uploadImageButton;

    private static final String IMAGE_BASE_DIR = "C:\\xampp\\htdocs\\img\\games\\";

    public void setGame(Games game, AdminDashboardController dashboardController) {
        this.game = game;
        this.dashboardController = dashboardController;
        if (gameNameField == null || gameTypeComboBox == null || gameImageView == null) {
            System.err.println("One or more FXML elements are null in UpdateGameController. Check FXML injection.");
            showAlert("Erreur", "Failed to load game data. Check FXML configuration.");
            return;
        }
        loadGameData();
    }

    private void loadGameData() {
        gameNameField.setText(game.getGame_name());
        gameTypeComboBox.setValue(game.getGameType());

        if (game.getImagePath() != null && !game.getImagePath().isEmpty()) {
            // Construct the full path to load the existing image
            String fullImagePath = IMAGE_BASE_DIR + game.getImagePath();
            File file = new File(fullImagePath);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString(), 200, 150, true, true);
                gameImageView.setImage(image);
            } else {
                System.err.println("Existing game image not found at: " + fullImagePath);
                gameImageView.setImage(null);
            }
        }
    }

    @FXML
    private void handleUploadImage(ActionEvent event) {
        if (uploadImageButton == null || gameImageView == null) {
            System.err.println("uploadImageButton or gameImageView is null in UpdateGameController. Check FXML injection.");
            showAlert("Erreur", "Failed to handle image upload. Check FXML configuration.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Game Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Path destinationPath = Paths.get(IMAGE_BASE_DIR);
                if (!Files.exists(destinationPath)) {
                    Files.createDirectories(destinationPath);
                }

                String fileName = "game_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = destinationPath.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath);

                if (game.getImagePath() != null && !game.getImagePath().isEmpty()) {
                    File oldImageFile = new File(IMAGE_BASE_DIR + game.getImagePath());
                    if (oldImageFile.exists()) {
                        oldImageFile.delete();
                        System.out.println("Deleted old image: " + oldImageFile.getPath());
                    }
                }

                game.setImagePath(fileName);

                Image image = new Image(selectedFile.toURI().toString(), 200, 150, true, true);
                gameImageView.setImage(image);
                showSuccessAlert("Succès", "Image updated successfully!");
            } catch (IOException e) {
                showAlert("Erreur", "Failed to upload image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleUpdateGame(ActionEvent event) {
        if (gameNameField == null || gameTypeComboBox == null) {
            System.err.println("gameNameField or gameTypeComboBox is null in UpdateGameController. Check FXML injection.");
            showAlert("Erreur", "Failed to update game. Check FXML configuration.");
            return;
        }

        String gameName = gameNameField.getText().trim();
        String gameType = gameTypeComboBox.getValue();

        if (gameName.isEmpty() || gameType == null) {
            showAlert("Erreur", "Game name and type are required.");
            return;
        }

        game.setGame_name(gameName);
        game.setGameType(gameType);
        dashboardController.updateGame(game);
        showSuccessAlert("Succès", "Game updated successfully!");

        Stage stage = (Stage) gameNameField.getScene().getWindow();
        stage.close();
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