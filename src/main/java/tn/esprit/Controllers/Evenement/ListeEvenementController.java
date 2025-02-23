package tn.esprit.Controllers.Evenement;



import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.Services.Evenement.CategorieEvService;
import tn.esprit.Services.Evenement.EvenementService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListeEvenementController implements Initializable {
    @FXML
    private FlowPane eventContainer;
    @FXML
    private TextField searchField;

    private final EvenementService es = new EvenementService();
    private final CategorieEvService ces = new CategorieEvService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        afficherEvenements();
    }

    private void afficherEvenements() {
        eventContainer.getChildren().clear();
        List<Evenement> evenements = es.getAll();

        for (Evenement event : evenements) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/EvenementCard.fxml"));
                Parent card = loader.load();
                EvenementCardController controller = loader.getController();
                controller.setData(event);

                eventContainer.getChildren().add(card);
                FlowPane.setMargin(card, new Insets(10));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void ButtonAjouterEvenement(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/AjouterEvenement.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }

    @FXML
    private void ButtonListeCategorie(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListeCategorie.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }

    @FXML
    private void Search() {
        String searchText = searchField.getText().trim().toLowerCase();
        eventContainer.getChildren().clear();
        List<Evenement> filteredEvents = es.GetByNom(searchText);
        System.out.println(filteredEvents);
        for (Evenement event : filteredEvents) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/EvenementCard.fxml"));
                Parent card = loader.load();
                EvenementCardController controller = loader.getController();
                controller.setData(event);

                eventContainer.getChildren().add(card);
                FlowPane.setMargin(card, new Insets(10));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}