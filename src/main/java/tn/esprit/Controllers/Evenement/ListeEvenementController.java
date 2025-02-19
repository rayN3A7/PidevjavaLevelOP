package tn.esprit.Controllers.Evenement;



import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.Services.Evenement.CategorieEvService;
import tn.esprit.Services.Evenement.EvenementService;
import tn.esprit.Controllers.Evenement.DetailsEvenementController;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListeEvenementController implements Initializable {
    @FXML
    private VBox eventContainer;
    @FXML
    private TextField searchField;

    private final EvenementService es = new EvenementService();
    private final CategorieEvService ces = new CategorieEvService();
    String userRole = SessionManager.getInstance().getRole().name();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        afficherEvenements();
    }

    private void afficherEvenements() {
        List<Evenement> evenements = es.getAll();

        for (Evenement event : evenements) {
            HBox eventBox = new HBox(20);
            eventBox.setStyle("-fx-padding: 10px; -fx-border-color: #ddd; -fx-border-width: 1px;");

            Label idLabel = new Label("" + event.getId());
            Label nomLabel = new Label(event.getNom_event());
            Label dateLabel = new Label(event.getDate_event().toString());
            Label lieuLabel = new Label(event.getLieu_event());
            Label categorieLabel = new Label(ces.getNomCategorieEvent(event.getCategorie_id()));
            Label placesLabel = new Label(""+event.getMax_places_event());

            idLabel.getStyleClass().add("event-label");
            nomLabel.getStyleClass().add("event-label");
            dateLabel.getStyleClass().add("event-label");
            lieuLabel.getStyleClass().add("event-label");
            categorieLabel.getStyleClass().add("categorie-label");
            placesLabel.getStyleClass().add("event-label");
            Button ReserverButton = new Button("Reserver Une place");
            ReserverButton.setOnAction(e -> reserverPlace(event));
            Button ModifierButton = new Button("Modifier");
            ModifierButton.setOnAction(event1 -> UpdateForm(event));
            Button deleteButton = new Button("Supprimer");
            deleteButton.setOnAction(event1 -> deleteEvent(event));
            if(userRole == "CLIENT" || userRole == "COACH"){
                ModifierButton.setDisable(true);
                deleteButton.setDisable(true);
            }
            Button DetailsButton = new Button("Details");
            DetailsButton.setOnAction(e -> DetailsEvent(event));

            // Ajout des labels dans le HBox
            eventBox.getChildren().addAll(idLabel, nomLabel, dateLabel, lieuLabel, categorieLabel, placesLabel,ReserverButton,ModifierButton,deleteButton,DetailsButton);

            // Ajout du HBox dans la VBox (eventContainer)
            eventContainer.getChildren().add(eventBox);
        }
    }
    public void deleteEvent(Evenement event){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression de l'événement");
        alert.setContentText("Voulez-vous vraiment supprimer l'événement " + event.getNom_event() + " ?");
        alert.showAndWait();
        if(alert.getResult().getText().equals("OK")){
        es.delete(event);
        eventContainer.getChildren().clear();
        }else {
            alert.close();
        }
        }

        public void UpdateForm(Evenement event){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/UpdateEvenement.fxml"));
                Parent root = loader.load();

                // Accéder au contrôleur et passer les données de l'événement sélectionné
                ModifierEvenementController controller = loader.getController();
                controller.initData(event);

                Stage stage = (Stage) eventContainer.getScene().getWindow();
                stage.getScene().setRoot(root);

            } catch (IOException e) {
                e.getMessage();
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

        List<Evenement> filteredEvents;
        filteredEvents = es.rechercheByNom(searchText);

        for (Evenement event : filteredEvents) {
            HBox eventBox = new HBox(20);
            eventBox.setStyle("-fx-padding: 10px; -fx-border-color: #ddd; -fx-border-width: 1px;");

            Label idLabel = new Label("" + event.getId());
            Label nomLabel = new Label(event.getNom_event());
            Label dateLabel = new Label(event.getDate_event().toString());
            Label lieuLabel = new Label(event.getLieu_event());
            Label categorieLabel = new Label(ces.getNomCategorieEvent(event.getCategorie_id()));
            Label placesLabel = new Label("" + event.getMax_places_event());

            idLabel.getStyleClass().add("event-label");
            nomLabel.getStyleClass().add("event-label");
            dateLabel.getStyleClass().add("event-label");
            lieuLabel.getStyleClass().add("event-label");
            categorieLabel.getStyleClass().add("categorie-label");
            placesLabel.getStyleClass().add("event-label");
            Button ReserverButton = new Button("Reserver Une place");
            ReserverButton.setOnAction(e -> reserverPlace(event));
            Button ModifierButton = new Button("Modifier");
            ModifierButton.setOnAction(e -> UpdateForm(event));

            Button deleteButton = new Button("Supprimer");
            deleteButton.setOnAction(e -> deleteEvent(event));
            if(userRole == "CLIENT" || userRole == "COACH"){
                ModifierButton.setDisable(true);
                deleteButton.setDisable(true);
            }
            Button DetailsButton = new Button("Details");
            DetailsButton.setOnAction(e -> DetailsEvent(event));


            eventBox.getChildren().addAll(idLabel, nomLabel, dateLabel, lieuLabel, categorieLabel, placesLabel,ReserverButton ,ModifierButton, deleteButton,DetailsButton);
            eventContainer.getChildren().add(eventBox);
        }
    }

    public void DetailsEvent(Evenement event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/DetailsEvenement.fxml"));
            Parent root = loader.load();

            // Accéder au contrôleur et passer les données de l'événement sélectionné
            DetailsEvenementController controller = loader.getController();
            controller.initData(event);

            Stage stage = (Stage) eventContainer.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.getMessage();
        }
    }
    public void reserverPlace(Evenement event) {
        int userId = SessionManager.getInstance().getUserId();
        // Vérifier si l'utilisateur a déjà réservé
        if (es.reservationExiste(userId, event.getId())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Réservation");
            alert.setHeaderText(null);
            alert.setContentText("Vous avez déjà réservé une place pour cet événement.");
            alert.showAndWait();
            return;
        }

        boolean success = es.reserverPlace(userId, event.getId());

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Réservation");
            alert.setHeaderText(null);
            alert.setContentText("Votre place a été réservée avec succès !");
            alert.showAndWait();

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Échec de la réservation. Vérifiez la disponibilité.");
            alert.showAndWait();
        }
    }



}
