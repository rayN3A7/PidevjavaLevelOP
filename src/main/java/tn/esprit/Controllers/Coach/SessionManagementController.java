package tn.esprit.Controllers.Coach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceSession;
import tn.esprit.utils.SessionManager;

import java.util.Date;
import java.util.List;

public class SessionManagementController {

    @FXML private TextField gameField;
    @FXML private TextField priceField;
    @FXML private TextField durationField;
    @FXML private TextArea sessionsTextArea;
    @FXML private TextField deleteGameField;
    @FXML private Button addSessionButton;
    @FXML private Button showSessionsButton;
    @FXML private Button deleteSessionButton;

    private int coachId = SessionManager.getInstance().getUserId();
    private String roleuser = SessionManager.getInstance().getRole().name();

    private final ServiceSession serviceSession = new ServiceSession();

    @FXML
    private void addSession() {
        try {
            String game = gameField.getText();
            double price = Double.parseDouble(priceField.getText());
            String duration = durationField.getText();
            if(roleuser =="COACH"){
            Session_game session = new Session_game(0, price, new Date(), duration, game, coachId);
            serviceSession.add(session);
            showSessions();}
            else{
                showAlert("Ajout Session","Le coach seulement peut ajouter une session", Alert.AlertType.WARNING);
            }
        } catch (NumberFormatException e) {
            System.out.println("Erreur : Vérifiez les valeurs saisies.");
        }
    }

    @FXML
    private void showSessions() {
        List<Session_game> sessions = serviceSession.getAll();
        StringBuilder sessionListText = new StringBuilder();

        for (Session_game session : sessions) {
            sessionListText.append("Jeu: ").append(session.getGame())
                    .append("\nPrix: ").append(session.getprix()).append(" DT")
                    .append("\nDurée: ").append(session.getduree_session())
                    .append("\n--------------------\n");
        }
        sessionsTextArea.setText(sessionListText.toString());
    }

    @FXML
    private void deleteSession() {
        try {
            // Récupérer l'ID de la session à supprimer
            int sessionId = Integer.parseInt(deleteGameField.getText());

            // Créer un objet Session_game avec l'ID spécifié
            Session_game sessionToDelete = new Session_game();
            sessionToDelete.setId(sessionId);

            // Supprimer la session
            serviceSession.delete(sessionToDelete);

            // Rafraîchir la liste des sessions
            showSessions();
        } catch (NumberFormatException e) {
            System.out.println("Erreur : L'ID de la session doit être un nombre valide.");
        }
    }
    @FXML
    private void UpdateSession(ActionEvent event)throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/SessionUpdate.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
