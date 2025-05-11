package tn.esprit.Controllers.Coach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private static final String IMAGE_DIR = "C:\\xampp\\htdocs\\img\\"; // Chemin XAMPP

    @FXML
    private void addSession() {
        try {
            String game = gameField.getText();
            String priceText = priceField.getText();
            if (priceText.isEmpty()) {
                showAlert("Erreur", "Le prix ne peut pas être vide");
                return;
            }
            double price = Double.parseDouble(priceText);
            String duration = durationField.getText();

            if (roleuser.equals("COACH")) {
                if (selectedImageName != null && !selectedImageName.isEmpty()) {

                    saveImageToServer();
                }
                Session_game session = new Session_game(price, new Date(), duration, game, currentCoachId, selectedImageName);
                serviceSession.add(session);
                clearFields();
                showSuccessAlert("Succès", "Session ajoutée avec succès");
                backToManagement(null);
            } else {
                showAlert("Erreur", "Seul un coach peut ajouter une session");
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un prix valide");
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de la sauvegarde de l'image");
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
                imageUrlField.setText(selectedFile.getAbsolutePath());
                Image image = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(image);
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de lire le fichier image sélectionné");
                e.printStackTrace();
            }
        }
    }
    private void showAlert(String title, String message) {
        showStyledAlert(title, message, "/forumUI/icons/alert.png", "/forumUI/icons/alert.png", "OK", 80, 80);
    }

    private void showSuccessAlert(String title, String message) {
        showStyledAlert(title, message, "/forumUI/icons/sucessalert.png", "/forumUI/icons/sucessalert.png", "OK", 60, 80);
    }
    private void showStyledAlert (String title, String message, String iconPath, String stageIconPath,
                                  String buttonText, double iconHeight, double iconWidth) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(new Image(getClass().getResource(iconPath).toExternalForm()));
        icon.setFitHeight(iconHeight);
        icon.setFitWidth(iconWidth);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource(stageIconPath).toExternalForm()));

        ButtonType okButton = new ButtonType(buttonText, ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        alert.showAndWait();
    }
}