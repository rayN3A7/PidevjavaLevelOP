package tn.esprit.Controllers.Produit;

import java.io.File;
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
import javafx.stage.FileChooser;
import tn.esprit.Models.Produit;
import tn.esprit.Models.Stock;
import tn.esprit.Services.ProduitService;
import tn.esprit.Services.StockService;

public class StockController {
    @FXML private VBox stockContainer;
    @FXML private TextField searchField;
    @FXML private TextField txtId;
    @FXML private TextField txtProduit;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtPrice;
    @FXML private TextField txtImage;
    @FXML private VBox editForm;

    private StockService stockService;
    private ProduitService produitService;
    private List<Stock> stocks;
    private Stock selectedStock;

    @FXML
    public void initialize() {
        stockService = new StockService();
        produitService = new ProduitService();
        loadStocks();
        if (editForm != null) {
            editForm.setVisible(false);
        } else {
            System.err.println("Warning: editForm is null. Check if fx:id is properly set in FXML.");
        }
    }

    private void loadStocks() {
        stockContainer.getChildren().clear();
        stocks = stockService.getAll();
        for (Stock stock : stocks) {
            stockContainer.getChildren().add(createStockRow(stock));
        }
    }

    private HBox createStockRow(Stock stock) {
        HBox row = new HBox(0);
        row.getStyleClass().add("platform-info");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(10);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(20);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(15);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(15);
        ColumnConstraints col5 = new ColumnConstraints();
        col5.setPercentWidth(20);
        ColumnConstraints col6 = new ColumnConstraints();
        col6.setPercentWidth(20);

        gridPane.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6);

        Label idLabel = new Label(String.valueOf(stock.getId()));
        idLabel.getStyleClass().addAll("info-value", "cell");
        idLabel.setMaxWidth(Double.MAX_VALUE);

        Produit produit = produitService.getOne(stock.getProduitId());
        Label produitLabel = new Label(produit != null ? produit.getNomProduit() : "N/A");
        produitLabel.getStyleClass().addAll("info-value", "cell");
        produitLabel.setMaxWidth(Double.MAX_VALUE);
        produitLabel.setWrapText(true);

        Label quantityLabel = new Label(String.valueOf(stock.getQuantity()));
        quantityLabel.getStyleClass().addAll("info-value", "cell");
        quantityLabel.setMaxWidth(Double.MAX_VALUE);

        Label priceLabel = new Label(String.format("%d DNT", stock.getPrixProduit()));
        priceLabel.getStyleClass().addAll("info-value", "cell");
        priceLabel.setMaxWidth(Double.MAX_VALUE);

        Label imageLabel = new Label(stock.getImage());
        imageLabel.getStyleClass().addAll("info-value", "cell");
        imageLabel.setMaxWidth(Double.MAX_VALUE);
        imageLabel.setWrapText(true);

        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("buy-now-button");
        editButton.setOnAction(event -> updateStock(stock));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("back-button");
        deleteButton.setOnAction(event -> deleteStock(stock));

        HBox actionsBox = new HBox(5, editButton, deleteButton);
        actionsBox.getStyleClass().add("action-buttons");
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);

        gridPane.add(idLabel, 0, 0);
        gridPane.add(produitLabel, 1, 0);
        gridPane.add(quantityLabel, 2, 0);
        gridPane.add(priceLabel, 3, 0);
        gridPane.add(imageLabel, 4, 0);
        gridPane.add(actionsBox, 5, 0);

        gridPane.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(gridPane, javafx.scene.layout.Priority.ALWAYS);

        row.getChildren().add(gridPane);
        return row;
    }

    @FXML
    public void searchStock() {
        String searchText = searchField.getText().toLowerCase();
        stockContainer.getChildren().clear();

        for (Stock stock : stocks) {
            Produit produit = produitService.getOne(stock.getProduitId());
            if (String.valueOf(stock.getId()).contains(searchText) ||
                (produit != null && produit.getNomProduit().toLowerCase().contains(searchText)) ||
                String.valueOf(stock.getQuantity()).contains(searchText) ||
                String.valueOf(stock.getPrixProduit()).contains(searchText)) {
                stockContainer.getChildren().add(createStockRow(stock));
            }
        }
    }

    @FXML
    public void updateStock(Stock stock) {
        this.selectedStock = stock;
        txtId.setText(String.valueOf(stock.getId()));
        txtProduit.setText(String.valueOf(stock.getProduitId()));
        txtQuantity.setText(String.valueOf(stock.getQuantity()));
        txtPrice.setText(String.valueOf(stock.getPrixProduit()));
        txtImage.setText(stock.getImage());
        
        editForm.setVisible(true);
    }

    @FXML
    public void saveStockChanges() {
        try {
            if (!validateForm()) {
                showAlert(AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            // Validate numeric fields
            int quantity;
            int price;
            int productId;
            try {
                quantity = Integer.parseInt(txtQuantity.getText().trim());
                price = Integer.parseInt(txtPrice.getText().trim());
                productId = Integer.parseInt(txtProduit.getText().trim());
                
                if (quantity < 0 || price < 0) {
                    showAlert(AlertType.ERROR, "Erreur", "La quantité et le prix doivent être positifs.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(AlertType.ERROR, "Erreur", "La quantité, le prix et l'ID du produit doivent être des nombres entiers.");
                return;
            }

            // Verify that the product exists
            Produit produit = produitService.getOne(productId);
            if (produit == null) {
                showAlert(AlertType.ERROR, "Erreur", "Produit avec ID " + productId + " n'existe pas.");
                return;
            }

            if (selectedStock != null) {
                // Update existing stock
                selectedStock.setProduitId(productId);
                selectedStock.setQuantity(quantity);
                selectedStock.setPrixProduit(price);
                selectedStock.setImage(txtImage.getText().trim());

                stockService.update(selectedStock);
                showAlert(AlertType.INFORMATION, "Succès", "Le stock a été mis à jour avec succès.");
            } else {
                // Create new stock
                Stock newStock = new Stock(
                    0,
                    productId,
                    0,
                    quantity,
                    price,
                    txtImage.getText().trim()
                );
                
                stockService.add(newStock);
                showAlert(AlertType.INFORMATION, "Succès", "Le nouveau stock a été ajouté avec succès.");
            }

            loadStocks();
            clearFields();
            editForm.setVisible(false);
            selectedStock = null;

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Une erreur inattendue est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void cancelEdit() {
        editForm.setVisible(false);
        selectedStock = null;
        clearFields();
    }

    @FXML
    public void ButtonAjouterStock() {
        selectedStock = null;
        clearFields();
        editForm.setVisible(true);
    }

    @FXML
    public void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            String destinationPath = "../../main/resources/assets/image/" + file.getName();
            txtImage.setText(destinationPath);
        }
    }

    private void deleteStock(Stock stock) {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce stock ?");

        if (confirmation.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            try {
                stockService.delete(stock);
                loadStocks();
                showAlert(AlertType.INFORMATION, "Succès", "Le stock a été supprimé avec succès.");
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur", 
                    "Une erreur est survenue lors de la suppression: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void clearFields() {
        txtId.clear();
        txtProduit.clear();
        txtQuantity.clear();
        txtPrice.clear();
        txtImage.clear();
    }

    private boolean validateForm() {
        return !txtProduit.getText().trim().isEmpty() &&
               !txtQuantity.getText().trim().isEmpty() &&
               !txtPrice.getText().trim().isEmpty() &&
               !txtImage.getText().trim().isEmpty();
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
            BorderPane currentRoot = (BorderPane) stockContainer.getScene().getRoot();
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
            BorderPane currentRoot = (BorderPane) stockContainer.getScene().getRoot();
            currentRoot.setCenter(shopView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers le magasin");
        }
    }

    @FXML
    private void navigateToOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/commandes_view.fxml"));
            Parent ordersView = loader.load();
            BorderPane currentRoot = (BorderPane) stockContainer.getScene().getRoot();
            currentRoot.setCenter(ordersView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers les commandes");
        }
    }
}