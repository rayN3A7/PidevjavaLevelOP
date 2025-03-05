package tn.esprit.Controllers.Coach;

import java.util.Date;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceSession;

public class SessionUpdateController {

    @FXML private TextField updateIdField;
    @FXML private TextField updateGameField;
    @FXML private TextField updatePriceField;
    @FXML private TextField updateDurationField;
    @FXML private Label availabilityLabel;

    private final ServiceSession serviceSession = new ServiceSession();

    @FXML
    private void updateSession(ActionEvent event) {
        try {
            int id = Integer.parseInt(updateIdField.getText());
            String newGame = updateGameField.getText();
            double newPrice = Double.parseDouble(updatePriceField.getText());
            String newDuration = updateDurationField.getText();

            // Récupérer la session existante depuis la base de données
            Session_game existingSession = serviceSession.getSessionById(id);
            if (existingSession == null) {
                showAlert("Erreur", "Session introuvable", Alert.AlertType.ERROR);
                return;
            }

            // Validation des champs
            if (newGame.isEmpty() || newDuration.isEmpty()) {
                showAlert("Erreur", "Tous les champs doivent être remplis", Alert.AlertType.ERROR);
                return;
            }

            if (newPrice <= 0) {
                showAlert("Erreur", "Le prix doit être supérieur à 0", Alert.AlertType.ERROR);
                return;
            }

            // Mise à jour des champs de l'objet existant
            existingSession.setprix(newPrice);
            existingSession.setduree_session(newDuration);
            existingSession.setGame(newGame);

            // Appel du service pour mettre à jour en base
            serviceSession.update(existingSession);

            showAlert("Succès", "Session mise à jour avec succès", Alert.AlertType.INFORMATION);

            // Retourner à la page de gestion
            ManagementSesssion(event);

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs valides", Alert.AlertType.ERROR);
        } catch (Exception e) {
            System.out.println("Erreur détaillée : " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void ManagementSesssion(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/SessionManagement.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @FXML
    public void initData(int sessionId) {
        Session_game session = serviceSession.getSessionById(sessionId);
        if (session != null) {
            updateIdField.setText(String.valueOf(sessionId));
            updateGameField.setText(session.getGame());
            updatePriceField.setText(String.valueOf(session.getprix()));
            updateDurationField.setText(session.getduree_session());
            Date existingDateCreation = session.getdate_creation();
            updateIdField.setEditable(false);
            availabilityLabel.setText("Session chargée avec succès");
            availabilityLabel.setStyle("-fx-text-fill: #2ecc71;");
        } else {
            availabilityLabel.setText("Erreur lors du chargement de la session");
            availabilityLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }
}
