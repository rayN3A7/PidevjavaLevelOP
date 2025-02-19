package tn.esprit.Controllers.Coach;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceReservation;
import tn.esprit.Services.ServiceSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

public class VerifierReservationController {

    @FXML
    private ComboBox<Session_game> sessionComboBox; // ComboBox pour sélectionner la session
    @FXML
    private Button verifierButton; // Bouton pour vérifier la réservation

    private ServiceSession serviceSession = new ServiceSession();
    private ServiceReservation serviceReservation = new ServiceReservation();

    @FXML
    public void initialize() {
        // Charger les sessions disponibles
        System.out.println("Chargement des sessions...");
        sessionComboBox.getItems().setAll(serviceSession.getAll());
        System.out.println("Sessions disponibles : " + sessionComboBox.getItems());
    }

    @FXML
    public void handleVerifierReservation() {
        // Vérification de la sélection d'une session
        Session_game selectedSession = sessionComboBox.getValue();
        if (selectedSession == null) {
            showAlert("Erreur", "Veuillez sélectionner une session", AlertType.ERROR);
            return;
        }

        // Vérification si la session est déjà réservée
        boolean isReserved = serviceReservation.isSessionReserved(selectedSession.getId());
        System.out.println("Vérification de la réservation pour la session ID: " + selectedSession.getId() + " -> Réservée ? " + isReserved);

        if (isReserved) {
            showAlert("Réservation existante", "Cette session est déjà réservée", AlertType.INFORMATION);
        } else {
            showAlert("Pas de réservation", "Cette session n'est pas encore réservée", AlertType.INFORMATION);
        }
    }

    // Méthode pour afficher des alertes
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void hazem(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/reservation.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
    @FXML
    private void mmmm (ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/search_session.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
}
