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

public class AjouterCategorieController {
    CategorieEvService ces = new CategorieEvService();

    @FXML
    private TextField NomCategorie;
    @FXML
    private TextArea DescCategorie;

    @FXML
    private void AjouterCategorie(){
        try{
            String nom = NomCategorie.getText();
        String description = DescCategorie.getText();
        Categorieevent ce = new Categorieevent(description,nom);
        ces.add(ce);
        }catch(Exception e){
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez vérifier les champs");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void Annuler(){
        NomCategorie.clear();
        DescCategorie.clear();
    }
    @FXML
    private void ButtonListeCategories(ActionEvent event)throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListeCategorie.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }


}
