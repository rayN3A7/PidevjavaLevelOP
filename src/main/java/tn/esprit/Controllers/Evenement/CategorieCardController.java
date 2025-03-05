package tn.esprit.Controllers.Evenement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Categorieevent;
import tn.esprit.Services.Evenement.CategorieEvService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;

public class CategorieCardController {
    @FXML
    private Label nomLabel, desclabel;
    @FXML
    private HBox actionsHBox;

    @FXML
    private Button modifierButton, supprimerButton, detailsButton;

    private Categorieevent categorie;
    private CategorieEvService ces = new CategorieEvService();
    private String userRole = SessionManager.getInstance().getRole().name();

    public void setData(Categorieevent cevent) {
        this.categorie = cevent;
        nomLabel.setText(cevent.getNom());
        desclabel.setText(cevent.getDescriptionCategorie());

        if (userRole.equals("ADMIN")) {
            // Créer le bouton Modifier
            modifierButton = new Button("Modifier");
            modifierButton.getStyleClass().add("edit-button");
            modifierButton.setOnAction(e -> updateForm());

            // Créer le bouton Supprimer
            supprimerButton = new Button("Supprimer");
            supprimerButton.getStyleClass().add("delete-button");
            supprimerButton.setOnAction(e -> deleteCategorie());

            // Ajouter les boutons au HBox
            actionsHBox.getChildren().addAll(modifierButton, supprimerButton);
        }
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
            Scene currentScene = modifierButton.getScene();
            currentScene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDetails() {
        if (userRole.equals("ADMIN")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/DetailsCategorieAdmin.fxml"));
                Parent root = loader.load();
                DetailsCategorieController controller = loader.getController();
                controller.initData(categorie);

                // Récupérer la scène actuelle et changer son contenu
                Stage stage = (Stage) detailsButton.getScene().getWindow();
                stage.getScene().setRoot(root);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/DetailsCategorie.fxml"));
                Parent root = loader.load();
                DetailsCategorieController controller = loader.getController();
                controller.initData(categorie);

                // Récupérer la scène actuelle et changer son contenu
                Stage stage = (Stage) detailsButton.getScene().getWindow();
                stage.getScene().setRoot(root);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
