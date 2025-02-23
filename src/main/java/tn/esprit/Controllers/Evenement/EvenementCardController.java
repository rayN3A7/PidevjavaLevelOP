package tn.esprit.Controllers.Evenement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.Services.EmailService;
import tn.esprit.Services.Evenement.CategorieEvService;
import tn.esprit.Services.Evenement.EvenementService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;

public class EvenementCardController {
    @FXML
    private Label nomLabel, dateLabel, lieuLabel, categorieLabel, placesLabel;
    @FXML
    private Button reserverButton, modifierButton, supprimerButton, detailsButton;

    private Evenement event;
    private EvenementService es = new EvenementService();
    private CategorieEvService ces = new CategorieEvService();
    String userRole = SessionManager.getInstance().getRole().name();

    public void setData(Evenement event) {
        this.event = event;
        nomLabel.setText(event.getNom_event());
        dateLabel.setText(event.getDate_event().toString());
        lieuLabel.setText(event.getLieu_event());
        categorieLabel.setText(ces.getNomCategorieEvent(event.getCategorie_id()));
        placesLabel.setText("Places restantes: " + event.getMax_places_event());
        if (userRole.equals("CLIENT") || userRole.equals("COACH")) {
            modifierButton.setDisable(true);
            supprimerButton.setDisable(true);
        }

        reserverButton.setOnAction(e -> reserverPlace(event));
        modifierButton.setOnAction(e -> updateForm());
        supprimerButton.setOnAction(e -> deleteEvent());
        detailsButton.setOnAction(e -> showDetails());
    }

    @FXML
    private void reserverPlace(Evenement e1) {
        int userId = SessionManager.getInstance().getUserId();
        String userEmail = SessionManager.getInstance().getEmail(); // Récupérer l'email de l'utilisateur connecté

        if (event == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun événement sélectionné !");
            return;
        }

        if (es.reservationExiste(userId, event.getId())) {
            showAlert(Alert.AlertType.WARNING, "Réservation", "Vous avez déjà réservé une place pour cet événement.");
            return;
        }

        if (event.getMax_places_event() <= 0) {
            showAlert(Alert.AlertType.ERROR, "Réservation", "Il n'y a plus de places disponibles pour cet événement.");
            return;
        }

        boolean success = es.reserverPlace(userId, event.getId());

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Réservation", "Votre place a été réservée avec succès !");

            // Envoi de l'email en utilisant le fichier HTML comme template
            try {
                String subject = "Confirmation de réservation pour " + event.getNom_event();
                EmailService.sendEmailWithTemplate(userEmail, subject, event, ces);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'envoi d'email", "Votre réservation a été prise en compte, mais l'email de confirmation n'a pas pu être envoyé.");
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Échec", "La réservation a échoué. Vérifiez la disponibilité des places.");
        }
    }
    private void showAlert(Alert.AlertType type, String titre, String contenu) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
    private void updateForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/UpdateEvenement.fxml"));
            Parent root = loader.load();
            ModifierEvenementController controller = loader.getController();
            controller.initData(event);
            Stage stage = (Stage) modifierButton.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteEvent() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression de l'événement");
        alert.setContentText("Voulez-vous vraiment supprimer l'événement " + event.getNom_event() + " ?");
        alert.showAndWait();

        if (alert.getResult().getText().equals("OK")) {
            es.delete(event);
        } else {
            alert.close();
        }
    }

    private void showDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/DetailsEvenement.fxml"));
            Parent root = loader.load();
            DetailsEvenementController controller = loader.getController();
            controller.initData(event);
            Stage stage = (Stage) detailsButton.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
