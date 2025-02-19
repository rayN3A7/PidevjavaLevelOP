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

import java.io.IOException;

public class GetCodeController {

    @FXML
    private Button btnVerifCode;

    @FXML
    private Label lblError;

    @FXML
    private Label lblOblier;

    @FXML
    private TextField txtCode;

    @FXML
    void handleCode(ActionEvent event) {
        String userEnteredOtp = txtCode.getText().trim(); // Get the OTP entered by the user
        String generatedOtp = EmailService.getGeneratedOtp(); // Retrieve the generated OTP

        // Verify the OTP
        if (userEnteredOtp.equals(generatedOtp)) {
            navigateToLoginPage();
            lblError.setText("Code correct !");
            lblError.setStyle("-fx-text-fill: green;");

            // Navigate to the next screen (e.g., reset password screen)
            // You can add navigation logic here
        } else {
            lblError.setText("Code incorrect. Veuillez réessayer.");
            lblError.setStyle("-fx-text-fill: red;");
        }
    }

    private void navigateToLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion Utilisateur/ReinitialiserPw/ReinitialiserPw.fxml"));
            Stage stage = (Stage) btnVerifCode.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            lblError.setText("Impossible de charger la page d'accueil.");
            e.printStackTrace();
        }
    }

}
