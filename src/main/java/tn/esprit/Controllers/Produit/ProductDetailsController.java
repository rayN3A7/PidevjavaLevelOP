package tn.esprit.Controllers.Produit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tn.esprit.Models.Produit;
import tn.esprit.Models.Stock;
import tn.esprit.Services.HardwareSpecs;
import tn.esprit.Services.ProduitService;
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

    // System specs fields
    @FXML private Label cpuLabel;
    @FXML private Label gpuLabel;
    @FXML private Label ramLabel;
    @FXML private Label osLabel;

    // FPS estimation fields
    @FXML private Label estimatedFpsLabelLow;
    @FXML private Label estimatedFpsLabelMedium;
    @FXML private Label estimatedFpsLabelHigh;
    @FXML private Label fpsDetailsLabel;
    private final Dotenv dotenv = Dotenv.configure().load();
    private List<Image> productImages = new ArrayList<>();
    private List<ImageView> thumbnails = new ArrayList<>();
    private int currentImageIndex = 0;
    private StockService stockService;
    private Stock currentStock;
    private Produit currentProduct;
    private ProduitService produitService = new ProduitService();

    // OpenAI API configuration
    private  String OPENAI_API_KEY = dotenv.get("OPENAI_API_KEY");
    private  String OPENAI_API_URL = dotenv.get("OPENAI_API_URL");
    private static final String OPENAI_MODEL = "gpt-3.5-turbo";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        stockService = new StockService();
        if (imagePreviewOverlay != null) {
            imagePreviewOverlay.setVisible(false);
            imagePreviewOverlay.setOnMouseClicked(event -> hideImagePreview());
        }
        updateSystemSpecs();
    }

    public void setProductData(Produit product, Stock stock) {
        if (productName == null || productPrice == null || productImage == null || productDescription == null ||
                platformLabel == null || regionLabel == null || typeLabel == null || activationRegionLabel == null ||
                carouselImages == null || imagePreviewOverlay == null || previewImage == null ||
                cpuLabel == null || gpuLabel == null || ramLabel == null || osLabel == null ||
                estimatedFpsLabelLow == null || estimatedFpsLabelMedium == null || estimatedFpsLabelHigh == null || fpsDetailsLabel == null) {
            System.err.println("ERROR: FXML injection failed! Check fx:id in product-details.fxml");
            showAlert(Alert.AlertType.ERROR, "Error", "FXML injection failed. Check the FXML file for missing or mismatched fx:id values.");
            return;
        }
        this.currentStock = stock;
        this.currentProduct = product;
        productName.setText(product.getNomProduit() != null ? product.getNomProduit() : "N/A");
        productPrice.setText(stock != null && stock.getPrixProduit() > 0 ? stock.getPrixProduit() + " DNT" : "N/A");
        productDescription.setText(product.getDescription() != null ? product.getDescription() : "No description available");
        platformLabel.setText(product.getPlatform() != null ? product.getPlatform() : "Non disponible");
        regionLabel.setText(product.getRegion() != null ? product.getRegion() : "Mondial");
        typeLabel.setText(product.getType() != null ? product.getType() : "Clé numérique");
        activationRegionLabel.setText(product.getActivation_region() != null ? product.getActivation_region() : "Aucune restriction");
        if (stock != null && stock.getImage() != null && !stock.getImage().isEmpty()) {
            loadProductImages(stock.getImage());
        } else {
            loadDefaultImage();
        }
        updateSystemSpecs();
    }

    private void updateSystemSpecs() {
        try {
            String specsJson = HardwareSpecs.getHardwareSpecs();
            String cpu = "Unknown";
            String ram = "Unknown";
            String gpu = "Unknown";
            String[] parts = specsJson.split(",");
            for (String part : parts) {
                if (part.contains("\"cpu\"")) {
                    cpu = part.split(":")[1].replace("\"", "").trim();
                } else if (part.contains("\"ram\"")) {
                    ram = part.split(":")[1].replace("\"", "").trim();
                } else if (part.contains("\"gpu\"")) {
                    gpu = part.split(":")[1].replace("\"", "").trim();
                }
            }
            if (specsJson.contains("\"error\"")) {
                throw new Exception("Hardware specs retrieval failed: " + specsJson);
            }
            System.out.println("Raw Hardware Specs: CPU=" + cpu + ", RAM=" + ram + ", GPU=" + gpu);
            if (cpuLabel != null) cpuLabel.setText("CPU: " + cpu);
            if (gpuLabel != null) gpuLabel.setText("GPU: " + gpu);
            if (ramLabel != null) ramLabel.setText("RAM: " + ram);
            if (osLabel != null) osLabel.setText("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
            if (currentProduct != null) {
                estimateFPS(cpu, ram, gpu);
            }
        } catch (Exception e) {
            System.err.println("Error updating system specs: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update system specifications: " + e.getMessage());
        }
    }

    private void estimateFPS(String cpu, String ram, String gpu) {
        try {
            String systemSpecs = String.format("OS: %s %s, CPU: %s, RAM: %s, GPU: %s",
                    System.getProperty("os.name"), System.getProperty("os.arch"), cpu, ram, gpu);
            String gameName = currentProduct.getNomProduit();
            if (gameName == null || gameName.trim().isEmpty()) {
                throw new Exception("No game selected for FPS estimation");
            }

            // Create a more detailed prompt that requests FPS estimates for different graphics settings
            String prompt = String.format(
                    "Estimate the expected FPS (Frames Per Second) for the game '%s' on the following hardware configuration: %s. " +
                            "Assume that anti-aliasing is completely disabled. " +
                            "Consider CPU, GPU, RAM, and other relevant hardware aspects when making your estimation and also consider UserbenchMark and can i Run it websites . " +
                            "Provide three FPS values corresponding to Low, Medium, and High graphics settings, strictly in the format: low,medium,high.",
                    gameName, systemSpecs);


            String fpsResponse = callOpenAIApi(prompt);
            System.out.println("Estimateur de fps LevelOp " + fpsResponse);

            // Parse the comma-separated response
            String[] fpsValues = fpsResponse.split(",");
            if (fpsValues.length != 3) {
                throw new Exception("Invalid FPS response format");
            }

            int lowFps = Integer.parseInt(fpsValues[0].trim());
            int mediumFps = Integer.parseInt(fpsValues[1].trim());
            int highFps = Integer.parseInt(fpsValues[2].trim());

            // Update the UI labels with the different FPS values
            if (estimatedFpsLabelLow != null) {
                estimatedFpsLabelLow.setText(String.format("Low Settings: %d FPS", lowFps));
            }
            if (estimatedFpsLabelMedium != null) {
                estimatedFpsLabelMedium.setText(String.format("Medium Settings: %d FPS", mediumFps));
            }
            if (estimatedFpsLabelHigh != null) {
                estimatedFpsLabelHigh.setText(String.format("High Settings: %d FPS", highFps));
            }
            if (fpsDetailsLabel != null) {
                fpsDetailsLabel.setText("Basant sur les estimations DE LevelOp AI ");
            }
        } catch (Exception e) {
            System.err.println("Error estimating FPS: " + e.getMessage());
            if (estimatedFpsLabelLow != null) {
                estimatedFpsLabelLow.setText("Low Settings: Unavailable");
            }
            if (estimatedFpsLabelMedium != null) {
                estimatedFpsLabelMedium.setText("Medium Settings: Unavailable");
            }
            if (estimatedFpsLabelHigh != null) {
                estimatedFpsLabelHigh.setText("High Settings: Unavailable");
            }
            if (fpsDetailsLabel != null) {
                fpsDetailsLabel.setText("Could not analyze system specifications or game: " + e.getMessage());
            }
        }
    }

    private String callOpenAIApi(String prompt) throws IOException {
        String requestBodyJson = String.format(
                "{\"model\":\"%s\",\"messages\":[{\"role\":\"system\",\"content\":\"You are a gaming performance expert. Provide FPS estimates as a single number only.\"},{\"role\":\"user\",\"content\":\"%s\"}]}",
                OPENAI_MODEL,
                prompt.replace("\"", "\\\"")
        );

        RequestBody body = RequestBody.create(requestBodyJson, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBodyString = response.body().string();
                System.err.println("OpenAI API request failed: " + response.code() + " - " + responseBodyString);
                throw new IOException("OpenAI API request failed: " + response.code());
            }
            String responseBody = response.body().string();
            return parseOpenAIResponse(responseBody);
        }
    }

    private String parseOpenAIResponse(String responseBody) throws IOException {
        try {
            return objectMapper.readTree(responseBody)
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText().trim();
        } catch (Exception e) {
            throw new IOException("Failed to parse OpenAI API response: " + e.getMessage());
        }
    }

    private int parseFpsResponse(String fpsResponse) {
        try {
            return Integer.parseInt(fpsResponse);
        } catch (NumberFormatException e) {
            System.err.println("Invalid FPS response from OpenAI: " + fpsResponse);
            return 30; // Default fallback FPS
        }
    }

    private void loadProductImages(String mainImagePath) {
        productImages.clear();
        thumbnails.clear();
        carouselImages.getChildren().clear();
        String basePath = "/assets/image/";
        try {
            Image mainImage = new Image(getClass().getResourceAsStream(basePath + mainImagePath));
            if (!mainImage.isError()) {
                productImages.add(mainImage);
                productImage.setImage(mainImage);
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
            productImage.setImage(productImages.get(index));
            currentImageIndex = index;
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
            BorderPane root = (BorderPane) productImage.getScene().getRoot();
            root.setCenter(shopContent);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de shop-page.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour à la boutique: " + e.getMessage());
        }
    }

    @FXML
    private void handleBuyNow() {
        try {
            if (currentStock == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Ce produit n'est pas disponible en stock.");
                return;
            }
            if (currentStock.getQuantity() <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Ce produit est en rupture de stock.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/commande-confirmation.fxml"));
            Parent confirmationDialog = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Valider commande");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(productImage.getScene().getWindow());
            dialogStage.setScene(new Scene(confirmationDialog));

            CommandeConfirmationController controller = loader.getController();
            if (controller == null) {
                throw new IOException("Failed to get CommandeConfirmationController");
            }
            controller.setDialogStage(dialogStage);
            controller.setData(currentProduct, currentStock);

            dialogStage.showAndWait();

            if (controller.isValidateClicked()) {
                currentStock = stockService.getByProduitId(currentProduct.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur inattendue est survenue: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}