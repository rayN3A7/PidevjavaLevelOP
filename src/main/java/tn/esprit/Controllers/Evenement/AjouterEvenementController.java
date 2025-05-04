package tn.esprit.Controllers.Evenement;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Categorieevent;
import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.Services.Evenement.CategorieEvService;
import tn.esprit.Services.Evenement.EvenementService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AjouterEvenementController {
    EvenementService es = new EvenementService();
    CategorieEvService ces = new CategorieEvService();
    @FXML
    private TextField NomEvent,LieuEvent,NBPEvent;
    @FXML
    private DatePicker DateEvent;
    @FXML
    private ComboBox<String> TimeEvent;
    @FXML
    private ComboBox<String> CatEvent;
    @FXML
    private Label imageLabel;

    private File selectedFile;
    private static final String IMAGE_DIR = "C:\\xampp\\htdocs\\img\\";
    private static final String IMAGE_URL = System.getenv("IMG_UPLOAD_PATH") != null 
        ? System.getenv("IMG_UPLOAD_PATH") 
        : "http://localhost/img/";
    @FXML
    private void initialize() {
        GetCategorie();
        fillTimeComboBox();
    }
    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            imageLabel.setText(selectedFile.getName());
        }
    }

    private void GetCategorie(){
        List<Categorieevent> categorieList = ces.getAll();
        List<String> categories = categorieList.stream()
                .map(Categorieevent::getNom)
                .toList();
        CatEvent.getItems().setAll(categories);
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
    private void AjouterEvenemnt() {
        try {
            if (!validateFields()) {
                return;
            }

            String nom_event = NomEvent.getText().trim();
            String lieu_event = LieuEvent.getText().trim();
            int max_places_event = Integer.parseInt(NBPEvent.getText().trim());

            String selectedDate = DateEvent.getValue().toString();
            String selectedTime = TimeEvent.getValue() + ":00";
            String dateTimeString = selectedDate + " " + selectedTime;
            Timestamp dateTime = Timestamp.valueOf(dateTimeString);

            if (dateTime.before(new Timestamp(System.currentTimeMillis()))) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La date de l'événement doit être dans le futur.");
                return;
            }

            String categorie_event = CatEvent.getValue();
            int categorie_id = ces.getIdCategorieEvent(categorie_event);

            String imageName = null;
            if (selectedFile != null) {
                imageName = selectedFile.getName();
                File destinationFile = new File(IMAGE_DIR + imageName);
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            Evenement evenement = new Evenement(categorie_id, max_places_event, nom_event, lieu_event, dateTime, imageName);
            es.add(evenement);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez vérifier les champs.");
        }
    }

    private boolean validateFields() {
        // Vérifier si le nom et le lieu sont vides
        if (NomEvent.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom ne peuvent pas être vides.");
            return false;
        }
        if (LieuEvent.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le lieu ne peuvent pas être vides.");
            return false;
        }

        try {
            int max_places_event = Integer.parseInt(NBPEvent.getText().trim());
            if (max_places_event <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le nombre de places doit être supérieur à zéro.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un nombre valide pour les places.");
            return false;
        }

        return true;
    }

    @FXML
    private void Annuler(ActionEvent event){
        NomEvent.clear();
        LieuEvent.clear();
        NBPEvent.clear();
        DateEvent.getEditor().clear();
        CatEvent.getSelectionModel().clearSelection();
        TimeEvent.getSelectionModel().clearSelection();
    }
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    public void ButtonListeEvenement(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListeEvenementAdmin.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }

}
