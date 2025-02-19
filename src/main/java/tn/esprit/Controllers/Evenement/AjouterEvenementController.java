package tn.esprit.Controllers.Evenement;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Categorieevent;
import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.Services.Evenement.CategorieEvService;
import tn.esprit.Services.Evenement.EvenementService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.ResourceBundle;

public class AjouterEvenementController {
    EvenementService es = new EvenementService();
    CategorieEvService ces = new CategorieEvService();
    @FXML
    private TextField NomEvent,LieuEvent,NBPEvent;
    @FXML
    private DatePicker DateEvent;
    @FXML
    private ComboBox<String> CatEvent;

    @FXML
    private void initialize() {
        GetCategorie();
    }
    private void GetCategorie(){
        List<Categorieevent> categorieList = ces.getAll();
        List<String> categories = categorieList.stream()
                .map(Categorieevent::getNom)
                .toList();
        CatEvent.getItems().setAll(categories);
    }
    @FXML
    private void AjouterEvenemnt(){
        try{
        String nom_event = NomEvent.getText();
        String lieu_event = LieuEvent.getText();
        int max_places_event = Integer.parseInt(NBPEvent.getText());
        Date date = Date.valueOf(DateEvent.getValue());
        String categorie_event = CatEvent.getValue();
        int categorie_id = ces.getIdCategorieEvent(categorie_event);
        Evenement evenement = new Evenement(categorie_id,max_places_event,nom_event,lieu_event,date);
        es.add(evenement);
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Evenement ajouté avec succès");
        }catch(Exception e){
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez vérifier les champs");
        }
    }

    @FXML
    private void Annuler(ActionEvent event){
        NomEvent.clear();
        LieuEvent.clear();
        NBPEvent.clear();
        DateEvent.getEditor().clear();
        CatEvent.getSelectionModel().clearSelection();
    }
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    public void ButtonListeEvenement(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListEvenement.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }

}
