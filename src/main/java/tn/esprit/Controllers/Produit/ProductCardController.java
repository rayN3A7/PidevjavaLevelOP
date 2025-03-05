package tn.esprit.Controllers.Produit;

import java.io.File;
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

    private static final String IMAGE_DIR = "C:\\xampp\\htdocs\\img\\";

    @FXML
    public void initialize() {
        stockService = new StockService();
    }

    public void setProductData(Produit product) {
        this.product = product;
        productName.setText(product.getNomProduit());

        this.stock = stockService.getByProduitId(product.getId());
        if (stock != null) {
            productPrice.setText(stock.getPrixProduit() + " TND");

            String imagePath = IMAGE_DIR + stock.getImage();
            try {
                Image image = new Image(new File(imagePath).toURI().toString());
                if (!image.isError()) {
                    productImage.setImage(image);
                } else {
                    loadDefaultImage();
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                loadDefaultImage();
            }
        } else {
            productPrice.setText("N/A");
            loadDefaultImage();
        }
    }

    private void loadDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/assets/image/default-product.png"));
            if (!defaultImage.isError()) {
                productImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Error loading default image: " + e.getMessage());
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