package tn.esprit.Controllers.Evenement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Categorieevent;
import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.Services.Evenement.CategorieEvService;
import tn.esprit.Services.Evenement.EvenementService;

import java.io.IOException;
import java.util.List;

public class ModifierEvenementController {
    @FXML
    private TextField NomEvent;
    @FXML
    private DatePicker DateEvent;
    @FXML
    private TextField LieuEvent;
    @FXML
    private TextField NBPEvent;
    @FXML
    private ComboBox<String> CatEvent;

    private Evenement eventAModifier;
    private final EvenementService es = new EvenementService();
    private final CategorieEvService ces = new CategorieEvService();

    public void initData(Evenement event) {
        this.eventAModifier = event;
        NomEvent.setText(event.getNom_event());
        DateEvent.setValue(event.getDate_event().toLocalDate());
        LieuEvent.setText(event.getLieu_event());
        NBPEvent.setText(String.valueOf(event.getMax_places_event()));
        CatEvent.setValue(ces.getNomCategorieEvent(event.getCategorie_id()));
    }
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
    private void ModifierEvenement() {
        if (eventAModifier != null) {
            eventAModifier.setNom_event(NomEvent.getText());
            eventAModifier.setDate_event(java.sql.Date.valueOf(DateEvent.getValue()));
            eventAModifier.setLieu_event(LieuEvent.getText());
            eventAModifier.setMax_places_event(Integer.parseInt(NBPEvent.getText()));
            String selectedCategory = CatEvent.getValue();
            int categoryId = ces.getIdCategorieEvent(selectedCategory);
            eventAModifier.setCategorie_id(categoryId);

            es.update(eventAModifier);
        }
    }
    @FXML
    private void Annuler() {
        NomEvent.clear();
        LieuEvent.clear();
        NBPEvent.clear();
        DateEvent.getEditor().clear();
        CatEvent.getSelectionModel().clearSelection();
    }
    @FXML
    private void ButtonListeEvenement(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListEvenement.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
}
