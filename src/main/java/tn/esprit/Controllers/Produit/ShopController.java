package tn.esprit.Controllers.Produit;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import tn.esprit.Models.Produit;
import tn.esprit.Services.ProduitService;

public class ShopController {

    @FXML private GridPane productGrid;
    @FXML private TextField searchField;
    @FXML private HBox adminButtonsContainer;

    private ProduitService produitService = new ProduitService();
    private List<Produit> produits;

    @FXML
    public void initialize() {
        loadProducts();
        // Check if user is admin and show admin buttons
        if (isAdmin()) {
            adminButtonsContainer.setVisible(true);
        }

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts(newValue);
        });
    }

    private boolean isAdmin() {
        try {
            String[] sessionInfo = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("session.txt"))).split("\n");
            if (sessionInfo.length >= 2) {
                String role = sessionInfo[1].trim();
                return "ADMIN".equalsIgnoreCase(role);
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    public void handleProductManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/produit_view.fxml"));
            Region root = loader.load();
            productGrid.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleStockManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/stock_view.fxml"));
            Region root = loader.load();
            productGrid.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOrderManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/commande_view.fxml"));
            Region root = loader.load();
            productGrid.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProducts() {
        productGrid.getChildren().clear();
        produits = produitService.getAll();
        displayProducts(produits);
    }

    private void displayProducts(List<Produit> produits) {
        productGrid.getChildren().clear(); // Clear existing products
        int column = 0;
        int row = 1;

        try {
            for (Produit produit : produits) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Produit/product-card.fxml"));
                Region productCard = fxmlLoader.load();

                ProductCardController cardController = fxmlLoader.getController();
                cardController.setProductData(produit);

                productGrid.add(productCard, column++, row);
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterProducts(String searchText) {
        List<Produit> filteredProducts = produits.stream()
                .filter(produit -> produit.getNomProduit().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
        displayProducts(filteredProducts);
    }
}
