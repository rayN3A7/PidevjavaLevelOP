package tn.esprit.Controllers.Produit;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.Models.Commande;
import tn.esprit.Models.Produit;
import tn.esprit.Models.Stock;
import tn.esprit.Services.CommandeService;
import tn.esprit.Services.StockService;
import tn.esprit.utils.SessionManager;
import java.net.URI;

public class CommandeConfirmationController {
    @FXML private Label productNameLabel;
    @FXML private Label priceLabel;

    private Produit produit;
    private Stock stock;
    private CommandeService commandeService;
    private StockService stockService;
    private Stage dialogStage;
    private boolean validateClicked = false;
    private final int DEFAULT_USER_ID = SessionManager.getInstance().getUserId();
    private Commande currentCommande;

    @FXML
    public void initialize() {
        commandeService = new CommandeService();
        stockService = new StockService();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setData(Produit produit, Stock stock) {
        this.produit = produit;
        this.stock = stock;

        productNameLabel.setText(produit.getNomProduit());
        priceLabel.setText(stock.getPrixProduit() + " DNT");

        // Créer une nouvelle commande avec le statut initial
        createInitialCommande();
    }

    private void createInitialCommande() {
        try {
            currentCommande = new Commande();
            currentCommande.setUtilisateurId(DEFAULT_USER_ID);
            currentCommande.setProduitId(produit.getId());
            currentCommande.setStatus("en cours");

            commandeService.add(currentCommande);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la création de la commande: " + e.getMessage());
        }
    }

    public boolean isValidateClicked() {
        return validateClicked;
    }

    @FXML
    private void handleValidate() {
        if (stock == null || stock.getQuantity() <= 0) {
            updateCommandeStatus("annulé");
            showAlert(AlertType.ERROR, "Erreur", "Désolé, ce produit est en rupture de stock!");
            return;
        }

        try {
            // Create a Stripe Checkout Session
            Stripe.apiKey = "sk_test_51QvMH5PNauIHPjoTTov10mAdNwhbSH0ycAHTkArf2taZUSP5rtsMNxgyehsKnq4dfoazZz1nXkGNrQQn4uzSxZBt00pANi7uFX";

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:8080/payment/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:8080/payment/cancel")
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount((long) (stock.getPrixProduit() * 100)) // Convert to cents
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(produit.getNomProduit())
                                            .build())
                                    .build())
                            .setQuantity(1L)
                            .build())
                    .build();

            Session session = Session.create(params);

            // Open the Stripe Checkout page in the default browser
            java.awt.Desktop.getDesktop().browse(new URI(session.getUrl()));

            // Update order status to pending payment
            currentCommande.setStatus("pending_payment");
            commandeService.update(currentCommande);

            validateClicked = true;
            dialogStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            updateCommandeStatus("annulé");
            showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la création du paiement: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        updateCommandeStatus("annulé");
        dialogStage.close();
    }

    private void updateCommandeStatus(String status) {
        try {
            if (currentCommande != null) {
                currentCommande.setStatus(status);
                commandeService.update(currentCommande);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du statut de la commande: " + e.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}