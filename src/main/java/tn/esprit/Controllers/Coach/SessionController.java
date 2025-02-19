package tn.esprit.Controllers.Coach;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class SessionController {

    @FXML private TextField idField;
    @FXML private Label sessionDetailsLabel;
    @FXML private Button searchButton;
    @FXML private Button promoButton;
    @FXML private VBox promoSessionsVBox;

    private ServiceSession serviceSession = new ServiceSession();

    // Méthode pour rechercher une session par ID
    private void searchSessionById() {
        try {
            int id = Integer.parseInt(idField.getText());
            Session_game session = serviceSession.getSessionById(id);
            if (session != null) {
                sessionDetailsLabel.setText("ID: " + session.getId() + "\nGame: " + session.getGame() +
                        "\nPrice: " + session.getprix() + "\nDuration: " + session.getduree_session());
            } else {
                sessionDetailsLabel.setText("Session not found.");
            }
        } catch (NumberFormatException e) {
            sessionDetailsLabel.setText("Invalid ID format.");
        }
    }

    // Méthode pour afficher les sessions en promo
    private void showPromoSessions() {
        List<Session_game> promoSessions = serviceSession.getSessionsInPromo();
        promoSessionsVBox.getChildren().clear(); // Vider la VBox avant d'ajouter les nouvelles données

        // Ajouter un label pour chaque session en promo
        for (Session_game session : promoSessions) {
            Label sessionLabel = new Label("ID: " + session.getId() +
                    "\nGame: " + session.getGame() +
                    "\nPrice: " + session.getprix() +
                    "\nDuration: " + session.getduree_session() );
            sessionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7F8C8D; -fx-padding: 10px; -fx-background-color: #ecf0f1; -fx-border-radius: 8px;");
            promoSessionsVBox.getChildren().add(sessionLabel);
        }
    }

    // Initialisation du contrôleur
    @FXML
    public void initialize() {
        searchButton.setOnAction(event -> searchSessionById());
        promoButton.setOnAction(event -> showPromoSessions());
    }
    @FXML
    private void search(ActionEvent event)throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/search_session.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
}
