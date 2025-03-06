package tn.esprit.Controllers.Coach;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

            Stripe.apiKey = "sk_test_51QwnFBQo8eHPYc0vBA9m5i0nBxdhefRpQrwYyk8VAPRg21d2UKSRiUJsR7T7VIFAlyeiDuQaZRwSCeQveeOETK9q00BDEThZIx";


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


            showAlert("Paiement réussi",
                    "Le paiement a été traité avec succès."
            );


            closeWindow();

        } catch (StripeException e) {
            showAlert("Erreur de paiement",
                    "Une erreur est survenue lors du traitement du paiement : " + e.getMessage()
            );
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
                    "Veuillez remplir tous les champs."
            );
            return false;
        }


        if (!cardNumberField.getText().matches("\\d{16}")) {
            showAlert("Numéro de carte invalide",
                    "Le numéro de carte doit contenir 16 chiffres."
            );
            return false;
        }


        if (!expiryDateField.getText().matches("\\d{2}/\\d{2}")) {
            showAlert("Date d'expiration invalide",
                    "La date d'expiration doit être au format MM/YY."
            );
            return false;
        }


        if (!cvvField.getText().matches("\\d{3,4}")) {
            showAlert("CVV invalide",
                    "Le CVV doit contenir 3 ou 4 chiffres."
            );
            return false;
        }

        return true;
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

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}