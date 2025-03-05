package tn.esprit.Controllers.Utilisateur;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoginController {

    @FXML
    private Button btnConx;

    @FXML
    private Button lblOblier;

    @FXML
    private CheckBox remamber;

    @FXML
    private PasswordField txtPsw;

    @FXML
    private TextField txtUseName;

    @FXML
    private Button btnRegister;

    @FXML
    private Label lblError;
    @FXML
    private Button btnRejoignez;

    private final UtilisateurService userService = new UtilisateurService(); // Service pour gérer les utilisateurs

    @FXML
    private void initialize() {
        // Check if user is already authenticated
        if (SessionManager.getInstance().isLoggedIn()) {
            Platform.runLater(this::navigateToHome2);
            return;
        }

        btnRegister.setOnAction(event -> navigateToRegister());
        lblOblier.setOnAction(event -> navigateToGetmpPage());
    }

    @FXML
    void handleLogin(ActionEvent event) {
        lblError.setText("");
        String email = txtUseName.getText().trim();
        String password = txtPsw.getText();
        boolean rememberMe = remamber.isSelected();

        if (email.isEmpty() || password.isEmpty()) {
            lblError.setText("Veuillez remplir tous les champs.");
            return;
        }

        Utilisateur user = userService.getByEmail(email);
        if (user != null) {
            // Check if user is banned
            if (user.isBan()) {
                LocalDateTime banTime = user.getBanTime();
                String banMessage = "Votre compte a été banni";
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                if (banTime != null) {
                    banMessage += " jusqu'au " + banTime.format(formatter);
                } else {
                    banMessage += " de manière permanente";
                }
                showAlert("Compte banni", banMessage);
                return;
            }
        }

        Utilisateur u=null;
        if (userService.loginUser(email, password, rememberMe)) {
            u = userService.getByEmail(email);
            if(u.getRole().equals(Role.CLIENT)||u.getRole().equals(Role.COACH))
                navigateToHome();
            else{
                navigateToDash();
            }
        } else {
            lblError.setText("Email ou mot de passe incorrect.");
        }
    }
    private void navigateToDash() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashadmin.fxml"));
            Stage stage = (Stage) btnConx.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            lblError.setText("Impossible de charger la page d'accueil.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(new Image(getClass().getResource("/forumUI/icons/alert.png").toExternalForm()));
        icon.setFitHeight(80);
        icon.setFitWidth(80);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/alert.png").toString()));

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        alert.showAndWait();
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) btnConx.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            lblError.setText("Impossible de charger la page d'accueil.");
            e.printStackTrace();
        }
    }

    private void navigateToGetmpPage() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestion Utilisateur/MotPasseOublier/pswOublier.fxml"));
            Stage stage = (Stage) lblOblier.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            lblError.setText("Impossible de charger la page d'accueil.");
            e.printStackTrace();
        }
    }

    private void navigateToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion Utilisateur/Register/Register.fxml"));
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            lblError.setText("Impossible de charger la page d'accueil.");
            e.printStackTrace();
        }
    }

    private void navigateToHome2() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) txtUseName.getScene().getWindow(); // Récupérer la fenêtre actuelle
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            lblError.setText("Impossible de charger la page d'accueil.");
            e.printStackTrace();
        }
    }


}
