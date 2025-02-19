package tn.esprit.Controllers.Coach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceSession;

import java.util.Date;

public class SessionUpdateController {

    @FXML private TextField updateIdField;
    @FXML private TextField updateGameField;
    @FXML private TextField updatePriceField;
    @FXML private TextField updateDurationField;
    @FXML private TextField sessionIdField;
    @FXML private Label availabilityLabel;

    private final ServiceSession serviceSession = new ServiceSession();

    @FXML
    private void updateSession() {
        try {
            int id = Integer.parseInt(updateIdField.getText());
            String newGame = updateGameField.getText();
            double newPrice = Double.parseDouble(updatePriceField.getText());
            String newDuration = updateDurationField.getText();

            // Créer un nouvel objet Session_game avec les nouvelles informations
            Session_game session = new Session_game(id, newPrice, new Date(), newDuration, newGame, 1);
            serviceSession.update(session);
            System.out.println("Session mise à jour avec succès !");
        } catch (NumberFormatException e) {
            System.out.println("Erreur : Veuillez entrer des valeurs valides.");
        }
    }
    @FXML
    private void ManagementSesssion(ActionEvent event)throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/SessionManagement.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
}
