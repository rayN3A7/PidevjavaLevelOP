package tn.esprit.Controllers.Coach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Models.Reservation;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceReservation;
import tn.esprit.utils.SessionManager;

import java.sql.Date;
import java.time.LocalDate;

public class ReservationController {

    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField sessionIdField;
    private int clientId = SessionManager.getInstance().getUserId();
    @FXML
    private Label statusLabel;

    private final ServiceReservation serviceReservation = new ServiceReservation();

    @FXML
    private void handleAddReservation() {
        try {
            LocalDate localDate = datePicker.getValue();
            int sessionId = Integer.parseInt(sessionIdField.getText());

            if (localDate == null) {
                showAlert("Erreur", "Veuillez sélectionner une date valide.");
                return;
            }

            if (clientId <= 0 || sessionId <= 0) {
                showAlert("Erreur", "Les identifiants doivent être des nombres positifs.");
                return;
            }

            // Vérifier si la session a déjà été réservée
            if (serviceReservation.isSessionAlreadyReserved(sessionId)) {
                showAlert("Erreur", "Cette session a déjà été réservée.");
                return;
            }

            // Créer une session avec l'ID spécifié
            Session_game session = new Session_game();
            session.setId(sessionId); // Définir l'ID de la session

            // Créer une nouvelle réservation avec la session et le client
            Reservation newReservation = new Reservation(Date.valueOf(localDate), session, clientId);

            // Ajouter la réservation
            serviceReservation.add(newReservation);

            statusLabel.setText("Réservation ajoutée avec succès !");
            statusLabel.setStyle("-fx-text-fill: green;");
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez saisir des identifiants numériques valides.");
        }
    }

    @FXML
    private void handleDeleteReservation() {
        try {
            int sessionId = Integer.parseInt(sessionIdField.getText());

            if (clientId <= 0 || sessionId <= 0) {
                showAlert("Erreur", "Les identifiants doivent être des nombres positifs.");
                return;
            }

            // Récupérer la réservation par client_id et session_id
            Reservation reservationToDelete = serviceReservation.getReservationByClientAndSession(clientId, sessionId);

            if (reservationToDelete != null) {
                // Supprimer la réservation
                serviceReservation.delete(reservationToDelete);
                statusLabel.setText("Réservation supprimée avec succès !");
                statusLabel.setStyle("-fx-text-fill: red;");
            } else {
                showAlert("Erreur", "Aucune réservation trouvée pour ce client et cette session.");
            }

            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez saisir des identifiants numériques valides.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        datePicker.setValue(null);
        sessionIdField.clear();
    }
    @FXML
    private void ListReservationC(ActionEvent event)throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/verifier_reservation.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
}
