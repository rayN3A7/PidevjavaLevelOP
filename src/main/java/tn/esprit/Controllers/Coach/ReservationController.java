package tn.esprit.Controllers.Coach;

import java.sql.Date;
import java.time.LocalDate;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.Models.Reservation;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceReservation;
import tn.esprit.utils.SessionManager;

public class ReservationController {

    @FXML private DatePicker datePicker;
    @FXML private Label statusLabel;

    private int sessionId;
    private int clientId = SessionManager.getInstance().getUserId();

    private final ServiceReservation serviceReservation = new ServiceReservation();

    @FXML
    private void handleAddReservation() {
        try {
            LocalDate localDate = datePicker.getValue();
            LocalDate today = LocalDate.now();

            if (localDate == null) {
                showAlert("Erreur", "Veuillez sélectionner une date valide.");
                return;
            }

            if (localDate.isBefore(today)) {
                showAlert("Erreur", "Impossible de réserver pour une date passée. Veuillez choisir une date future.");
                return;
            }

            if (clientId <= 0 || sessionId <= 0) {
                showAlert("Erreur", "Informations de réservation invalides.");
                return;
            }


            if (serviceReservation.isSessionAlreadyReserved(sessionId)) {
                showAlert("Erreur", "Cette session a déjà été réservée.");
                return;
            }


            Session_game session = new Session_game();
            session.setId(sessionId);


            Reservation reservation = new Reservation(
                    Date.valueOf(localDate),
                    session,
                    clientId
            );

            serviceReservation.add(reservation);
            statusLabel.setText("Réservation effectuée avec succès !");
            statusLabel.setStyle("-fx-text-fill: #2ecc71;");


            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> goToMyReservations());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la réservation: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteReservation() {
        try {
            if (clientId <= 0 || sessionId <= 0) {
                showAlert("Erreur", "Informations de réservation invalides.");
                return;
            }

            Reservation reservationToDelete = serviceReservation.getReservationByClientAndSession(clientId, sessionId);

            if (reservationToDelete != null) {
                serviceReservation.delete(reservationToDelete);
                statusLabel.setText("Réservation supprimée avec succès !");
                statusLabel.setStyle("-fx-text-fill: red;");
            } else {
                showAlert("Erreur", "Aucune réservation trouvée pour cette session.");
            }

            clearFields();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        showStyledAlert(title, message, "/forumUI/icons/alert.png", "/forumUI/icons/alert.png", "OK", 80, 80);
    }

    private void showSuccessAlert(String title, String message) {
        showStyledAlert(title, message, "/forumUI/icons/sucessalert.png", "/forumUI/icons/sucessalert.png", "OK", 60, 80);
    }
    private void showStyledAlert (String title, String message, String iconPath, String stageIconPath,
                                  String buttonText, double iconHeight, double iconWidth) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(new Image(getClass().getResource(iconPath).toExternalForm()));
        icon.setFitHeight(iconHeight);
        icon.setFitWidth(iconWidth);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource(stageIconPath).toExternalForm()));

        ButtonType okButton = new ButtonType(buttonText, ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        alert.showAndWait();
    }

    private void clearFields() {
        datePicker.setValue(null);
    }

    @FXML
    private void ListReservationC(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/verifier_reservation.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    public void initData(int sessionId) {
        this.sessionId = sessionId;
    }

    @FXML
    private void goToMyReservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/my_sessions.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) datePicker.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la navigation vers Mes Réservations");
        }
    }
}
