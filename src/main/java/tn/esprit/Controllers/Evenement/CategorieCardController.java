package tn.esprit.Controllers.Evenement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Categorieevent;
import tn.esprit.Services.Evenement.CategorieEvService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;

public class CategorieCardController {
    @FXML
    private Label nomLabel, desclabel;

    @FXML
    private Button modifierButton, supprimerButton, detailsButton;

    private Categorieevent categorie;
    private CategorieEvService ces = new CategorieEvService();
    private String userRole = SessionManager.getInstance().getRole().name();

    public void setData(Categorieevent cevent) {
        this.categorie = cevent;
        nomLabel.setText(cevent.getNom());
        desclabel.setText(cevent.getDescriptionCategorie());

        if (userRole.equals("CLIENT") || userRole.equals("COACH")) {
            modifierButton.setDisable(true);
            supprimerButton.setDisable(true);
        }

        modifierButton.setOnAction(e -> updateForm());
        supprimerButton.setOnAction(e -> deleteCategorie());
        detailsButton.setOnAction(e -> showDetails());
    }

    private void deleteCategorie() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression de la catégorie");
        alert.setContentText("Voulez-vous vraiment supprimer " + categorie.getNom() + " ?");
        alert.showAndWait();

        if (alert.getResult().getText().equals("OK")) {
            ces.delete(categorie);
        }
    }

    private void updateForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/UpdateCategorie.fxml"));
            Parent root = loader.load();
            ModifierCategorieController controller = loader.getController();
            controller.initDataC(categorie);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/DetailsCategorie.fxml"));
            Parent root = loader.load();
            DetailsCategorieController controller = loader.getController();
            controller.initData(categorie);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
