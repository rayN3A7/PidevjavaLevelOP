package tn.esprit.Controllers.Evenement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Categorieevent;
import tn.esprit.Services.Evenement.CategorieEvService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListeCategorieController implements Initializable {
    private CategorieEvService ces = new CategorieEvService();

    @FXML
    private FlowPane eventContainer;

    @FXML
    private TextField searchField;

    private String userRole = SessionManager.getInstance().getRole().name();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        afficherCEvenements();
    }

    private void afficherCEvenements() {
        eventContainer.getChildren().clear();
        List<Categorieevent> Cevenements = ces.getAll();

        for (Categorieevent cevent : Cevenements) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/CategorieCard.fxml"));
                Parent card = loader.load();

                CategorieCardController controller = loader.getController();
                controller.setData(cevent);

                eventContainer.getChildren().add(card);
            } catch (IOException e) {
                e.getMessage();
            }
        }
    }

    @FXML
    private void ListeEvenement(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListEvenement.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @FXML
    private void Search() {
        String searchText = searchField.getText().trim().toLowerCase();
        eventContainer.getChildren().clear();
        List<Categorieevent> filteredCEvents = ces.rechercheByNom(searchText);

        for (Categorieevent cevent : filteredCEvents) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/CategorieCard.fxml"));
                Parent card = loader.load();
                CategorieCardController controller = loader.getController();
                controller.setData(cevent);
                eventContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
