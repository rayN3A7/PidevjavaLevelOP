package tn.esprit.Controllers.Coach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceSession;

import java.util.List;

public class GameSearchSessionController {

    @FXML private TextField gameField;
    @FXML private Label sessionDetailsLabel;
    @FXML private Button searchButton;
    @FXML private VBox sessionsContainer;

    private final ServiceSession serviceSession = new ServiceSession();

    @FXML
    public void initialize() {
        gameField.setPromptText("Entrez le nom du jeu");
        searchButton.setOnAction(event -> searchSessionByGame());
    }

    private void searchSessionByGame() {
        String game = gameField.getText().trim();
        if (game.isEmpty()) {
            sessionDetailsLabel.setText("Veuillez entrer un nom de jeu.");
            return;
        }

        List<Session_game> sessions = serviceSession.getAll();
        sessionsContainer.getChildren().clear();
        boolean found = false;

        for (Session_game session : sessions) {
            if (session.getGame().toLowerCase().contains(game.toLowerCase())) {
                found = true;

                VBox sessionCard = new VBox(10);
                Label gameLabel = new Label("Jeu: " + session.getGame());
                Label priceLabel = new Label("Prix: " + session.getprix() + " DT");
                Label durationLabel = new Label("Durée: " + session.getduree_session());
                Button checkAvailabilityButton = new Button("Voir disponibilité");

                checkAvailabilityButton.setStyle("-fx-background-color: #0585e6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 8 15; " +
                        "-fx-background-radius: 20; ");

                checkAvailabilityButton.setOnAction(event -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/verifier_reservation.fxml"));
                        Parent root = loader.load();
                        Scene scene = new Scene(root);
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                sessionCard.getChildren().addAll(gameLabel, priceLabel, durationLabel, checkAvailabilityButton);
                sessionsContainer.getChildren().add(sessionCard);
            }
        }

        if (!found) {
            sessionDetailsLabel.setText("Aucune session trouvée pour ce jeu.");
        }
    }

    @FXML
    private void backToSearch(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/search_session.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}