package tn.esprit.Controllers.Utilisateur;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Services.EmailService;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;

public class PswSenderController {

    @FXML
    private Button btnSendCode;

    @FXML
    private Label lblError;

    @FXML
    private Label lblOblier;

    @FXML
    private TextField txtEmail;

    UtilisateurService us = new UtilisateurService();
    EmailService em = new EmailService();
    public static String emailsta;

    @FXML
    void handleCode(ActionEvent event) {
        if (txtEmail == null) {
            System.out.println("txtEmail is NULL! Check FXML binding.");
            return;
        }
        String email = txtEmail.getText().trim(); // Get the email from the text field

        // Check if the email exists in the database
        if (us.emailExists(email)) {
            // Generate OTP
            String otp = em.generateOtp();

            // Send the OTP to the user's email
            em.sendOtpEmail(email, otp);
            emailsta=email;
            navigateToResiveCodePage();

            // Display success message
            lblError.setText("Un code de réinitialisation a été envoyé à votre adresse email.");
            lblError.setStyle("-fx-text-fill: green;");
        } else {
            // Display error message if the email does not exist
            lblError.setText("L'adresse email n'existe pas dans notre système.");
            lblError.setStyle("-fx-text-fill: red;");
        }
    }

    private void navigateToResiveCodePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion Utilisateur/MotPasseOublier/ObtenirCode.fxml"));
            Stage stage = (Stage) btnSendCode.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            lblError.setText("Impossible de charger la page d'accueil.");
            e.printStackTrace();
        }
    }

}
