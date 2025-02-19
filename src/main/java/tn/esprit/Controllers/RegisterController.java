package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;
import java.util.regex.Pattern;

public class RegisterController {
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private TextField txtNumero;
    @FXML private TextField txtPassword;
    @FXML private TextField txtConfirmPassword;

    @FXML private Label lblNomError;
    @FXML private Label lblPrenomError;
    @FXML private Label lblEmailError;
    @FXML private Label lblUsernameError;
    @FXML private Label lblNumeroError;
    @FXML private Label lblPasswordError;
    @FXML private Button btnGoToLogin;
    @FXML private Button btnRegister;

    private final UtilisateurService userService = new UtilisateurService();

    @FXML
    private void initialize() {
        btnRegister.setOnAction(event -> handleRegister());
        btnGoToLogin.setOnAction(event -> navigateToLoginPage());
    }

    private void handleRegister() {
        boolean isValid = true;

        // Vérification du nom
        String nom = txtNom.getText().trim();
        if (nom.isEmpty()) {
            lblNomError.setText("Nom requis !");
            isValid = false;
        } else {
            lblNomError.setText("");
        }

        // Vérification du prénom
        String prenom = txtPrenom.getText().trim();
        if (prenom.isEmpty()) {
            lblPrenomError.setText("Prénom requis !");
            isValid = false;
        } else {
            lblPrenomError.setText("");
        }

        // Vérification de l'email
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            lblEmailError.setText("Email requis !");
            isValid = false;
        } else if (!isValidEmail(email)) {
            lblEmailError.setText("Format d'email invalide !");
            isValid = false;
        } else if (userService.emailExists(email)) {
            lblEmailError.setText("Email déjà utilisé !");
            isValid = false;
        } else {
            lblEmailError.setText("");
        }

        // Vérification du nom d'utilisateur
        String username = txtUsername.getText().trim();
        if (username.isEmpty()) {
            lblUsernameError.setText("Nom d'utilisateur requis !");
            isValid = false;
        } else if (userService.nicknameExists(username)) {
            lblUsernameError.setText("Nom d'utilisateur déjà pris !");
            isValid = false;
        } else {
            lblUsernameError.setText("");
        }

        // Vérification du numéro
        int numero = 0;
        String numStr = txtNumero.getText().trim();
        if (numStr.isEmpty()) {
            lblNumeroError.setText("Numéro requis !");
            isValid = false;
        } else {
            try {
                numero = Integer.parseInt(numStr);
                lblNumeroError.setText("");
            } catch (NumberFormatException e) {
                lblNumeroError.setText("Numéro invalide !");
                isValid = false;
            }
        }

        // Vérification du mot de passe
        String password = txtPassword.getText();
        if (password.isEmpty()) {
            lblPasswordError.setText("Mot de passe requis !");
            isValid = false;
        } else if (!isValidPassword(password)) {
            lblPasswordError.setText("Doit contenir 8+ caractères, une majuscule, une minuscule, un chiffre et un caractère spécial !");
            isValid = false;
        } else {
            lblPasswordError.setText("");
        }

        // Vérification de la confirmation du mot de passe
        if (!password.equals(txtConfirmPassword.getText())) {
            lblPasswordError.setText("Les mots de passe ne correspondent pas !");
            isValid = false;
        }

        // Si tout est valide, création et enregistrement de l'utilisateur
        if (isValid) {
            Utilisateur newUser = new Utilisateur(email, password, username, nom, numero, prenom, Role.CLIENT);
            userService.add(newUser);
            System.out.println("Utilisateur inscrit avec succès !");
            navigateToLoginPage();
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return Pattern.matches(passwordRegex, password);
    }

    private void navigateToLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion Utilisateur/Login/Login.fxml"));
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            lblPrenomError.setText("Impossible de charger la page d'accueil.");
            e.printStackTrace();
        }
    }
}
