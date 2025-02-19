package tn.esprit.Controllers.Utilisateur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;

public class LoginController {

    @FXML
    private Button btnConx;

    @FXML
    private Button lblOblier;


    @FXML
    private CheckBox remamber;

    @FXML
    private PasswordField  txtPsw;

    @FXML
    private TextField txtUseName;

    @FXML
    private Button btnRegister;

    @FXML
    private Label lblError;

    private final UtilisateurService userService = new UtilisateurService(); // Service pour gérer les utilisateurs


    @FXML
    private void initialize() {
        btnRegister.setOnAction(event -> navigateToRegister());
        lblOblier.setOnAction(event -> navigateToGetmpPage());
    }


    @FXML
    void handleLogin(ActionEvent event) {
        lblError.setText(""); // Clear previous errors
        String email = txtUseName.getText().trim();
        String password = txtPsw.getText();
        boolean rememberMe = remamber.isSelected();

        if (email.isEmpty() || password.isEmpty()) {
            lblError.setText("Veuillez remplir tous les champs.");
            return;
        }

        if (userService.loginUser(email, password, rememberMe)) {
            navigateToHome();
        } else {
            lblError.setText("Email ou mot de passe incorrect.");
        }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion Utilisateur/MotPasseOublier/pswOublier.fxml"));
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
}
