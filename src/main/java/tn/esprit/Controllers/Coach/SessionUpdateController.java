package tn.esprit.Controllers.Coach;

import java.util.Date;

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


            Session_game existingSession = serviceSession.getSessionById(id);
            if (existingSession == null) {
                showAlert("Erreur", "Session introuvable");
                return;
            }

            // Validation des champs
            if (newGame.isEmpty() || newDuration.isEmpty()) {
                showAlert("Erreur", "Tous les champs doivent être remplis");
                return;
            }

            if (newPrice <= 0) {
                showAlert("Erreur", "Le prix doit être supérieur à 0");
                return;
            }


            existingSession.setprix(newPrice);
            existingSession.setduree_session(newDuration);
            existingSession.setGame(newGame);


            serviceSession.update(existingSession);

            showSuccessAlert("Succès", "Session mise à jour avec succès");


            ManagementSesssion(event);

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs valides");
        } catch (Exception e) {
            System.out.println("Erreur détaillée : " + e.getMessage());
            e.printStackTrace();
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
