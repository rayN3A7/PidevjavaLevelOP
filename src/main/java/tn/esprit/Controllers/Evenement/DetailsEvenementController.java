package tn.esprit.Controllers.Evenement;

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
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.Services.Evenement.CategorieEvService;
import tn.esprit.Services.Evenement.EvenementService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;

public class DetailsEvenementController {
    private final CategorieEvService ces = new CategorieEvService();
    private final EvenementService es = new EvenementService();
    @FXML
    private Label eventNameLabel,eventDateLabel,eventLieuLabel,eventNBPLabel,eventCatLabel;
    @FXML
    private Button reserverButton;
    private Evenement currentEvent;


    public void initData(Evenement event) {
        if (event != null) {
            currentEvent=event;
            eventNameLabel.setText(event.getNom_event());
            eventDateLabel.setText(event.getDate_event().toString());
            eventLieuLabel.setText(event.getLieu_event());
            eventNBPLabel.setText(String.valueOf(event.getMax_places_event()));
            eventCatLabel.setText(ces.getNomCategorieEvent(event.getCategorie_id()));
        }
    }
    @FXML
    private void RetourButtonVersListeEvenement(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListEvenement.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
    @FXML
    private void reserverPlace(ActionEvent e1) {
        int userId = SessionManager.getInstance().getUserId();

        if (currentEvent== null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Aucun événement sélectionné !");
            alert.showAndWait();
            return;
        }

        if (es.reservationExiste(userId, currentEvent.getId())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Réservation");
            alert.setHeaderText(null);
            alert.setContentText("Vous avez déjà réservé une place pour cet événement.");
            alert.showAndWait();
            return;
        }

        boolean success = es.reserverPlace(userId, currentEvent.getId());

        Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle("Réservation");
        alert.setHeaderText(null);
        alert.setContentText(success ? "Votre place a été réservée avec succès !" : "Échec de la réservation. Vérifiez la disponibilité.");
        alert.showAndWait();
    }

}
