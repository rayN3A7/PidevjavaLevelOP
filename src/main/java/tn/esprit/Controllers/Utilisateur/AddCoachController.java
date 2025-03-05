package tn.esprit.Controllers.Utilisateur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.esprit.Models.Demande;
import tn.esprit.Models.Games;
import tn.esprit.Services.DemandeService;
import tn.esprit.Services.GamesService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class AddCoachController {

    private static final String UPLOAD_DIR = "C:\\xampp\\htdocs\\img\\";

    @FXML
    private Button btnGoToLogin;
    @FXML
    private Label lblCVStatus;
    @FXML
    private Label lblError;
    @FXML
    private Button btnUploadCV;
    @FXML
    private ComboBox<String> cbgames;
    @FXML
    private Button btnGoToHome;
    @FXML
    private TextArea txtDescription;

    private File selectedFile;

    private GamesService gm = new GamesService();
    private DemandeService demandeService = new DemandeService();

    @FXML
    public void initialize() {
        populateGamesComboBox();
        btnGoToHome.setOnAction(event -> navigateToHome2());

        // Initialize error label
        if (lblError != null) {
            lblError.setVisible(false);
            lblError.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 12px;");
        }
    }

    private void populateGamesComboBox() {
        List<Games> gamesList = gm.getAll();
        cbgames.getItems().clear();
        for (Games game : gamesList) {
            cbgames.getItems().add(game.getGame_name());
        }
        if (!cbgames.getItems().isEmpty()) {
            cbgames.getSelectionModel().selectFirst();
        }
    }

    @FXML
    void handleUploadCV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setTitle("Sélectionnez un fichier CV");

        Stage stage = (Stage) btnUploadCV.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            lblCVStatus.setText("Fichier sélectionné : " + selectedFile.getName());
            lblCVStatus.setStyle("-fx-text-fill: green;");
            hideError();
        } else {
            lblCVStatus.setText("Aucun fichier sélectionné");
            lblCVStatus.setStyle("-fx-text-fill: #ff4444;");
        }
    }

    private boolean validateFields() {
        if (txtDescription.getText().trim().isEmpty() && selectedFile == null) {
            showError("Veuillez remplir la description et télécharger votre CV");
            return false;
        }
        if (txtDescription.getText().trim().isEmpty()) {
            showError("Veuillez remplir la description");
            return false;
        }
        if (selectedFile == null) {
            showError("Veuillez télécharger votre CV");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        }
    }

    private void hideError() {
        if (lblError != null) {
            lblError.setVisible(false);
        }
    }

    @FXML
    void handleSubmit(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        try {
            // Generate unique filename with timestamp
            String uniqueFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
            String filePath = UPLOAD_DIR + uniqueFileName;

            // Create Demande object with the file path
            Demande demande = new Demande(
                    SessionManager.getInstance().getUserId(),
                    cbgames.getValue(),
                    txtDescription.getText().trim(),
                    uniqueFileName // Store only the filename, not full path
            );
            demande.setDate(new Timestamp(System.currentTimeMillis()));

            // Copy file to destination and add demande
            byte[] fileData = Files.readAllBytes(selectedFile.toPath());
            demandeService.add(demande, fileData);

            System.out.println("Demande submitted successfully!");
            navigateToHome();
        } catch (IOException e) {
            showError("Error saving file: " + e.getMessage());
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) ((btnGoToLogin != null && btnGoToLogin.getScene() != null)
                    ? btnGoToLogin.getScene().getWindow()
                    : Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null));

            if (stage == null) {
                System.out.println("Error: Could not determine the active stage.");
                return;
            }

            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            System.out.println("Error loading Home.fxml");
            e.printStackTrace();
        }
    }

    private void navigateToHome2() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) btnGoToHome.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            System.out.println("errorr");
            e.printStackTrace();
        }
    }
}
