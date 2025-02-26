package tn.esprit.Controllers.Produit;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import tn.esprit.Models.Produit;
import tn.esprit.Models.Stock;
import tn.esprit.Services.StockService;

public class ProductCardController {
    @FXML private Label productName;
    @FXML private Label productPrice;
    @FXML private ImageView productImage;
    @FXML private Button viewDetailsButton;

    private Produit product;
    private Stock stock;
    private StockService stockService;

    @FXML
    public void initialize() {
        stockService = new StockService();
    }

    public void setProductData(Produit product) {
        this.product = product;
        productName.setText(product.getNomProduit());

        // Get stock information
        this.stock = stockService.getByProduitId(product.getId());
        if (stock != null) {
            productPrice.setText(stock.getPrixProduit() + " DNT");

            // Load image from stock
            String imagePath = "/assets/image/" + stock.getImage();
            try {
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                productImage.setImage(image);
            } catch (IllegalArgumentException | NullPointerException e) {
                System.err.println("Error loading image: " + e.getMessage());
                // Load a default image
                try {
                    Image defaultImage = new Image(getClass().getResourceAsStream("/assets/image/default-product.png"));
                    productImage.setImage(defaultImage);
                } catch (Exception ex) {
                    System.err.println("Error loading default image: " + ex.getMessage());
                }
            }
        } else {
            productPrice.setText("N/A");
            // Load default image
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/assets/image/default-product.png"));
                productImage.setImage(defaultImage);
            } catch (Exception e) {
                System.err.println("Error loading default image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/product-details.fxml"));
            Parent detailsContent = loader.load();

            ProductDetailsController controller = loader.getController();
            if (controller != null) {
                controller.setProductData(product, stock);
            } else {
                System.err.println("Error: Controller is null.");
            }

            BorderPane root = (BorderPane) productName.getScene().getRoot();
            root.setCenter(detailsContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
