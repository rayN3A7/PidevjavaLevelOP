package tn.esprit.Controllers.Coach;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceReservation;
import tn.esprit.Services.ServiceSession;

public class VerifierReservationController {

    @FXML private VBox sessionsContainer;

    private final ServiceSession serviceSession = new ServiceSession();
    private final ServiceReservation serviceReservation = new ServiceReservation();

    @FXML
    public void initialize() {
        showAllSessions();
    }

    private void showAllSessions() {
        List<Session_game> sessions = serviceSession.getAll();
        sessionsContainer.getChildren().clear();

        for (Session_game session : sessions) {
            // Créer une carte pour chaque session
            VBox sessionCard = new VBox(10);
            sessionCard.setStyle("-fx-background-color: #162942; " +
                    "-fx-padding: 15; " +
                    "-fx-background-radius: 8; " +
                    "-fx-margin: 5;");

            // Informations de la session
            Label gameLabel = new Label("Jeu: " + session.getGame());
            gameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label priceLabel = new Label("Prix: " + session.getprix() + " DT");
            priceLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

            Label durationLabel = new Label("Durée: " + session.getduree_session());
            durationLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

            // Status de réservation
            Label statusLabel = new Label();
            statusLabel.setStyle("-fx-font-size: 14px;");

            // Conteneur pour les boutons
            HBox buttonsBox = new HBox(10);
            buttonsBox.setAlignment(Pos.CENTER_LEFT);

            // Bouton de vérification
            Button checkButton = new Button("Vérifier la disponibilité");
            checkButton.setStyle("-fx-background-color: #0585e6; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 14px; " +
                    "-fx-padding: 8 15; " +
                    "-fx-background-radius: 20; " );

            // Bouton de réservation
            Button reserveButton = new Button("Réserver");
            reserveButton.setStyle("-fx-background-color: #fe0369; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 14px; " +
                    "-fx-padding: 8 15; " +
                    "-fx-background-radius: 20; " );
            reserveButton.setVisible(false); // Caché par défaut

            // Action des boutons
            final int sessionId = session.getId();
            checkButton.setOnAction(event -> {
                boolean isReserved = serviceReservation.isSessionAlreadyReserved(sessionId);
                if (isReserved) {
                    statusLabel.setText("Cette session est déjà réservée");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
                    reserveButton.setVisible(false);
                } else {
                    statusLabel.setText("Cette session est disponible");
                    statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 14px;");
                    reserveButton.setVisible(true);
                }
            });

            reserveButton.setOnAction(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/reservation.fxml"));
                    Parent root = loader.load();

                    ReservationController reservationController = loader.getController();
                    reservationController.initData(sessionId);

                    Scene scene = new Scene(root);
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Ajouter les boutons au conteneur
            buttonsBox.getChildren().addAll(checkButton, reserveButton);

            // Ajouter tous les éléments à la carte
            sessionCard.getChildren().addAll(
                    gameLabel,
                    priceLabel,
                    durationLabel,
                    buttonsBox,
                    statusLabel
            );
            sessionsContainer.getChildren().add(sessionCard);
        }
    }

    @FXML
    private void hazem(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/reservation.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @FXML
    private void mmmm(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/search_session.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }
}
