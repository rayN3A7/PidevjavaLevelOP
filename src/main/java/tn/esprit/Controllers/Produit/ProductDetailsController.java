package tn.esprit.Controllers.Produit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Models.Produit;
import tn.esprit.Models.Stock;
import tn.esprit.Services.StockService;

public class ProductDetailsController {
    @FXML private Label productName;
    @FXML private Label productPrice;
    @FXML private ImageView productImage;
    @FXML private TextArea productDescription;
    @FXML private HBox carouselImages;
    @FXML private StackPane imagePreviewOverlay;
    @FXML private ImageView previewImage;

    // Platform info fields
    @FXML private Label platformLabel;
    @FXML private Label regionLabel;
    @FXML private Label typeLabel;
    @FXML private Label activationRegionLabel;

    private List<Image> productImages = new ArrayList<>();
    private List<ImageView> thumbnails = new ArrayList<>();
    private int currentImageIndex = 0;
    private StockService stockService;
    private Stock currentStock;
    private Produit currentProduct;

    @FXML
    public void initialize() {
        stockService = new StockService();
        if (imagePreviewOverlay != null) {
            imagePreviewOverlay.setVisible(false);
            imagePreviewOverlay.setOnMouseClicked(event -> hideImagePreview());
        }
    }

    public void setProductData(Produit product, Stock stock) {
        // Debug: Check if FXML fields are injected
        if (productName == null || productPrice == null || productImage == null ||
                productDescription == null || platformLabel == null || regionLabel == null ||
                typeLabel == null || activationRegionLabel == null || carouselImages == null ||
                imagePreviewOverlay == null || previewImage == null) {
            System.err.println("ERROR: FXML injection failed! Check fx:id in product-details.fxml");
            return;
        }

        this.currentStock = stock;
        this.currentProduct = product;

        // Set basic product info
        productName.setText(product.getNomProduit());
        productPrice.setText(stock != null ? stock.getPrixProduit() + " DNT" : "N/A");
        productDescription.setText(product.getDescription());

        // Set platform info
        platformLabel.setText(product.getPlatform() != null ? product.getPlatform() : "Non disponible");
        regionLabel.setText(product.getRegion() != null ? product.getRegion() : "Mondial");
        typeLabel.setText(product.getType() != null ? product.getType() : "Clé numérique");
        activationRegionLabel.setText(product.getActivation_region() != null ?
                product.getActivation_region() : "Aucune restriction");

        // Load images for carousel
        if (stock != null && stock.getImage() != null) {
            loadProductImages(stock.getImage());
        } else {
            loadDefaultImage();
        }
    }

    private void loadProductImages(String mainImagePath) {
        // Clear existing images
        productImages.clear();
        thumbnails.clear();
        carouselImages.getChildren().clear();

        // Add main product image
        String basePath = "/assets/image/";
        try {
            // Load main image
            Image mainImage = new Image(getClass().getResourceAsStream(basePath + mainImagePath));
            if (!mainImage.isError()) {
                productImages.add(mainImage);
                productImage.setImage(mainImage);

                // Create thumbnail
                ImageView thumbnail = new ImageView(mainImage);
                thumbnail.setFitHeight(80);
                thumbnail.setFitWidth(80);
                thumbnail.setPreserveRatio(true);
                thumbnail.getStyleClass().add("carousel-thumbnail");
                thumbnail.getStyleClass().add("selected");

                final int index = thumbnails.size();
                thumbnail.setOnMouseClicked(e -> {
                    selectImage(index);
                    showImagePreview(productImages.get(index));
                });

                thumbnails.add(thumbnail);
                carouselImages.getChildren().add(thumbnail);
            } else {
                loadDefaultImage();
            }
        } catch (Exception e) {
            System.err.println("Error loading main image: " + mainImagePath);
            loadDefaultImage();
        }
    }

    private void loadDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/assets/images/default-product.png"));
            productImage.setImage(defaultImage);
            
            // Add to carousel
            ImageView thumbnail = new ImageView(defaultImage);
            thumbnail.setFitHeight(80);
            thumbnail.setFitWidth(80);
            thumbnail.setPreserveRatio(true);
            thumbnail.getStyleClass().add("carousel-thumbnail");
            thumbnail.getStyleClass().add("selected");
            
            productImages.clear();
            thumbnails.clear();
            carouselImages.getChildren().clear();
            
            productImages.add(defaultImage);
            thumbnails.add(thumbnail);
            carouselImages.getChildren().add(thumbnail);
        } catch (Exception e) {
            System.err.println("Error loading default image: " + e.getMessage());
        }
    }

    private void showImagePreview(Image image) {
        previewImage.setImage(image);
        imagePreviewOverlay.setVisible(true);
        imagePreviewOverlay.setOpacity(0);
        imagePreviewOverlay.setScaleX(0.9);
        imagePreviewOverlay.setScaleY(0.9);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), imagePreviewOverlay);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), imagePreviewOverlay);
        scaleIn.setToX(1);
        scaleIn.setToY(1);

        fadeIn.play();
        scaleIn.play();
    }

    private void hideImagePreview() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), imagePreviewOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), imagePreviewOverlay);
        scaleOut.setFromX(1);
        scaleOut.setFromY(1);
        scaleOut.setToX(0.9);
        scaleOut.setToY(0.9);

        fadeOut.setOnFinished(e -> imagePreviewOverlay.setVisible(false));

        fadeOut.play();
        scaleOut.play();
    }

    private void selectImage(int index) {
        if (index >= 0 && index < productImages.size()) {
            // Update main image
            productImage.setImage(productImages.get(index));
            currentImageIndex = index;

            // Update thumbnail selection
            for (int i = 0; i < thumbnails.size(); i++) {
                ImageView thumbnail = thumbnails.get(i);
                if (i == index) {
                    if (!thumbnail.getStyleClass().contains("selected")) {
                        thumbnail.getStyleClass().add("selected");
                    }
                } else {
                    thumbnail.getStyleClass().remove("selected");
                }
            }
        }
    }

    @FXML
    private void previousImage() {
        int newIndex = currentImageIndex - 1;
        if (newIndex < 0) {
            newIndex = productImages.size() - 1;
        }
        selectImage(newIndex);
    }

    @FXML
    private void nextImage() {
        int newIndex = (currentImageIndex + 1) % productImages.size();
        selectImage(newIndex);
    }

    @FXML
    private void handleBackToShop() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/shop-page.fxml"));
            ScrollPane shopContent = loader.load();

            // Update the center of the BorderPane
            BorderPane root = (BorderPane) productImage.getScene().getRoot();
            root.setCenter(shopContent);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de shop-page.fxml: " + e.getMessage());
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors du retour à la boutique: " + e.getMessage());
        }
    }

    @FXML
    private void handleBuyNow() {
        try {
            // Check if stock exists and has quantity
            if (currentStock == null) {
                showAlert(AlertType.ERROR, "Erreur", "Ce produit n'est pas disponible en stock.");
                return;
            }
            
            if (currentStock.getQuantity() <= 0) {
                showAlert(AlertType.ERROR, "Erreur", "Ce produit est en rupture de stock.");
                return;
            }

            // Load the confirmation dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/commande-confirmation.fxml"));
            Parent confirmationDialog = loader.load();

            // Create and configure the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter au panier");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(productImage.getScene().getWindow());
            
            Scene scene = new Scene(confirmationDialog);
            dialogStage.setScene(scene);

            // Get the controller and set the data
            CommandeConfirmationController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setData(currentProduct, currentStock);

            // Show the dialog and wait for user response
            dialogStage.showAndWait();

            // If user validated the commande, refresh the product details
            if (controller.isValidateClicked()) {
                // Refresh stock information
                currentStock = stockService.getByProduitId(currentProduct.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'ouverture de la fenêtre de confirmation: " + e.getMessage());
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