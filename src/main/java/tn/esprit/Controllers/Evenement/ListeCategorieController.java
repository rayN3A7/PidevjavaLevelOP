package tn.esprit.Controllers.Evenement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Categorieevent;
import tn.esprit.Services.Evenement.CategorieEvService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListeCategorieController implements Initializable {
    private CategorieEvService ces = new CategorieEvService();
    @FXML
    private VBox eventContainer;
    @FXML
    private TextField searchField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        afficherCEvenements();
    }

    private void afficherCEvenements() {
        List<Categorieevent> Cevenements = ces.getAll();

        for (Categorieevent cevent : Cevenements) {
            HBox eventBox = new HBox(20);
            eventBox.setStyle("-fx-padding: 10px; -fx-border-color: #ddd; -fx-border-width: 1px;");

            Label idLabel = new Label("" + cevent.getId());
            Label nomLabel = new Label(cevent.getNom());
            Label DescLabel = new Label(cevent.getDescriptionCategorie());

            idLabel.getStyleClass().add("event-label");
            nomLabel.getStyleClass().add("event-label");
            DescLabel.getStyleClass().add("event-label");
            Button ModifierButton = new Button("Modifier");
            ModifierButton.setOnAction(e -> {
                UpdateForm(cevent);
            });
            Button deleteButton = new Button("Supprimer");
            deleteButton.setOnAction(event -> deleteCategorie(cevent));
            Button DetailsButton = new Button("Details");
            DetailsButton.setOnAction(e -> DetailsEvent(cevent));

            // Ajout des labels dans le HBox
            eventBox.getChildren().addAll(idLabel, nomLabel,DescLabel,ModifierButton,deleteButton,DetailsButton);

            // Ajout du HBox dans la VBox (eventContainer)
            eventContainer.getChildren().add(eventBox);
        }
    }

    private void deleteCategorie(Categorieevent event){
        ces.delete(event);
        eventContainer.getChildren().clear();
    }
    @FXML
    private void ListeEvenemnt(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListEvenement.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
    @FXML
    private void ButtonAjouterCategorie(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/AjouterCategorie.fxml"));
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

        List<Categorieevent> filteredCEvents;
        filteredCEvents = ces.rechercheByNom(searchText);

        for (Categorieevent cevent : filteredCEvents) {
            HBox eventBox = new HBox(20);
            eventBox.setStyle("-fx-padding: 10px; -fx-border-color: #ddd; -fx-border-width: 1px;");

            Label idLabel = new Label("" + cevent.getId());
            Label nomLabel = new Label(cevent.getNom());
            Label DescLabel = new Label(cevent.getDescriptionCategorie());

            idLabel.getStyleClass().add("event-label");
            nomLabel.getStyleClass().add("event-label");
            DescLabel.getStyleClass().add("event-label");
            Button ModifierButton = new Button("Modifier");
            ModifierButton.setOnAction(e -> UpdateForm(cevent));
            Button deleteButton = new Button("Supprimer");
            deleteButton.setOnAction(event -> deleteCategorie(cevent));
            Button DetailsButton = new Button("Details");
            DetailsButton.setOnAction(e -> DetailsEvent(cevent));


            eventBox.getChildren().addAll(idLabel, nomLabel,DescLabel,ModifierButton,deleteButton,DetailsButton);
            eventContainer.getChildren().add(eventBox);
        }
    }
    public void DetailsEvent(Categorieevent cevent){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/DetailsCategorie.fxml"));
            Parent root = loader.load();

            // Accéder au contrôleur et passer les données de l'événement sélectionné
            DetailsCategorieController controller = loader.getController();
            controller.initData(cevent);

            Stage stage = (Stage) eventContainer.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.getMessage();
        }
    }
    public void UpdateForm(Categorieevent cevent){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/UpdateCategorie.fxml"));
            Parent root = loader.load();

            // Accéder au contrôleur et passer les données de l'événement sélectionné
            ModifierCategorieController controller = loader.getController();
            controller.initDataC(cevent);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Catégorie");
            stage.show();

        } catch (IOException e) {
            e.getMessage();
        }
    }
}
