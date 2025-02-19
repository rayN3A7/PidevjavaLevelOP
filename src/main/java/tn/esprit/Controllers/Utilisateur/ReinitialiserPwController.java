package tn.esprit.Controllers.Utilisateur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;
import java.util.regex.Pattern;

import static tn.esprit.Controllers.Utilisateur.PswSenderController.emailsta;

public class ReinitialiserPwController {

    @FXML
    private Button btnGoToLogin;

    @FXML
    private Button btnRegister;

    @FXML
    private Label lblPasswordError;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private PasswordField txtPassword;

    UtilisateurService us =new UtilisateurService();


    @FXML
    void GotoLogin(ActionEvent event) {

    }

    @FXML
    void btnConfirmer(ActionEvent event) {

        boolean isValid = true;
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

            us.updatePassword(emailsta,password);
            System.out.println("Utilisateur inscrit avec succès !");
            navigateToLoginPage();
        }

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
            lblPasswordError.setText("Impossible de charger la page d'accueil.");
            e.printStackTrace();
        }
    }



}
