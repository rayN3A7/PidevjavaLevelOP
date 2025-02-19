package tn.esprit.Controllers.Produit;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import tn.esprit.Models.Produit;
import tn.esprit.Services.ProduitService;

public class ShopController {

    @FXML private GridPane productGrid;

    private ProduitService produitService = new ProduitService();

    @FXML
    public void initialize() {
        List<Produit> produits = produitService.getAll();
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
}
