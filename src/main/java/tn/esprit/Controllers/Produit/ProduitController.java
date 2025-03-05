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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.Models.Produit;
import tn.esprit.Models.Stock;
import tn.esprit.Services.ProduitService;
import tn.esprit.Services.StockService;
import tn.esprit.utils.SessionManager;

public class ProduitController {

    @FXML private VBox productContainer;
    @FXML private TextField searchField;
    @FXML private TextField txtNomProduit;
    @FXML private TextField txtDescription;
    @FXML private TextField txtPlatform;
    @FXML private TextField txtRegion;
    @FXML private TextField txtType;
    @FXML private TextField txtActivationRegion;
    @FXML private TextField txtScore;
    @FXML private VBox editForm;

    private ProduitService produitService;
    private StockService stockService;
    private List<Produit> produits;
    private Produit selectedProduit;
    String userRole= SessionManager.getInstance().getRole().name();
    @FXML
    public void initialize() {
        produitService = new ProduitService();
        stockService = new StockService();
        loadProducts();
        if (editForm != null) {
            editForm.setVisible(false);
        } else {
            System.err.println("Warning: editForm is null. Check if fx:id is properly set in FXML.");
        }

        // Add listener to search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchProducts(newValue);
        });
    }

    private void loadProducts() {
        productContainer.getChildren().clear();
        produits = produitService.getAll();

        for (Produit produit : produits) {
            Stock stock = stockService.getByProduitId(produit.getId());
            productContainer.getChildren().add(createProductRow(produit, stock));
        }
    }

    private HBox createProductRow(Produit produit, Stock stock) {
        HBox row = new HBox(0); // Remove spacing as we'll control it with the GridPane
        row.getStyleClass().add("platform-info");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));

        // Create a GridPane for precise column alignment
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Set column constraints without ID column
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(15);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(15);
        ColumnConstraints col5 = new ColumnConstraints();
        col5.setPercentWidth(20);

        gridPane.getColumnConstraints().addAll(col1, col2, col3, col4, col5);

        // Create and style the labels without ID
        Label nameLabel = new Label(produit.getNomProduit());
        nameLabel.getStyleClass().addAll("info-value", "cell");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setWrapText(true);

        Label descLabel = new Label(produit.getDescription());
        descLabel.getStyleClass().addAll("info-value", "cell");
        descLabel.setMaxWidth(Double.MAX_VALUE);
        descLabel.setWrapText(true);

        Label priceLabel = new Label(stock != null ? stock.getPrixProduit() + " €" : "N/A");
        priceLabel.getStyleClass().addAll("info-value", "cell");
        priceLabel.setMaxWidth(Double.MAX_VALUE);

        Label platformLabel = new Label(produit.getPlatform());
        platformLabel.getStyleClass().addAll("info-value", "cell");
        platformLabel.setMaxWidth(Double.MAX_VALUE);
        platformLabel.setWrapText(true);

        // Create action buttons
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("buy-now-button");
        editButton.setOnAction(event -> updateProduit(produit, stock));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("back-button");
        deleteButton.setOnAction(event -> deleteProduit(produit));

        HBox actionsBox = new HBox(5, editButton, deleteButton);
        actionsBox.getStyleClass().add("action-buttons");
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);

        // Add components to the GridPane without ID column
        gridPane.add(nameLabel, 0, 0);
        gridPane.add(descLabel, 1, 0);
        gridPane.add(priceLabel, 2, 0);
        gridPane.add(platformLabel, 3, 0);
        gridPane.add(actionsBox, 4, 0);

        // Make the GridPane fill the entire width
        gridPane.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(gridPane, javafx.scene.layout.Priority.ALWAYS);

        row.getChildren().add(gridPane);
        return row;
    }

    private void searchProducts(String searchText) {
        productContainer.getChildren().clear();
        searchText = searchText.toLowerCase();

        for (Produit produit : produits) {
            if (produit.getNomProduit().toLowerCase().contains(searchText) ||
                    produit.getDescription().toLowerCase().contains(searchText)) {
                Stock stock = stockService.getByProduitId(produit.getId());
                productContainer.getChildren().add(createProductRow(produit, stock));
            }
        }
    }

    @FXML
    private void searchProducts() {
        String searchText = searchField.getText().toLowerCase();
        productContainer.getChildren().clear();

        for (Produit produit : produits) {
            if (produit.getNomProduit().toLowerCase().contains(searchText) ||
                    produit.getDescription().toLowerCase().contains(searchText) ||
                    produit.getPlatform().toLowerCase().contains(searchText) ||
                    produit.getType().toLowerCase().contains(searchText)) {
                Stock stock = stockService.getByProduitId(produit.getId());
                productContainer.getChildren().add(createProductRow(produit, stock));
            }
        }
    }

    @FXML
    public void updateProduit(Produit produit, Stock stock) {
        this.selectedProduit = produit;
        txtNomProduit.setText(produit.getNomProduit());
        txtDescription.setText(produit.getDescription());
        txtPlatform.setText(produit.getPlatform());
        txtRegion.setText(produit.getRegion());
        txtType.setText(produit.getType());
        txtActivationRegion.setText(produit.getActivation_region());
        txtScore.setText(String.valueOf(produit.getScore()));

        editForm.setVisible(true);
    }

    @FXML
    public void saveProductChanges() {
        try {
            // Get values from form fields
            String nomProduit = txtNomProduit.getText().trim();
            String description = txtDescription.getText().trim();
            String platform = txtPlatform.getText().trim();
            String region = txtRegion.getText().trim();
            String type = txtType.getText().trim();
            String activationRegion = txtActivationRegion.getText().trim();

            // Validate required fields
            if (nomProduit.isEmpty() || description.isEmpty() || platform.isEmpty()) {
                showAlert(AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs obligatoires (nom, description, plateforme).");
                return;
            }

            // Parse numeric fields
            int score;
            try {
                score = Integer.parseInt(txtScore.getText().trim());
                if (score < 0 || score > 100) {
                    showAlert(AlertType.ERROR, "Erreur", "Le score doit être entre 0 et 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(AlertType.ERROR, "Erreur", "Le score doit être un nombre entier.");
                return;
            }

            if (selectedProduit != null) {
                // Update existing product
                selectedProduit.setNomProduit(nomProduit);
                selectedProduit.setDescription(description);
                selectedProduit.setPlatform(platform);
                selectedProduit.setRegion(region);
                selectedProduit.setType(type);
                selectedProduit.setActivation_region(activationRegion);
                selectedProduit.setScore(score);

                produitService.update(selectedProduit);
                showAlert(AlertType.INFORMATION, "Succès", "Le produit a été mis à jour avec succès.");
            } else {
                // Create new product
                Produit newProduit = new Produit(
                        0, // ID will be set by database
                        nomProduit,
                        description,
                        platform,
                        region,
                        type,
                        activationRegion,
                        score
                );

                try {
                    produitService.add(newProduit);
                    showAlert(AlertType.INFORMATION, "Succès", "Le nouveau produit a été ajouté avec succès.");
                } catch (RuntimeException e) {
                    showAlert(AlertType.ERROR, "Erreur Base de données",
                            "Impossible d'ajouter le produit à la base de données. Erreur: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }

            // Refresh the product list
            loadProducts();
            // Clear form and hide it
            clearFields();
            editForm.setVisible(false);
            selectedProduit = null;

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Une erreur inattendue est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void cancelEdit() {
        editForm.setVisible(false);
        selectedProduit = null;
        clearFields();
    }

    @FXML
    public void ButtonAjouterProduit() {
        selectedProduit = null;
        clearFields();
        editForm.setVisible(true);
    }

    private void deleteProduit(Produit produit) {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce produit ?");

        if (confirmation.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            try {
                // Delete associated stock entry first
                Stock stock = stockService.getByProduitId(produit.getId());
                if (stock != null) {
                    stockService.delete(stock);
                }

                // Then delete the product
                produitService.delete(produit);
                loadProducts();
                showAlert(AlertType.INFORMATION, "Succès", "Le produit a été supprimé avec succès.");
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de la suppression: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void clearFields() {
        txtNomProduit.clear();
        txtDescription.clear();
        txtPlatform.clear();
        txtRegion.clear();
        txtType.clear();
        txtActivationRegion.clear();
        txtScore.clear();
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
        if(userRole.equals ("ADMIN")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sidebarAdmin.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);

                Stage window = (Stage) productContainer.getScene().getWindow();
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

                Stage window = (Stage) productContainer.getScene().getWindow();
                window.setScene(scene);
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers l'accueil");
            }
        }
    }
}
