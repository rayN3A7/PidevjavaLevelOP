package tn.esprit.Controllers.Produit;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import tn.esprit.Models.Produit;
import tn.esprit.Services.ProduitService;

public class ShopController {

    @FXML private GridPane productGrid;
    @FXML private TextField searchField;

    private ProduitService produitService = new ProduitService();
    private List<Produit> produits;

    @FXML
    public void initialize() {
        loadProducts();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts(newValue);
        });
    }

    private void loadProducts() {
        productGrid.getChildren().clear();
        produits = produitService.getAll();
        displayProducts(produits);
    }

    private void displayProducts(List<Produit> produits) {
        productGrid.getChildren().clear();
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