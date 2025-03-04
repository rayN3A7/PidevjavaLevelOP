package tn.esprit.Controllers.forum;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Models.Games;
import tn.esprit.Models.Role;
import tn.esprit.Services.GamesService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.util.List;

public class AdminDashboardController {

    @FXML private FlowPane gamesContainer;
    @FXML private BorderPane mainLayout;
    private GamesService gamesService = new GamesService();
    private AdminSidebarController sidebarController;

    @FXML
    public void initialize() {
        if (SessionManager.getInstance().getRole() == Role.ADMIN) {
            loadAdminSidebar();
        }
        loadGames();
    }

    private void loadAdminSidebar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/sidebarAdmin.fxml"));
            VBox adminSidebar = loader.load();
            sidebarController = loader.getController();
            mainLayout.setLeft(adminSidebar); // Dynamically set the sidebar
        } catch (IOException e) {
            System.err.println("Error loading admin sidebar: " + e.getMessage());
            showAlert("Erreur", "Failed to load admin sidebar: " + e.getMessage());
        }
    }

    public void loadGames() {
        new Thread(() -> {
            List<Games> games = gamesService.getAll();
            Platform.runLater(() -> {
                gamesContainer.getChildren().clear();
                gamesContainer.getChildren().add(new Label("Games List"));
                for (Games game : games) {
                    addGameCard(game);
                }
            });
        }).start();
    }

    private void addGameCard(Games game) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/GameCard.fxml"));
            VBox gameCard = loader.load();
            GameCardController cardController = loader.getController();
            cardController.setGameData(game, this);
            gamesContainer.getChildren().add(gameCard);
        } catch (IOException e) {
            System.err.println("Error loading game card: " + e.getMessage());
            showAlert("Erreur", "Failed to load game card: " + e.getMessage());
        }
    }

    @FXML
    public void showAddGameForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/AddGame.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Game");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadGames();
        } catch (IOException e) {
            showAlert("Erreur", "Failed to open Add Game form: " + e.getMessage());
        }
    }

    public void updateGame(Games game) {
        try {
            gamesService.update(game);
            loadGames();
        } catch (Exception e) {
        }
    }

    public void deleteGame(Games game) {
        try {
            gamesService.delete(game);
            loadGames();
            showAlert("Succès", "Game deleted successfully!");
        } catch (Exception e) {
            showAlert("Erreur", "Failed to delete game: " + e.getMessage());
        }
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