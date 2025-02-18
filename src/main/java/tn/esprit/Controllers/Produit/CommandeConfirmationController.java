package tn.esprit.Controllers.Produit;

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
        if (stock.getQuantity() <= 0) {
            updateCommandeStatus("annulé");
            showAlert(AlertType.ERROR, "Erreur", "Désolé, ce produit est en rupture de stock!");
            return;
        }

        try {
            // Mettre à jour le statut de la commande à "terminé"
            currentCommande.setStatus("terminé");
            commandeService.update(currentCommande);

            // Mettre à jour la quantité en stock
            stock.setQuantity(stock.getQuantity() - 1);
            stockService.update(stock);

            validateClicked = true;
            dialogStage.close();
            
            showAlert(AlertType.INFORMATION, "Succès", "Votre commande a été validée avec succès! Merci de votre achat.");
        } catch (Exception e) {
            e.printStackTrace();
            updateCommandeStatus("annulé");
            showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la validation: " + e.getMessage());
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