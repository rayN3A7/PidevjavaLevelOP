package tn.esprit.Controllers.Produit;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
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
    @FXML private VBox editForm;

    private CommandeService commandeService;
    private ProduitService produitService;
    private List<Commande> commandes;
    private Commande selectedCommande;
    private final int DEFAULT_USER_ID = SessionManager.getInstance().getUserId();

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

        // Updated column constraints for better layout
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(40); // Product name column
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(30); // Status column
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(30); // Actions column

        gridPane.getColumnConstraints().addAll(col1, col2, col3);

        // Get product name and create label
        Produit produit = produitService.getOne(commande.getProduitId());
        Label produitLabel = new Label(produit != null ? produit.getNomProduit() : "N/A");
        produitLabel.getStyleClass().addAll("info-value", "cell");
        produitLabel.setMaxWidth(Double.MAX_VALUE);
        produitLabel.setWrapText(true);

        // Status label
        Label statusLabel = new Label(commande.getStatus());
        statusLabel.getStyleClass().addAll("info-value", "cell");
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        // Action buttons
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("buy-now-button");
        editButton.setOnAction(event -> updateCommande(commande));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("back-button");
        deleteButton.setOnAction(event -> deleteCommande(commande));

        HBox actionsBox = new HBox(5, editButton, deleteButton);
        actionsBox.getStyleClass().add("action-buttons");
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);

        // Add components to grid
        gridPane.add(produitLabel, 0, 0);
        gridPane.add(statusLabel, 1, 0);
        gridPane.add(actionsBox, 2, 0);

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
        // ID and user fields removed

        Produit produit = produitService.getOne(commande.getProduitId());
        txtProduit.setText(produit != null ? produit.getNomProduit() : "");
        txtStatus.setText(commande.getStatus());

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
                // Update existing command
                selectedCommande.setProduitId(produit.getId());
                selectedCommande.setStatus(txtStatus.getText().trim());
                commandeService.update(selectedCommande);
                showAlert(AlertType.INFORMATION, "Succès", "La commande a été mise à jour avec succès.");
            } else {
                // Add new command
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
        String normalizedInput = name.trim().toLowerCase(); // Trim + lowercase user input
        for (Produit produit : products) {
            // Trim + lowercase database product name
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
    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/main.fxml"));
            BorderPane mainView = loader.load();
            BorderPane currentRoot = (BorderPane) commandeContainer.getScene().getRoot();
            currentRoot.setCenter(mainView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers l'accueil");
        }
    }

    @FXML
    private void navigateToShop() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/shop-page.fxml"));
            Parent shopView = loader.load();
            BorderPane currentRoot = (BorderPane) commandeContainer.getScene().getRoot();
            currentRoot.setCenter(shopView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers le magasin");
        }
    }

    @FXML
    private void navigateToProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/produit_view.fxml"));
            Parent productsView = loader.load();
            BorderPane currentRoot = (BorderPane) commandeContainer.getScene().getRoot();
            currentRoot.setCenter(productsView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers les produits");
        }
    }
    @FXML
    private void cancelEdit(){
        editForm.setVisible(false);
    }
}