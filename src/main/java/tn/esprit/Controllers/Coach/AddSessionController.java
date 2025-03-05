package tn.esprit.Controllers.Coach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceSession;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class AddSessionController {

    @FXML private TextField gameField;
    @FXML private TextField priceField;
    @FXML private TextField durationField;
    @FXML private TextField imageUrlField;
    @FXML private Button selectImageButton;
    @FXML private ImageView imagePreview;

    private final ServiceSession serviceSession = new ServiceSession();
    private final int currentCoachId = SessionManager.getInstance().getUserId();
    private final String roleuser = SessionManager.getInstance().getRole().name();
    private String selectedImageName;
    private static final String IMAGE_DIR = "C:\\xampp\\htdocs\\img\\games\\"; // Chemin XAMPP

    @FXML
    private void addSession() {
        try {
            String game = gameField.getText();
            String priceText = priceField.getText();
            if (priceText.isEmpty()) {
                showAlert("Erreur", "Le prix ne peut pas être vide", Alert.AlertType.ERROR);
                return;
            }
            double price = Double.parseDouble(priceText);
            String duration = durationField.getText();

            if (roleuser.equals("COACH")) {
                if (selectedImageName != null && !selectedImageName.isEmpty()) {
                    // Sauvegarder l'image dans htdocs/ima
                    saveImageToServer();
                }
                Session_game session = new Session_game(price, new Date(), duration, game, currentCoachId, selectedImageName);
                serviceSession.add(session);
                clearFields();
                showAlert("Succès", "Session ajoutée avec succès", Alert.AlertType.INFORMATION);
                backToManagement(null);
            } else {
                showAlert("Erreur", "Seul un coach peut ajouter une session", Alert.AlertType.WARNING);
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un prix valide", Alert.AlertType.ERROR);
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de la sauvegarde de l'image", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void saveImageToServer() throws IOException {
        File selectedFile = new File(imageUrlField.getText());
        Path destination = Paths.get(IMAGE_DIR + selectedImageName);
        Files.copy(selectedFile.toPath(), destination);
    }

    private void clearFields() {
        gameField.clear();
        priceField.clear();
        durationField.clear();
        imageUrlField.clear();
        imagePreview.setImage(null);
        selectedImageName = null;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void backToManagement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/SessionManagement.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = event == null ? (Stage) gameField.getScene().getWindow() : (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(gameField.getScene().getWindow());
        if (selectedFile != null) {
            try {
                selectedImageName = selectedFile.getName();
                imageUrlField.setText(selectedFile.getAbsolutePath()); // Afficher le chemin complet temporairement
                Image image = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(image);
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de lire le fichier image sélectionné", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }
}