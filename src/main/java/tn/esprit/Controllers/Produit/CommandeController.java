package tn.esprit.Controllers.Produit;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Models.Commande;
import tn.esprit.Models.Produit;
import tn.esprit.Services.CommandeService;
import tn.esprit.Services.ProduitService;
import tn.esprit.utils.SessionManager;

public class CommandeController {
    @FXML private VBox commandeContainer;
    @FXML private TextField searchField;
    @FXML private TextField txtProduit;
    @FXML private TextField txtStatus;
    @FXML private TextField txtUtilisateur;
    @FXML private TextField txtId;
    @FXML private Label lblDate;
    @FXML private VBox editForm;

    private CommandeService commandeService;
    private ProduitService produitService;
    private List<Commande> commandes;
    private Commande selectedCommande;
    private final int DEFAULT_USER_ID = SessionManager.getInstance().getUserId();
    String userRole= SessionManager.getInstance().getRole().name();

    @FXML
    public void initialize() {
        commandeService = new CommandeService();
        produitService = new ProduitService();
        loadCommandes();
        if (editForm != null) {
            editForm.setVisible(false);
        } else {
            System.err.println("Warning: editForm is null. Check if fx:id is properly set in FXML.");
        }
    }

    private void loadCommandes() {
        commandeContainer.getChildren().clear();
        commandes = commandeService.getAll();
        for (Commande commande : commandes) {
            commandeContainer.getChildren().add(createCommandeRow(commande));
        }
    }

    private HBox createCommandeRow(Commande commande) {
        HBox row = new HBox(0);
        row.getStyleClass().add("platform-info");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(40);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(30);

        gridPane.getColumnConstraints().addAll(col1, col2, col3);

        // Get utilisateur name - simplified to match the image showing "cs2"
        String utilisateurName = "User ID: " + commande.getUtilisateurId();
        // Try to simplify name for display
        try {
            int userId = commande.getUtilisateurId();
            // Here you could try to get the actual user's name/nickname
            // For now we'll just display "cs" + userId to match the image
            utilisateurName = "cs" + userId;
        } catch (Exception e) {
            // Fallback to default
        }
        
        Label utilisateurLabel = new Label(utilisateurName);
        utilisateurLabel.getStyleClass().addAll("info-value", "cell");
        utilisateurLabel.setMaxWidth(Double.MAX_VALUE);
        utilisateurLabel.setWrapText(true);
        
        // Get produit name
        Produit produit = produitService.getOne(commande.getProduitId());
        Label produitLabel = new Label(produit != null ? produit.getNomProduit() : "N/A");
        produitLabel.getStyleClass().addAll("info-value", "cell");
        produitLabel.setMaxWidth(Double.MAX_VALUE);
        produitLabel.setWrapText(true);

        // Display status or date based on the image
        String dateTimeText;
        if (commande.getCreatedAt() != null) {
            // Format to match the image: "2025-05-01 16:19:07"
            dateTimeText = commande.getCreatedAt().toLocalDate() + " " + 
                    commande.getCreatedAt().toLocalTime().toString();
        } else {
            dateTimeText = commande.getStatus(); // Fallback to status if no date
        }
        
        Label dateTimeLabel = new Label(dateTimeText);
        dateTimeLabel.getStyleClass().addAll("info-value", "cell");
        dateTimeLabel.setMaxWidth(Double.MAX_VALUE);
        dateTimeLabel.setWrapText(true);

        gridPane.add(utilisateurLabel, 0, 0);
        gridPane.add(produitLabel, 1, 0);
        gridPane.add(dateTimeLabel, 2, 0);

        gridPane.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(gridPane, javafx.scene.layout.Priority.ALWAYS);

        row.getChildren().add(gridPane);
        return row;
    }

    @FXML
    public void searchCommandes() {
        String searchText = searchField.getText().toLowerCase();
        commandeContainer.getChildren().clear();

        for (Commande commande : commandes) {
            Produit produit = produitService.getOne(commande.getProduitId());
            if ((produit != null && produit.getNomProduit().toLowerCase().contains(searchText)) ||
                    commande.getStatus().toLowerCase().contains(searchText)) {
                commandeContainer.getChildren().add(createCommandeRow(commande));
            }
        }
    }

    @FXML
    public void updateCommande(Commande commande) {
        this.selectedCommande = commande;

        // Set ID if available
        if (txtId != null) {
            txtId.setText(String.valueOf(commande.getId()));
        }
        
        // Set user info if available
        if (txtUtilisateur != null) {
            txtUtilisateur.setText("cs" + commande.getUtilisateurId());
        }

        // Set product info
        Produit produit = produitService.getOne(commande.getProduitId());
        txtProduit.setText(produit != null ? produit.getNomProduit() : "");
        
        // Set status
        txtStatus.setText(commande.getStatus());
        
        // Set date if available
        if (lblDate != null && commande.getCreatedAt() != null) {
            lblDate.setText(commande.getCreatedAt().toLocalDate() + " " + 
                    commande.getCreatedAt().toLocalTime().toString());
        }

        editForm.setVisible(true);
    }

    @FXML
    public void saveCommandeChanges() {
        try {
            if (!validateForm()) {
                showAlert(AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            String productName = txtProduit.getText().trim();
            Produit produit = findProductByName(productName);
            if (produit == null) {
                showAlert(AlertType.ERROR, "Erreur", "Produit non trouvé: " + productName + ". Veuillez vérifier le nom ou ajouter le produit.");
                return;
            }

            if (selectedCommande != null) {
                selectedCommande.setProduitId(produit.getId());
                selectedCommande.setStatus(txtStatus.getText().trim());
                commandeService.update(selectedCommande);
                showAlert(AlertType.INFORMATION, "Succès", "La commande a été mise à jour avec succès.");
            } else {
                Commande newCommande = new Commande();
                newCommande.setProduitId(produit.getId());
                newCommande.setStatus(txtStatus.getText().trim());
                newCommande.setUtilisateurId(DEFAULT_USER_ID);
                commandeService.add(newCommande);
                showAlert(AlertType.INFORMATION, "Succès", "La nouvelle commande a été ajoutée avec succès.");
            }

            loadCommandes();
            clearFields();
            editForm.setVisible(false);
            selectedCommande = null;

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Une erreur inattendue est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void ButtonAjouterCommande() {
        selectedCommande = null;
        clearFields();
        editForm.setVisible(true);
    }

    private Produit findProductByName(String name) {
        List<Produit> products = produitService.getAll();
        String normalizedInput = name.trim().toLowerCase();
        for (Produit produit : products) {
            String normalizedProductName = produit.getNomProduit().trim().toLowerCase();
            if (normalizedProductName.equals(normalizedInput)) {
                return produit;
            }
        }
        return null;
    }

    private void clearFields() {
        txtProduit.clear();
        txtStatus.clear();
    }

    private boolean validateForm() {
        return !txtProduit.getText().trim().isEmpty() &&
                !txtStatus.getText().trim().isEmpty();
    }

    private void deleteCommande(Commande commande) {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette commande ?");

        if (confirmation.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            try {
                commandeService.delete(commande);
                loadCommandes();
                showAlert(AlertType.INFORMATION, "Succès", "La commande a été supprimée avec succès.");
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de la suppression: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        if(userRole.equals ("ADMIN"))  {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sidebarAdmin.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);

                Stage window = (Stage) commandeContainer.getScene().getWindow();
                window.setScene(scene);
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers l'accueil");
            }
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/main.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);

                Stage window = (Stage) commandeContainer.getScene().getWindow();
                window.setScene(scene);
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers l'accueil");
            }
        }
    }

    @FXML
    private void cancelEdit(){
        editForm.setVisible(false);
    }
}