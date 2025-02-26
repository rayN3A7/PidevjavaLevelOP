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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
    @FXML
    private ComboBox<String> TimeEvent;

    private Evenement eventAModifier;
    private final EvenementService es = new EvenementService();
    private final CategorieEvService ces = new CategorieEvService();

    public void initData(Evenement event) {
        this.eventAModifier = event;

        // Extraire la date et l'heure depuis Timestamp
        Timestamp eventTimestamp = event.getDate_event();
        LocalDateTime eventDateTime = eventTimestamp.toLocalDateTime();

        // Séparer la date et l'heure
        LocalDate datePart = eventDateTime.toLocalDate();
        LocalTime timePart = eventDateTime.toLocalTime();

        // Formater l'heure en HH:mm
        String formattedTime = String.format("%02d:%02d", timePart.getHour(), timePart.getMinute());

        // Remplir les champs
        NomEvent.setText(event.getNom_event());
        DateEvent.setValue(datePart);
        LieuEvent.setText(event.getLieu_event());
        NBPEvent.setText(String.valueOf(event.getMax_places_event()));
        CatEvent.setValue(ces.getNomCategorieEvent(event.getCategorie_id()));
        TimeEvent.setValue(formattedTime);
    }
    @FXML
    private void initialize() {
        fillTimeComboBox();
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
            try {
                // Vérifier que tous les champs sont remplis
                if (NomEvent.getText().trim().isEmpty() || LieuEvent.getText().trim().isEmpty() ||
                        NBPEvent.getText().trim().isEmpty() || DateEvent.getValue() == null ||
                        TimeEvent.getValue() == null || CatEvent.getValue() == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs doivent être remplis.");
                    return;
                }

                eventAModifier.setNom_event(NomEvent.getText());
                eventAModifier.setLieu_event(LieuEvent.getText());
                eventAModifier.setMax_places_event(Integer.parseInt(NBPEvent.getText()));

                String selectedCategory = CatEvent.getValue();
                int categoryId = ces.getIdCategorieEvent(selectedCategory);
                eventAModifier.setCategorie_id(categoryId);

                // Récupérer la date et l'heure sélectionnées
                LocalDate selectedDate = DateEvent.getValue();
                LocalTime selectedTime = LocalTime.parse(TimeEvent.getValue());
                LocalDateTime dateTime = LocalDateTime.of(selectedDate, selectedTime);

                // Vérifier que la date est dans le futur
                if (dateTime.isBefore(LocalDateTime.now())) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "La date de l'événement doit être dans le futur.");
                    return;
                }

                // Convertir LocalDateTime en Timestamp et enregistrer
                eventAModifier.setDate_event(Timestamp.valueOf(dateTime));

                es.update(eventAModifier);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement modifié avec succès");

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le nombre de places doit être un nombre valide.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue. Veuillez réessayer.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun événement sélectionné pour modification.");
        }
    }
    private void fillTimeComboBox() {
        List<String> timeOptions = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 15) { // Incrément de 15 minutes
                timeOptions.add(String.format("%02d:%02d", hour, minute));
            }
        }
        TimeEvent.getItems().setAll(timeOptions);
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
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
