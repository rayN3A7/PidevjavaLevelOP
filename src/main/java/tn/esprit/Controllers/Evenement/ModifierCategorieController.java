package tn.esprit.Controllers.Evenement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Categorieevent;
import tn.esprit.Services.Evenement.CategorieEvService;

public class ModifierCategorieController {
    private CategorieEvService ces = new CategorieEvService();
    @FXML
    private TextField NomCategorie;
    @FXML
    private TextArea DescCategorie;
    private Categorieevent ce;

    public void initDataC(Categorieevent cevent) {
        ce = cevent;
        NomCategorie.setText(ce.getNom());
        DescCategorie.setText(ce.getDescriptionCategorie());
    }
    @FXML
    public void ModifierCategorie() {
        if(ce !=null){
            ce.setNom(NomCategorie.getText());
            ce.setDescriptioncategorie(DescCategorie.getText());
            ces.update(ce);
            showAlert(Alert.AlertType.INFORMATION,"Succès","Catégorie modifiée avec succès");
        }else{
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez vérifier les champs");
        }
    }
    @FXML
    public void Annuler(){
        NomCategorie.clear();
        DescCategorie.clear();
    }
    @FXML
    public void ButtonlisteCategories(ActionEvent cevent)throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListeCategorieAdmin.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) cevent.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
