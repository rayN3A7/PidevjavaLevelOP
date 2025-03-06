package tn.esprit.Controllers.Produit;

import java.io.File;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    @FXML private StackPane imagePreviewOverlay;
    @FXML private ImageView previewImage;
    @FXML private Label stockStatus;
    @FXML private Label platformLabel;
    @FXML private Label regionLabel;
    @FXML private Label typeLabel;
    @FXML private Label activationRegionLabel;

    @FXML private Label cpuLabel;
    @FXML private Label ramLabel;
    @FXML private Label osLabel;
    @FXML private VBox gpuVBox;

    @FXML private VBox fpsVBox;

    private final Dotenv dotenv = Dotenv.configure().load();
    private StockService stockService;
    private Stock currentStock;
    private Produit currentProduct;
    private ProduitService produitService = new ProduitService();

    private String OPENAI_API_KEY = dotenv.get("OPENAI_API_KEY");
    private String OPENAI_API_URL = dotenv.get("OPENAI_API_URL");
    private static final String OPENAI_MODEL = "gpt-3.5-turbo";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String IMAGE_DIR = "C:\\xampp\\htdocs\\img\\";

    @FXML
    public void initialize() {
        stockService = new StockService();
        if (imagePreviewOverlay != null) {
            imagePreviewOverlay.setVisible(false);
            imagePreviewOverlay.setOnMouseClicked(event -> hideImagePreview());
        }
        System.out.println("Product Description Style: " + productDescription.getStyle());
        updateSystemSpecs();
    }

    public void setProductData(Produit product, Stock stock) {
        if (productName == null || productPrice == null || productImage == null || productDescription == null ||
                platformLabel == null || regionLabel == null || typeLabel == null || activationRegionLabel == null ||
                imagePreviewOverlay == null || previewImage == null ||
                cpuLabel == null || ramLabel == null || osLabel == null || gpuVBox == null || fpsVBox == null) {
            System.err.println("ERROR: FXML injection failed! Check fx:id in product-details.fxml");
            showAlert(Alert.AlertType.ERROR, "Error", "FXML injection failed. Check the FXML file.");
            return;
        }
        this.currentStock = stock;
        this.currentProduct = product;
        productName.setText(product.getNomProduit() != null ? product.getNomProduit() : "N/A");
        productPrice.setText(stock != null && stock.getPrixProduit() > 0 ? stock.getPrixProduit() + " DNT" : "N/A");
        if (stock != null) {
            if (stock.getQuantity() > 0) {
                stockStatus.setText("En Stock");
                stockStatus.getStyleClass().setAll("stock-status", "in-stock");
            } else {
                stockStatus.setText("Hors Stock");
                stockStatus.getStyleClass().setAll("stock-status", "out-of-stock");
            }
        } else {
            stockStatus.setText("Non Disponible");
            stockStatus.getStyleClass().setAll("stock-status", "unavailable");
        }
        productDescription.setText(product.getDescription() != null ? product.getDescription() : "No description available");
        platformLabel.setText(product.getPlatform() != null ? product.getPlatform() : "Non disponible");
        regionLabel.setText(product.getRegion() != null ? product.getRegion() : "Mondial");
        typeLabel.setText(product.getType() != null ? product.getType() : "Clé numérique");
        activationRegionLabel.setText(product.getActivation_region() != null ? product.getActivation_region() : "Aucune restriction");

        if (stock != null && stock.getImage() != null && !stock.getImage().isEmpty()) {
            loadMainImage(stock.getImage());
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
            List<String> gpus = new ArrayList<>();

            String[] parts = specsJson.split(",");
            for (String part : parts) {
                if (part.contains("\"cpu\"")) {
                    cpu = part.split(":")[1].replace("\"", "").trim();
                } else if (part.contains("\"ram\"")) {
                    ram = part.split(":")[1].replace("\"", "").trim();
                } else if (part.contains("\"gpus\"")) {
                    String gpuPart = specsJson.substring(specsJson.indexOf("[") + 1, specsJson.indexOf("]"));
                    for (String gpu : gpuPart.split(",")) {
                        gpus.add(gpu.replace("\"", "").trim());
                    }
                }
            }
            if (specsJson.contains("\"error\"")) {
                throw new Exception("Hardware specs retrieval failed: " + specsJson);
            }

            cpuLabel.setText("CPU: " + cpu);
            ramLabel.setText("RAM: " + ram);
            osLabel.setText("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));

            gpuVBox.getChildren().clear();
            for (int i = 0; i < gpus.size(); i++) {
                Label gpuLabel = new Label("GPU " + (i + 1) + ": " + gpus.get(i));
                gpuLabel.getStyleClass().add("spec-item");
                gpuVBox.getChildren().add(gpuLabel);
            }

            if (currentProduct != null) {
                estimateFPS(cpu, ram, gpus);
            }
        } catch (Exception e) {
            System.err.println("Error updating system specs: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update system specifications: " + e.getMessage());
        }
    }

    private void estimateFPS(String cpu, String ram, List<String> gpus) {
        fpsVBox.getChildren().clear();
        try {
            String osInfo = System.getProperty("os.name") + " " + System.getProperty("os.arch");
            String gameName = currentProduct.getNomProduit();
            if (gameName == null || gameName.trim().isEmpty()) {
                throw new Exception("No game selected for FPS estimation");
            }

            for (int i = 0; i < gpus.size(); i++) {
                String systemSpecs = String.format("OS: %s, CPU: %s, RAM: %s, GPU: %s", osInfo, cpu, ram, gpus.get(i));
                String prompt = String.format(
                        "Estimate the expected FPS for the game '%s' on: %s. Assume anti-aliasing is disabled. " +
                                "Provide three FPS values for Low, Medium, and High settings in the format: low,medium,high.",
                        gameName, systemSpecs);

                String fpsResponse = callOpenAIApi(prompt);
                fpsResponse = fpsResponse.replaceAll("[^0-9,]", "").trim();
                String[] fpsValues = fpsResponse.split(",");
                if (fpsValues.length != 3) {
                    throw new Exception("Invalid FPS response format for GPU " + gpus.get(i));
                }

                int lowFps = Integer.parseInt(fpsValues[0].trim());
                int mediumFps = Integer.parseInt(fpsValues[1].trim());
                int highFps = Integer.parseInt(fpsValues[2].trim());

                Label gpuHeader = new Label("GPU " + (i + 1) + ": " + gpus.get(i));
                gpuHeader.getStyleClass().add("fps-header");
                Label lowLabel = new Label(String.format("Low Settings: %d FPS", lowFps));
                lowLabel.getStyleClass().add("fps-value");
                Label mediumLabel = new Label(String.format("Medium Settings: %d FPS", mediumFps));
                mediumLabel.getStyleClass().add("fps-value");
                Label highLabel = new Label(String.format("High Settings: %d FPS", highFps));
                highLabel.getStyleClass().add("fps-value");
                Label detailsLabel = new Label("Based on LevelOp AI estimates");
                detailsLabel.getStyleClass().add("fps-details");

                VBox gpuFpsBox = new VBox(5, gpuHeader, lowLabel, mediumLabel, highLabel, detailsLabel);
                gpuFpsBox.getStyleClass().add("fps-info");
                fpsVBox.getChildren().add(gpuFpsBox);
            }
        } catch (Exception e) {
            System.err.println("Error estimating FPS: " + e.getMessage());
            Label errorLabel = new Label("FPS Estimation Unavailable: " + e.getMessage());
            errorLabel.getStyleClass().add("fps-details");
            fpsVBox.getChildren().add(errorLabel);
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

    private void loadMainImage(String imageFileName) {
        try {
            if (imageFileName != null && !imageFileName.trim().isEmpty()) {
                String imagePath = IMAGE_DIR + imageFileName.trim();
                Image image = new Image(new File(imagePath).toURI().toString());
                if (image != null && !image.isError()) {
                    productImage.setImage(image);
                    return;
                }
                System.err.println("Error loading main image: " + imagePath + " - Image file not found or invalid");
            } else {
                System.err.println("Error loading main image: Image file name is null or empty");
            }
        } catch (Exception e) {
            System.err.println("Error loading main image: " + imageFileName + " - " + e.getMessage());
        }
        loadDefaultImage();
    }

    private void loadDefaultImage() {
        try {
            String defaultImagePath = IMAGE_DIR + "default-product.png";
            Image defaultImage = new Image(new File(defaultImagePath).toURI().toString());
            if (defaultImage != null && !defaultImage.isError()) {
                productImage.setImage(defaultImage);
            } else {
                System.err.println("Error loading default image: " + defaultImagePath + " - Default image not found or invalid");
                Image fallbackImage = new Image(getClass().getResourceAsStream("/assets/image/default-product.png"));
                if (fallbackImage != null && !fallbackImage.isError()) {
                    productImage.setImage(fallbackImage);
                } else {
                    System.err.println("Error loading fallback default image from resources");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading default image: " + e.getMessage());
        }
    }

    @FXML
    private void handleImageClick() {
        if (imagePreviewOverlay != null && previewImage != null) {
            previewImage.setImage(productImage.getImage());
            imagePreviewOverlay.setVisible(true);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), imagePreviewOverlay);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), previewImage);
            scaleIn.setFromX(0.9);
            scaleIn.setFromY(0.9);
            scaleIn.setToX(1);
            scaleIn.setToY(1);

            fadeIn.play();
            scaleIn.play();
        }
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

    @FXML
    private void handleBackToShop() {
        try {
            // Load the shop-page.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/shop-page.fxml"));
            Parent shopContent = loader.load();

            // Get the current stage
            Stage stage = (Stage) productImage.getScene().getWindow();

            // Load the main.fxml (which contains the navbar and dynamic content)
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/Produit/main.fxml"));
            Parent mainRoot = mainLoader.load();

            // Cast the main root to BorderPane (main.fxml is a BorderPane)
            BorderPane mainBorderPane = (BorderPane) mainRoot;

            // Set the shop content as the center of the main BorderPane
            mainBorderPane.setCenter(shopContent);

            // Set the scene to the main root
            stage.setScene(new javafx.scene.Scene(mainRoot));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de shop-page.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour à la boutique: " + e.getMessage());
        }
    }

    @FXML
    private void handleBuyNow() {
        try {
            if (currentStock == null || currentStock.getQuantity() <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Ce produit n'est pas disponible ou en rupture de stock.");
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
            controller.setDialogStage(dialogStage);
            controller.setData(currentProduct, currentStock);

            dialogStage.showAndWait();

            if (controller.isValidateClicked()) {
                currentStock = stockService.getByProduitId(currentProduct.getId());
                productPrice.setText(currentStock.getPrixProduit() + " DNT");
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