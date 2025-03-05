package tn.esprit.Controllers.forum;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.Models.Games;
import tn.esprit.Services.GamesService;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class GameCardController {

    @FXML private ImageView gameImage;
    @FXML private Label gameNameLabel;
    @FXML private Label gameTypeLabel;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;

    private Games game;
    private AdminDashboardController dashboardController;
    private GamesService gamesService = new GamesService();

    // Define the base directory where images are stored
    private static final String IMAGE_BASE_DIR = "C:\\xampp\\htdocs\\img\\games\\";

    public void setGameData(Games game, AdminDashboardController dashboardController) {
        this.game = game;
        this.dashboardController = dashboardController;

        if (gameNameLabel == null || gameTypeLabel == null || updateButton == null || deleteButton == null || (gameImage == null && game.getImagePath() != null)) {
            System.err.println("One or more FXML elements are null in GameCardController. Check FXML injection.");
            return;
        }

        gameNameLabel.setText(game.getGame_name());
        gameTypeLabel.setText("Type: " + game.getGameType());

        if (gameImage != null) {
            if (game.getImagePath() != null && !game.getImagePath().isEmpty()) {
                // Construct the full path by combining the base directory and file name
                String fullImagePath = IMAGE_BASE_DIR + game.getImagePath();
                File file = new File(fullImagePath);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString(), 250, 150, true, true);
                    gameImage.setImage(image);
                } else {
                    System.err.println("Game image file not found at: " + fullImagePath);
                    gameImage.setImage(null); // Optionally set a default "not found" image
                }
            } else {
                gameImage.setImage(null);
            }
        }

        updateButton.setOnAction(e -> handleUpdate());
        deleteButton.setOnAction(e -> handleDelete());
    }

    @FXML
    private void handleUpdate() {
        if (game == null || dashboardController == null) {
            System.err.println("Game or dashboard controller is null in handleUpdate. Check state.");
            showAlert("Erreur", "Failed to update game: Invalid state.");
            return;
        }

        try {
            String resourcePath = "/forumUI/UpdateGame.fxml";
            URL fxmlUrl = getClass().getResource(resourcePath);
            if (fxmlUrl == null) {
                throw new IOException("FXML resource not found: " + resourcePath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            UpdateGameController updateController = loader.getController();
            updateController.setGame(game, dashboardController);

            Stage stage = new Stage();
            stage.setTitle("Update Game");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            dashboardController.loadGames(); // Refresh the dashboard after updating
        } catch (IOException e) {
            System.err.println("Error opening update form: " + e.getMessage());
            showAlert("Erreur", "Failed to open update form: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (game == null || dashboardController == null) {
            System.err.println("Game or dashboard controller is null in handleDelete. Check state.");
            showAlert("Erreur", "Failed to delete game: Invalid state.");
            return;
        }
        dashboardController.deleteGame(game);
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
}