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
import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class AddCoachController {

    @FXML
    private Button btnGoToLogin;
    @FXML
    private Label lblCVStatus;

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
        // Call the method to populate the ComboBox
        populateGamesComboBox();
        btnGoToHome.setOnAction(event -> navigateToHome2());
    }

    private void populateGamesComboBox() {
        // Retrieve the list of games from the database
        List<Games> gamesList = gm.getAll();

        // Clear the ComboBox (in case it already has items)
        cbgames.getItems().clear();

        // Add each game's name to the ComboBox
        for (Games game : gamesList) {
            cbgames.getItems().add(game.getGame_name());
        }

        // Optionally, set a default selection
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
        } else {
            lblCVStatus.setText("Aucun fichier sélectionné");
        }
    }

    @FXML
    void handleSubmit(ActionEvent event) {
        if (selectedFile != null) {
            try {
                // Read the file content into a byte array
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());

                // Create a new Demande object
                Demande demande = new Demande(
                        SessionManager.getInstance().getUserId(),
                        cbgames.getValue(),
                        txtDescription.getText(),
                        fileContent
                );
                demande.setDate(new Timestamp(System.currentTimeMillis()));

                // Add the demande to the database
                demandeService.add(demande);



                System.out.println("Demande submitted successfully!");
                navigateToHome();
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        } else {
            System.out.println("No file selected!");
        }
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) ((btnGoToLogin != null && btnGoToLogin.getScene() != null) ?
                    btnGoToLogin.getScene().getWindow() :
                    Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null));

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