package tn.esprit.Controllers.Produit;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.param.ChargeCreateParams;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import tn.esprit.Models.Produit;
import tn.esprit.Models.Stock;
import tn.esprit.Services.StockService;

import java.util.regex.Pattern;

public class PaymentFormController {
    @FXML private TextField emailField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvcField;
    @FXML private TextField cardholderNameField;
    @FXML private Button payButton;
    @FXML private Button cancelButton;
    @FXML private Label paymentStatus;

    private Stage dialogStage;
    private Produit currentProduct;
    private Stock currentStock;
    private StockService stockService;
    private CommandeConfirmationController confirmationController;
    private boolean paymentSuccessful = false;

    static {
        Stripe.apiKey = "sk_test_51QvMH5PNauIHPjoTTov10mAdNwhbSH0ycAHTkArf2taZUSP5rtsMNxgyehsKnq4dfoazZz1nXkGNrQQn4uzSxZBt00pANi7uFX";
    }

    @FXML
    public void initialize() {
        stockService = new StockService();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setData(Produit product, Stock stock) {
        this.currentProduct = product;
        this.currentStock = stock;
    }

    public void setConfirmationController(CommandeConfirmationController controller) {
        this.confirmationController = controller;
    }

    public boolean isPaymentSuccessful() {
        return paymentSuccessful;
    }

    @FXML
    private void handlePay() {
        if (!validateInput()) {
            paymentStatus.setText("Essayer une autre fois");
            return;
        }

        try {
            String testToken = "tok_visa";
            ChargeCreateParams params = ChargeCreateParams.builder()
                    .setAmount((long) (currentStock.getPrixProduit() * 100))
                    .setCurrency("usd")
                    .setSource(testToken)
                    .setDescription("Payment for " + currentProduct.getNomProduit())
                    .setReceiptEmail(emailField.getText())
                    .build();

            Charge charge = Charge.create(params);
            if (charge.getStatus().equals("succeeded")) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Paiement effectué avec succès!");

                DialogPane dialogPane = successAlert.getDialogPane();
                dialogPane.getStylesheets().add(getClass().getResource("/assets/style/styles.css").toExternalForm());
                dialogPane.getStyleClass().add("custom-alert");

                Stage stage = (Stage) dialogPane.getScene().getWindow();
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/forumUI/icons/sucessalert.png")));

                successAlert.showAndWait();

                paymentSuccessful = true;
                dialogStage.close();
            } else {
                paymentStatus.setText("Payment failed: " + charge.getFailureMessage());
                paymentSuccessful = false;
            }
        } catch (StripeException e) {
            paymentStatus.setText("Error processing payment: " + e.getMessage());
            paymentSuccessful = false;
        }
    }

    @FXML
    private void handleCancel() {
        paymentSuccessful = false;
        dialogStage.close();
    }

    private boolean validateInput() {
        String email = emailField.getText().trim();
        String cardNumber = cardNumberField.getText().replaceAll("\\s", "").trim();
        String expiry = expiryField.getText().trim();
        String cvc = cvcField.getText().trim();
        String cardholderName = cardholderNameField.getText().trim();

        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!Pattern.matches(emailRegex, email)) {
            return false;
        }

        if (!cardNumber.matches("\\d{16}")) {
            return false;
        }

        if (!expiry.matches("^(0[1-9]|1[0-2])\\/([0-9]{2})$")) {
            return false;
        }

        if (!cvc.matches("\\d{3,4}")) {
            return false;
        }

        if (cardholderName.isEmpty()) {
            return false;
        }

        return true;
    }
}