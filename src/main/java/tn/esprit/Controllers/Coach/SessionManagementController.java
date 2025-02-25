package tn.esprit.Controllers.Coach;

import java.util.Date;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceSession;
import tn.esprit.utils.SessionManager;

public class SessionManagementController {

    @FXML private TextField gameField;
    @FXML private TextField priceField;
    @FXML private TextField durationField;
    @FXML private VBox sessionsContainer;

    private int currentCoachId = SessionManager.getInstance().getUserId();
    private String roleuser = SessionManager.getInstance().getRole().name();
    private final ServiceSession serviceSession = new ServiceSession();

    @FXML
    public void initialize() {
        showSessions(); // Afficher les sessions au démarrage
    }

    @FXML
    private void addSession() {
        try {
            String game = gameField.getText();
            double price = Double.parseDouble(priceField.getText());
            String duration = durationField.getText();

            if(roleuser.equals("COACH")) {
                Session_game session = new Session_game(0, price, new Date(), duration, game, currentCoachId);
                serviceSession.add(session);
                showSessions(); // Rafraîchir l'affichage
                clearFields();
                showAlert("Succès", "Session ajoutée avec succès", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Seul un coach peut ajouter une session", Alert.AlertType.WARNING);
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un prix valide", Alert.AlertType.ERROR);
        }
    }

    private void showSessions() {
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

            // Conteneur pour les boutons
            HBox buttonsBox = new HBox(10);
            buttonsBox.setStyle("-fx-padding: 10 0 0 0;");

            // Vérifier si la session appartient au coach connecté
            if (session.getCoach_id() == currentCoachId) {
                // Bouton de mise à jour
                Button updateButton = new Button("Modifier");
                updateButton.setStyle("-fx-background-color: #0585e6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 8 15; " +
                        "-fx-background-radius: 20; ");

                // Bouton de suppression
                Button deleteButton = new Button("Supprimer");
                deleteButton.setStyle("-fx-background-color: #fe0369; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 8 15; " +
                        "-fx-background-radius: 20; ");

                // Actions des boutons
                updateButton.setOnAction(event -> navigateToUpdate(session.getId()));
                deleteButton.setOnAction(event -> deleteSession(session));

                buttonsBox.getChildren().addAll(updateButton, deleteButton);
            } else {
                // Label indiquant que la session appartient à un autre coach
                Label ownerLabel = new Label("Session d'un autre coach");
                ownerLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-style: italic;");
                buttonsBox.getChildren().add(ownerLabel);
            }

            sessionCard.getChildren().addAll(
                    gameLabel,
                    priceLabel,
                    durationLabel,
                    buttonsBox
            );

            sessionsContainer.getChildren().add(sessionCard);
        }
    }

    private void navigateToUpdate(int sessionId) {
        try {
            // Vérifier si la session appartient au coach connecté
            Session_game session = serviceSession.getSessionById(sessionId);
            if (session.getCoach_id() != currentCoachId) {
                showAlert("Erreur", "Vous ne pouvez pas modifier une session qui ne vous appartient pas", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/SessionUpdate.fxml"));
            Parent root = loader.load();

            SessionUpdateController controller = loader.getController();
            controller.initData(sessionId);

            Scene scene = new Scene(root);
            Stage stage = (Stage) sessionsContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la navigation vers la mise à jour", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void deleteSession(Session_game session) {
        try {
            // Vérifier si la session appartient au coach connecté
            if (session.getCoach_id() != currentCoachId) {
                showAlert("Erreur", "Vous ne pouvez pas supprimer une session qui ne vous appartient pas", Alert.AlertType.ERROR);
                return;
            }

            serviceSession.delete(session);
            showSessions(); // Rafraîchir l'affichage
            showAlert("Succès", "Session supprimée avec succès", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la suppression", Alert.AlertType.ERROR);
        }
    }

    private void clearFields() {
        gameField.clear();
        priceField.clear();
        durationField.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void UpdateSession(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/SessionUpdate.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @FXML
    private void viewReservedSessions(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/reserved_sessions.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
