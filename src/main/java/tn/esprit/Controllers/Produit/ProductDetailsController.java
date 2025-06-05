package tn.esprit.Controllers.Produit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import tn.esprit.Models.Produit;
import tn.esprit.Models.Review;
import tn.esprit.Models.Stock;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.HardwareSpecs;
import tn.esprit.Services.ProduitService;
import tn.esprit.Services.ReviewService;
import tn.esprit.Services.StockService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

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

    @FXML private VBox reviewsContainer;
    @FXML private TextArea reviewTextArea;
    @FXML private Button submitReviewButton;
    @FXML private Label reviewStatusLabel;

    private static final Dotenv dotenv = Dotenv.load();
    private StockService stockService;
    private Stock currentStock;
    private Produit currentProduct;
    private ProduitService produitService = new ProduitService();

    private static final String GEMINI_API_KEY = dotenv.get("GEMINI_API_KEY");
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;
    private static final String OPENAI_MODEL = "gpt-3.5-turbo";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String IMAGE_DIR = "C:\\xampp\\htdocs\\img\\";

    private ReviewService reviewService;
    private int currentUserId;

    @FXML
    public void initialize() {
        stockService = new StockService();
        reviewService = new ReviewService();
        currentUserId = SessionManager.getInstance().getUserId();
        
        if (imagePreviewOverlay != null) {
            imagePreviewOverlay.setVisible(false);
            imagePreviewOverlay.setOnMouseClicked(event -> hideImagePreview());
        }

        // Initialiser le style de la description du produit
        if (productDescription != null) {
            productDescription.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            productDescription.setEditable(false);
            productDescription.setWrapText(true);
        }
        
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
        
        // Set the basic product data
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

        // Load product reviews if the components are available
        if (product != null && reviewsContainer != null) {
            loadProductReviews(product.getId());
            
            // Check eligibility only if all components are available
            if (reviewTextArea != null && submitReviewButton != null && reviewStatusLabel != null) {
                checkReviewEligibility(product.getId());
            } else {
                System.err.println("Some review form components are not initialized in FXML");
            }
        }
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
                        String gpuName = gpu.replace("\"", "").trim();
                        // Ne pas filtrer les GPUs Intel Iris Xe
                        if (!isVirtualDisplay(gpuName)) {
                            gpus.add(gpuName);
                        }
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

    private boolean isVirtualDisplay(String gpuName) {
        // Liste des mots-clés pour identifier les affichages virtuels et les moniteurs USB
        String[] virtualKeywords = {
            "virtual display",
            "virtual adapter",
            "usb display",
            "usb monitor",
            "mobile display",
            "mobile monitor",
            "generic display",
            "generic monitor",
            "microsoft basic display"
        };

        gpuName = gpuName.toLowerCase();
        
        // Vérifier si le nom contient des mots-clés d'affichage virtuel
        for (String keyword : virtualKeywords) {
            if (gpuName.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }

    public void estimateFPS(String cpu, String ram, List<String> gpus) {
        fpsVBox.getChildren().clear();
        try {
            String osInfo = System.getProperty("os.name") + " " + System.getProperty("os.arch");
            String gameName = currentProduct.getNomProduit();
            if (gameName == null || gameName.trim().isEmpty()) {
                throw new Exception("Aucun jeu sélectionné pour l'estimation FPS");
            }

            for (int i = 0; i < gpus.size(); i++) {
                String systemSpecs = String.format("OS: %s, CPU: %s, RAM: %s, GPU: %s", osInfo, cpu, ram, gpus.get(i));
                String prompt = String.format(
                        "Estimate the expected FPS for the game '%s' running on the following system: %s. " +
                                "Assume the game is played at 1920x1080 (1080p) resolution with anti-aliasing disabled. " +
                                "Provide FPS estimates for three graphical presets: " +
                                "Low (minimum graphical settings, prioritizing performance), " +
                                "Medium (balanced graphical settings), and " +
                                "High (maximum graphical settings, prioritizing visuals). " +
                                "Return the response strictly in the format: low,medium,high (e.g., 60,45,30) with only the three FPS numbers separated by commas, no additional text or explanation.",
                        gameName, systemSpecs);

                String fpsResponse;
                try {
                    fpsResponse = callGeminiApi(prompt);
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'appel à l'API pour l'estimation FPS: " + e.getMessage());
                    fpsResponse = "30,45,60"; // Valeurs par défaut en cas d'erreur
                }

                fpsResponse = fpsResponse.replaceAll("[^0-9,]", "").trim();
                String[] fpsValues = fpsResponse.split(",");

                int lowFps = 30, mediumFps = 45, highFps = 60;

                try {
                    if (fpsValues.length >= 3) {
                        lowFps = Integer.parseInt(fpsValues[0].trim());
                        mediumFps = Integer.parseInt(fpsValues[1].trim());
                        highFps = Integer.parseInt(fpsValues[2].trim());
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Format FPS invalide pour GPU " + gpus.get(i) + ", utilisation des valeurs par défaut.");
                }

                // Création des labels avec style
                Label gpuHeader = new Label("GPU " + (i + 1) + ": " + gpus.get(i));
                gpuHeader.getStyleClass().add("fps-header");
                
                Label lowLabel = new Label(String.format("Low Settings: %d FPS", lowFps));
                lowLabel.getStyleClass().add("fps-value");
                
                Label mediumLabel = new Label(String.format("Medium Settings: %d FPS", mediumFps));
                mediumLabel.getStyleClass().add("fps-value");
                
                Label highLabel = new Label(String.format("High Settings: %d FPS", highFps));
                highLabel.getStyleClass().add("fps-value");
                
                Label detailsLabel = new Label("Based on Gemini AI estimates");
                detailsLabel.getStyleClass().add("fps-details");

                VBox gpuFpsBox = new VBox(5, gpuHeader, lowLabel, mediumLabel, highLabel, detailsLabel);
                gpuFpsBox.getStyleClass().add("fps-info");
                fpsVBox.getChildren().add(gpuFpsBox);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'estimation FPS: " + e.getMessage());
            Label errorLabel = new Label("Estimation FPS non disponible");
            errorLabel.getStyleClass().add("fps-error");
            fpsVBox.getChildren().add(errorLabel);
        }
    }

    private String callGeminiApi(String prompt) throws IOException {
        try {
            if (GEMINI_API_KEY == null || GEMINI_API_KEY.trim().isEmpty()) {
                throw new IOException("Clé API Gemini non configurée");
            }

            JSONObject requestJson = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            parts.put(new JSONObject().put("text", prompt));
            content.put("parts", parts);
            contents.put(content);
            requestJson.put("contents", contents);

            RequestBody body = RequestBody.create(requestJson.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(GEMINI_API_URL)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Erreur API Gemini: " + response.code() + " - " + response.message());
                }
                String responseBody = response.body().string();
                return parseGeminiResponse(responseBody);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel à l'API Gemini: " + e.getMessage());
            // Retourner des valeurs par défaut en cas d'erreur
            return "30,45,60";
        }
    }

    private String parseGeminiResponse(String responseBody) throws IOException {
        try {
            // On recherche tous les nombres dans la réponse brute pour extraire les FPS
            Pattern fpsPattern = Pattern.compile("\\d+");
            Matcher matcher = fpsPattern.matcher(responseBody);

            // Stocker tous les nombres extraits (qui devraient être des FPS)
            List<String> fpsList = new ArrayList<>();
            while (matcher.find()) {
                fpsList.add(matcher.group());
            }

            // Vérifier si nous avons trois valeurs (pour bas, moyen, élevé)
            if (fpsList.size() >= 3) {
                String lowFps = fpsList.get(0);
                String mediumFps = fpsList.get(1);
                String highFps = fpsList.get(2);
                return String.format("%s,%s,%s", lowFps, mediumFps, highFps);
            } else {
                throw new IOException("Pas assez de valeurs FPS extraites. Réponse peut être incorrecte.");
            }
        } catch (Exception e) {
            throw new IOException("Erreur lors de l'extraction des FPS : " + e.getMessage());
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
            // Essayer d'abord de charger depuis le dossier des images
            String defaultImagePath = IMAGE_DIR + "default-product.png";
            File defaultImageFile = new File(defaultImagePath);
            
            if (defaultImageFile.exists()) {
                Image defaultImage = new Image(defaultImageFile.toURI().toString());
                if (!defaultImage.isError()) {
                    productImage.setImage(defaultImage);
                    return;
                }
            }
            
            // Si le fichier n'existe pas ou est invalide, essayer de charger depuis les ressources
            String resourcePath = "/assets/image/default-product.png";
            Image fallbackImage = new Image(getClass().getResourceAsStream(resourcePath));
            if (!fallbackImage.isError()) {
                productImage.setImage(fallbackImage);
                return;
            }
            
            // Si tout échoue, créer une image vide
            System.err.println("Impossible de charger l'image par défaut, création d'une image vide");
            productImage.setImage(null);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image par défaut: " + e.getMessage());
            productImage.setImage(null);
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
                productPrice.setText(currentStock.getPrixProduit() + " TND");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur inattendue est survenue: " + e.getMessage());
        }
    }

    private void loadProductReviews(int productId) {
        if (reviewsContainer == null) {
            System.err.println("Review container is not initialized in FXML");
            return;
        }
        
        reviewsContainer.getChildren().clear();
        
        // Add a label for the reviews section
        Label reviewsSectionLabel = new Label("Commentaires");
        reviewsSectionLabel.getStyleClass().add("section-title");
        reviewsContainer.getChildren().add(reviewsSectionLabel);
        
        // Get reviews for this product
        List<Review> reviews = reviewService.getByProduitId(productId);
        
        if (reviews.isEmpty()) {
            Label noReviewsLabel = new Label("Aucun commentaire pour ce produit.");
            noReviewsLabel.getStyleClass().add("info-value");
            reviewsContainer.getChildren().add(noReviewsLabel);
            return;
        }
        
        // Add each review to the container
        for (Review review : reviews) {
            VBox reviewBox = createReviewBox(review);
            reviewsContainer.getChildren().add(reviewBox);
            reviewsContainer.getChildren().add(new Separator());
        }
    }
    
    private VBox createReviewBox(Review review) {
        VBox reviewBox = new VBox(5);
        reviewBox.getStyleClass().add("review-box");
        reviewBox.setPadding(new javafx.geometry.Insets(10));
        
        // Review header with user info and date
        HBox reviewHeader = new HBox(10);
        reviewHeader.setAlignment(Pos.CENTER_LEFT);
        
        Utilisateur user = review.getUtilisateur();
        String username = user != null ? user.getNickname() : "Utilisateur inconnu";
        
        Label userLabel = new Label(username);
        userLabel.getStyleClass().add("review-username");
        
        // Format date
        String formattedDate = review.getCreatedAt().toLocalDate().toString();
        Label dateLabel = new Label(formattedDate);
        dateLabel.getStyleClass().add("review-date");
        
        // Add spacer between user and date
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        reviewHeader.getChildren().addAll(userLabel, spacer, dateLabel);
        
        // Review comment
        TextArea commentArea = new TextArea(review.getComment());
        commentArea.setEditable(false);
        commentArea.setWrapText(true);
        commentArea.getStyleClass().add("review-comment");
        commentArea.setPrefRowCount(2);
        
        reviewBox.getChildren().addAll(reviewHeader, commentArea);
        
        return reviewBox;
    }
    
    private void checkReviewEligibility(int productId) {
        if (reviewTextArea == null || submitReviewButton == null || reviewStatusLabel == null) {
            System.err.println("Review form components are not initialized in FXML");
            return;
        }
        
        // Set initial state
        reviewTextArea.setDisable(true);
        submitReviewButton.setDisable(true);
        reviewStatusLabel.setText("Vous devez avoir acheté ce produit pour laisser un commentaire.");
        
        // Check if user is logged in
        if (currentUserId <= 0) {
            reviewStatusLabel.setText("Connectez-vous pour laisser un commentaire.");
            return;
        }
        
        // Remove the check if user already reviewed this product
        // to allow multiple reviews from the same user
        
        // Check if user has purchased this product
        boolean hasPurchased = reviewService.hasUserPurchasedProduct(currentUserId, productId);
        if (hasPurchased) {
            reviewTextArea.setDisable(false);
            submitReviewButton.setDisable(false);
            reviewStatusLabel.setText("Partagez votre avis sur ce produit.");
        } else {
            reviewStatusLabel.setText("Achetez ce produit pour pouvoir laisser un commentaire.");
        }
    }
    
    @FXML
    private void handleSubmitReview() {
        if (currentProduct == null || reviewTextArea == null || reviewTextArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un commentaire.");
            return;
        }
        
        // Get user comment
        String comment = reviewTextArea.getText().trim();
        
        // Create a new review
        try {
            Utilisateur currentUser = new UtilisateurService().getOne(currentUserId);
            
            Review newReview = new Review();
            newReview.setUtilisateur(currentUser);
            newReview.setProduit(currentProduct);
            newReview.setComment(comment);
            
            boolean added = reviewService.add(newReview);
            
            if (added) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre commentaire a été ajouté.");
                reviewTextArea.clear();
                loadProductReviews(currentProduct.getId());
                checkReviewEligibility(currentProduct.getId());
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter votre commentaire.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
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