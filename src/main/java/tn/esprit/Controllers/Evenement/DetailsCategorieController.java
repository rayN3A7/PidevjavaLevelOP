package tn.esprit.Controllers.Evenement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Categorieevent;
import tn.esprit.utils.SessionManager;

import java.io.IOException;

public class DetailsCategorieController {
    @FXML
    private Label eventNameLabel;
    @FXML
    private TextArea DescLabel;
    String userRole = SessionManager.getInstance().getRole().name();
    public void initData(Categorieevent event) {
        if (event != null) {
            eventNameLabel.setText(event.getNom());
            DescLabel.setText(event.getDescriptionCategorie().toString());
        }
    }
@FXML
    private void RetourButtonVersListeCategorie(ActionEvent event) throws IOException {
    if (userRole.equals("ADMIN")) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListeCategorieAdmin.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    } else {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListeCategorie.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
}
}
