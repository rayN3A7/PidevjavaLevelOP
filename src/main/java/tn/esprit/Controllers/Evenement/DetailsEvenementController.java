package tn.esprit.Controllers.Evenement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.Services.Evenement.CategorieEvService;
import tn.esprit.Services.Evenement.EvenementService;

import java.io.IOException;

public class DetailsEvenementController {
    private CategorieEvService ces = new CategorieEvService();
    @FXML
    private Label eventNameLabel,eventDateLabel,eventLieuLabel,eventNBPLabel,eventCatLabel;


    public void initData(Evenement event) {
        if (event != null) {
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

}
