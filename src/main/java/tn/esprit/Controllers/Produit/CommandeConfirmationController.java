package tn.esprit.Controllers.Produit;

import java.net.URI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.Models.Commande;
import tn.esprit.Models.Produit;
import tn.esprit.Models.Stock;
import tn.esprit.Services.CommandeService;
import tn.esprit.Services.EmailService;
import tn.esprit.Services.StockService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

public class CommandeConfirmationController {
    @FXML private Label productNameLabel;
    @FXML private Label priceLabel;

    private Produit produit;
    private Stock stock;
    private CommandeService commandeService;
    private StockService stockService;
    private UtilisateurService utilisateurService;
    private Stage dialogStage;
    private boolean validateClicked = false;
    private final int DEFAULT_USER_ID = SessionManager.getInstance().getUserId();
    private Commande currentCommande;

    @FXML
    public void initialize() {
        commandeService = new CommandeService();
        stockService = new StockService();
        utilisateurService = new UtilisateurService();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setData(Produit produit, Stock stock) {
        this.produit = produit;
        this.stock = stock;

        productNameLabel.setText(produit.getNomProduit());
        priceLabel.setText(stock.getPrixProduit() + " DNT");

        createInitialCommande();
    }

    private void createInitialCommande() {
        try {
            if (currentCommande == null) {
                currentCommande = new Commande();
                currentCommande.setUtilisateurId(DEFAULT_USER_ID);
                currentCommande.setProduitId(produit.getId());
                currentCommande.setStatus("en cours");

                commandeService.add(currentCommande);

                if (currentCommande.getId() == 0) {
                    throw new Exception("Failed to create command - no ID was generated");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la création de la commande : " + e.getMessage());
        }
    }

    public boolean isValidateClicked() {
        return validateClicked;
    }

    @FXML
    private void handleValidate() {
        if (stock == null || stock.getQuantity() <= 0) {
            updateCommandeStatus("annulé");
            showAlert(AlertType.ERROR, "Erreur", "Désolé, ce produit est en rupture de stock !");
            return;
        }

        try {
            updateCommandeStatus("en cours de paiement");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/payment-form.fxml"));
            Parent paymentDialog = loader.load();

            Stage paymentStage = new Stage();
            paymentStage.setTitle("Paiement");
            paymentStage.initModality(Modality.WINDOW_MODAL);
            paymentStage.initOwner(dialogStage);
            paymentStage.setScene(new Scene(paymentDialog));

            PaymentFormController controller = loader.getController();
            controller.setDialogStage(paymentStage);
            controller.setData(produit, stock);
            controller.setConfirmationController(this);

            paymentStage.showAndWait();

            if (controller.isPaymentSuccessful()) {
                updateCommandeStatus("terminé");
                stock.setQuantity(stock.getQuantity() - 1);
                stockService.update(stock);
                validateClicked = true;

                String userEmail = utilisateurService.getEmail(DEFAULT_USER_ID);
                if (userEmail == null) {
                    throw new Exception("Email not found for user ID: " + DEFAULT_USER_ID);
                }
                String nickname = utilisateurService.getNickname(DEFAULT_USER_ID);
                if (nickname == null) {
                    throw new Exception("Nickname not found for user ID: " + DEFAULT_USER_ID);
                }

                String platform = "PC";
                EmailService.sendPurchaseConfirmationEmail(userEmail, nickname, produit.getNomProduit(), platform);
                showAlert(AlertType.INFORMATION, "Succès", "Paiement effectué ! Clé d'activation envoyée à " + userEmail);

                dialogStage.close();
            } else {
                updateCommandeStatus("annulé");
                showAlert(AlertType.ERROR, "Erreur", "Paiement annulé ou échoué.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            updateCommandeStatus("annulé");
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors du paiement : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        updateCommandeStatus("annulé");
        dialogStage.close();
    }

    private void updateCommandeStatus(String status) {
        if (currentCommande != null) {
            currentCommande.setStatus(status);
            commandeService.update(currentCommande);
        }
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}