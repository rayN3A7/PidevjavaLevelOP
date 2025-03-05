package tn.esprit.Controllers.Coach;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class StripePaymentFormController {

    @FXML
    private TextField cardNumberField;
    @FXML
    private TextField expiryDateField;
    @FXML
    private TextField cvvField;
    @FXML
    private TextField nameOnCardField;
    @FXML
    private Label amountLabel;
    @FXML
    private Button cancelButton;
    @FXML
    private Button payButton;

    private double amount;
    private int reservationId;

    public void initData(int reservationId, double amount) {
        this.reservationId = reservationId;
        this.amount = amount;
        amountLabel.setText(String.format("Montant total: %.2f €", amount));
    }

    @FXML
    private void handlePayment() {
        if (!validateFields()) {
            return;
        }

        try {
            // Initialize Stripe with your secret key
            Stripe.apiKey = "sk_test_51QwnFBQo8eHPYc0vBA9m5i0nBxdhefRpQrwYyk8VAPRg21d2UKSRiUJsR7T7VIFAlyeiDuQaZRwSCeQveeOETK9q00BDEThZIx";

            // Create a PaymentIntent with customer and payment method details
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (amount * 100))
                    .setCurrency("eur")
                    .setDescription("Réservation #" + reservationId)
                    .putMetadata("customer_name", nameOnCardField.getText())
                    .putMetadata("reservation_id", String.valueOf(reservationId))
                    .setPaymentMethod("pm_card_visa")
                    .setConfirm(true)
                    .addPaymentMethodType("card")
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Show success message
            showAlert("Paiement réussi", 
                    "Le paiement a été traité avec succès.", 
                    Alert.AlertType.INFORMATION);

            // Close the payment form
            closeWindow();

        } catch (StripeException e) {
            showAlert("Erreur de paiement", 
                    "Une erreur est survenue lors du traitement du paiement : " + e.getMessage(), 
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateFields() {
        if (cardNumberField.getText().isEmpty() ||
            expiryDateField.getText().isEmpty() ||
            cvvField.getText().isEmpty() ||
            nameOnCardField.getText().isEmpty()) {
            
            showAlert("Champs manquants", 
                    "Veuillez remplir tous les champs.", 
                    Alert.AlertType.WARNING);
            return false;
        }

        // Validate card number (should be 16 digits)
        if (!cardNumberField.getText().matches("\\d{16}")) {
            showAlert("Numéro de carte invalide", 
                    "Le numéro de carte doit contenir 16 chiffres.", 
                    Alert.AlertType.WARNING);
            return false;
        }

        // Validate expiry date (should be MM/YY format)
        if (!expiryDateField.getText().matches("\\d{2}/\\d{2}")) {
            showAlert("Date d'expiration invalide", 
                    "La date d'expiration doit être au format MM/YY.", 
                    Alert.AlertType.WARNING);
            return false;
        }

        // Validate CVV (should be 3 or 4 digits)
        if (!cvvField.getText().matches("\\d{3,4}")) {
            showAlert("CVV invalide", 
                    "Le CVV doit contenir 3 ou 4 chiffres.", 
                    Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}