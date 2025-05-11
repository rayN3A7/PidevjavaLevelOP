package tn.esprit.Controllers;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.Controllers.Evenement.DetailsEvenementController;
import tn.esprit.Controllers.Produit.ProductDetailsController;
import tn.esprit.Controllers.forum.QuestionDetailsController;
import tn.esprit.Models.*;
import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.Services.*;
import tn.esprit.Services.Evenement.EvenementService;
import tn.esprit.utils.SessionManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HomeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);
    private final Dotenv dotenv = Dotenv.configure().load();
    private final QuestionService questionService = new QuestionService();
    private final boolean isConnected = SessionManager.getInstance().isLoggedIn();
    private List<Question> topQuestions;
    private int currentIndex = 0;
    private static final int QUESTIONS_PER_PAGE = 4;
    private HBox[] carouselPages;
    private double dragStartX;
    private double dragThreshold = 50;

    @FXML private StackPane carouselContainer;
    @FXML private HBox questionCarousel;
    @FXML private HBox carouselIndicators;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label titleLabel;
    @FXML private Label title2Label;
    @FXML private HBox productContainer; // Added for dynamic product cards
    @FXML private HBox eventContainer;
    @FXML private HBox promoSessionsContainer;
    private static final String IMAGE_BASE_URL = "http://localhost/img/";
    private static final String DEFAULT_IMAGE_PATH = "/images/default-game.jpg";
    private List<Session_game> promoSessions;
    private final ServiceSession serviceSession = new ServiceSession();
    String path;
    String userRole = SessionManager.getInstance().getRole().name();
    private final ProduitService produitService = new ProduitService();
    private final EvenementService es = new EvenementService();
    private final CommandeService commandeService = new CommandeService();
    private final StockService stockService = new StockService(); // Add StockService

    @FXML
    public void initialize() {
        LOGGER.info("Initializing HomeController...");
        loadTopQuestions();
        initializeCarousel();
        updateCarousel();
        addPulsatingGlow(titleLabel, "#FFFFFF");
        addPulsatingGlow(title2Label, "#FF4081");
        addDragSupport();
        loadTopProducts();
        loadEvents();
        loadPromoSessions();
    }
    private void loadTopProducts() {
        try {
            // Get all products
            List<Produit> allProducts = produitService.getAll();

            // Calculate sales counts for each product based on completed orders
            Map<Integer, Integer> productSales = new HashMap<>();
            List<Commande> commandes = commandeService.getAll();
            for (Commande commande : commandes) {
                if ("terminé".equals(commande.getStatus())) { // Only count completed orders
                    int produitId = commande.getProduitId();
                    productSales.put(produitId, productSales.getOrDefault(produitId, 0) + 1);
                }
            }
            // Sort products by sales count (descending) and limit to top 4 (matching the screenshot)
            List<Produit> topProducts = allProducts.stream()
                    .sorted((p1, p2) -> Integer.compare(
                            productSales.getOrDefault(p2.getId(), 0),
                            productSales.getOrDefault(p1.getId(), 0)))
                    .limit(4)
                    .collect(Collectors.toList());

            if (productContainer != null) {
                productContainer.getChildren().clear();
                for (Produit product : topProducts) {
                    // Fetch the corresponding stock for this product to get image and price
                    Stock stock = stockService.getByProduitId(product.getId());
                    if (stock != null) {
                        VBox productCard = createProductCard(product, stock, productSales.getOrDefault(product.getId(), 0));
                        productContainer.getChildren().add(productCard);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error loading top products", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load top products");
        }
    }
    private VBox createProductCard(Produit product, Stock stock, int salesCount) {
        VBox card = new VBox(15); // Spacing to match the event card layout
        card.getStyleClass().add("event-card"); // Use the event-card style from CSS
        card.setMaxWidth(160); // Reduced width to match the screenshot
        card.setMaxHeight(280); // Reduced height to match the screenshot

        ImageView imageView = new ImageView();
        imageView.setFitHeight(200); // Match FXML static example height
        imageView.setFitWidth(140);  // Match FXML static example width
        imageView.setPreserveRatio(true); // Maintain aspect ratio for sharper images
        try {
            // Prepend the directory where images are stored (assuming the same path as in your static FXML)
            String imagePath = "file:///C:/xampp/htdocs/img/" + stock.getImage();
            Image image = new Image(imagePath);
            imageView.setImage(image);
        } catch (Exception e) {
            LOGGER.error("Error loading product image", e);
        }

        Label nameLabel = new Label(product.getNomProduit());
        nameLabel.getStyleClass().add("event-title"); // Use the event-title style from CSS for consistency
        nameLabel.setStyle("-fx-wrap-text: true; -fx-text-alignment: CENTER; -fx-alignment: CENTER;"); // Ensure text wraps and is centered

        // Fixed to show price in TND with proper formatting
        Label priceLabel = new Label(String.format("%.2f TND", (double) stock.getPrixProduit()));
        priceLabel.getStyleClass().add("event-info"); // Use the event-info style from CSS for consistency
        priceLabel.setStyle("-fx-alignment: CENTER; -fx-text-alignment: CENTER;"); // Center the price

        card.setAlignment(Pos.CENTER); // Center-align all content in the card
        card.getChildren().addAll(imageView, nameLabel, priceLabel);

        // Make the card clickable to navigate to product details
        card.setOnMouseClicked(event -> {
            navigateToProductDetails(product, stock);
            event.consume();
        });

        return card; // No inline style needed, as CSS will handle it via .event-card
    }
    private void navigateToProductDetails(Produit product, Stock stock) {
        try {
            // Load the main.fxml (which contains the navbar)
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/Produit/main.fxml"));
            Parent mainRoot = mainLoader.load();

            // Load the product details FXML
            FXMLLoader detailsLoader = new FXMLLoader(getClass().getResource("/Produit/product-details.fxml"));
            Parent detailsContent = detailsLoader.load();

            // Get the main controller
            ProductDetailsController controller = detailsLoader.getController();
            controller.setProductData(product, stock); // Pass product data

            // Insert product details into the center of main.fxml
            BorderPane mainLayout = (BorderPane) mainRoot;
            mainLayout.setCenter(detailsContent);

            // Get the current stage and set the updated scene
            Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.setScene(new Scene(mainRoot));
            stage.show();

            LOGGER.info("Navigated to product details with navbar for product: {}", product.getNomProduit());
        } catch (IOException e) {
            LOGGER.error("Error loading product details page: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page des détails du produit.");
        }
    }
    @FXML
    private void goToShop() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/main.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            LOGGER.error("Error loading shop page: " + e.getMessage(), e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Navigation Error");
            alert.setContentText("Could not load the shop page. Please try again.");
            alert.showAndWait();
        }
    }
    @FXML
    private void navigateToForum(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/Forum.fxml"));
            if (loader.getLocation() == null) {
                LOGGER.error("Forum.fxml not found at /forumUI/Forum.fxml");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Forum page not found.");
                return;
            }

            Parent root = loader.load();
            Scene forumScene = new Scene(root);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(forumScene);
            stage.show();

            LOGGER.info("Successfully navigated to Forum.fxml");
        } catch (IOException e) {
            LOGGER.error("Error loading Forum.fxml: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page du forum : " + e.getMessage());
        }
    }

    private void loadTopQuestions() {
        LOGGER.info("Loading top questions...");
        topQuestions = questionService.getAll();
        if (topQuestions == null || topQuestions.isEmpty()) {
            LOGGER.warn("No questions found.");
            return;
        }
        topQuestions.sort(Comparator.comparingInt(Question::getVotes).reversed());
        topQuestions = topQuestions.subList(0, Math.min(topQuestions.size(), 6));
        LOGGER.info("Loaded {} top questions.", topQuestions.size());
    }

    private void initializeCarousel() {
        LOGGER.info("Initializing carousel...");
        if (topQuestions == null || topQuestions.isEmpty()) {
            LOGGER.warn("Cannot initialize carousel: No questions available.");
            questionCarousel.getChildren().add(new Label("Aucune question disponible."));
            return;
        }

        int pageCount = (int) Math.ceil((double) topQuestions.size() / QUESTIONS_PER_PAGE);
        carouselPages = new HBox[pageCount];

        for (int i = 0; i < pageCount; i++) {
            HBox page = new HBox(20);
            page.setAlignment(Pos.CENTER);
            int startIndex = i * QUESTIONS_PER_PAGE;
            int endIndex = Math.min(startIndex + QUESTIONS_PER_PAGE, topQuestions.size());

            for (int j = startIndex; j < endIndex; j++) {
                Question question = topQuestions.get(j);
                HBox card = createQuestionCard(question);
                page.getChildren().add(card);
            }
            carouselPages[i] = page;
        }

        for (int i = 0; i < pageCount; i++) {
            Label indicator = new Label();
            indicator.setStyle("-fx-background-color: #4A4C5E; -fx-border-radius: 10; -fx-background-radius: 10; -fx-pref-width: 10; -fx-pref-height: 10; -fx-margin: 5;");
            if (i == 0) {
                indicator.getStyleClass().add("active");
            }
            carouselIndicators.getChildren().add(indicator);
        }

        if (carouselPages.length > 0) {
            questionCarousel.getChildren().add(carouselPages[0]);
        }
        LOGGER.info("Carousel initialized with {} pages.", pageCount);
    }

    private HBox createQuestionCard(Question question) {
        HBox card = new HBox(15);
        card.getStyleClass().add("modern-question-card");

        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("image-container");
        ImageView gameImage = new ImageView();
        gameImage.setFitHeight(60);
        gameImage.setFitWidth(60);
        double imageHeight = 60;
        double imageWidth = 60;
        String imageBaseDir = "C:\\xampp\\htdocs\\img\\games\\";
        if (question.getGame() != null && question.getGame().getImagePath() != null) {
            try {
                String fullImagePath = imageBaseDir + question.getGame().getImagePath();
                File imageFile = new File(fullImagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString(), true);
                    if (!image.isError()) {
                        gameImage.setImage(image);
                        imageHeight = image.getHeight() > 0 ? image.getHeight() : 60;
                        imageWidth = image.getWidth() > 0 ? image.getWidth() : 60;
                        gameImage.setFitHeight(imageHeight);
                        gameImage.setFitWidth(imageWidth);
                        LOGGER.info("Successfully loaded game image for question ID: {} at path: {}", question.getQuestion_id(), fullImagePath);
                    } else {
                        LOGGER.error("Image loading error for question ID: {}. Error: {}", question.getQuestion_id(), image.getException().getMessage());
                    }
                } else {
                    LOGGER.error("Game image file does not exist for question ID: {}. Path: {}", question.getQuestion_id(), fullImagePath);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load game image for question ID: {}. Path: {}. Error: {}", question.getQuestion_id(), question.getGame().getImagePath(), e.getMessage());
            }
        }
        gameImage.setMouseTransparent(true);
        imageContainer.getChildren().add(gameImage);

        VBox textBox = new VBox(5);
        textBox.getStyleClass().add("text-container");
        Label titleLabel = new Label(question.getTitle());
        titleLabel.getStyleClass().add("modern-question-title");
        titleLabel.setMouseTransparent(true);

        Label authorLabel = new Label("@" + (question.getUser() != null ? question.getUser().getNickname() : "Unknown"));
        authorLabel.getStyleClass().add("modern-author-tag");
        authorLabel.setMouseTransparent(true);

        textBox.getChildren().addAll(titleLabel, authorLabel);
        textBox.setMouseTransparent(true);

        card.getChildren().addAll(imageContainer, textBox);

        card.setMinWidth(300);
        card.setMaxWidth(300);
        card.setMinHeight(100);
        card.setMaxHeight(100);

        card.setOnMouseClicked(event -> {
            LOGGER.info("Question card clicked for question ID: {}. Event source: {}", question.getQuestion_id(), event.getSource());
            navigateToQuestionDetails(question);
            event.consume();
        });

        card.setOnMousePressed(event -> LOGGER.debug("Mouse pressed on question card ID: {}", question.getQuestion_id()));
        card.setOnMouseReleased(event -> LOGGER.debug("Mouse released on question card ID: {}", question.getQuestion_id()));

        return card;
    }

    private void navigateToQuestionDetails(Question question) {
        try {
            Stage stage = (Stage) questionCarousel.getScene().getWindow();
            if (stage == null) {
                LOGGER.error("Stage not found for navigation.");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Unable to find the application stage.");
                return;
            }

            if (!SessionManager.getInstance().isLoggedIn()) {
                LOGGER.info("User is not logged in. Redirecting to login page for question ID: {}", question.getQuestion_id());
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion Utilisateur/login/Login.fxml"));
                if (loader.getLocation() == null) {
                    LOGGER.error("Login.fxml not found at /gestion Utilisateur/login/Login.fxml");
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Login page not found.");
                    return;
                }

                Parent root = loader.load();
                Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
                stage.setScene(newScene);
                stage.show();
                LOGGER.info("Successfully navigated to login page.");
            } else {
                LOGGER.info("User is logged in. Navigating to QuestionDetails for question ID: {}", question.getQuestion_id());
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/QuestionDetails.fxml"));
                if (loader.getLocation() == null) {
                    LOGGER.error("QuestionDetails.fxml not found at /forumUI/QuestionDetails.fxml");
                    showAlert(Alert.AlertType.ERROR, "Erreur", "QuestionDetails page not found.");
                    return;
                }

                Parent root = loader.load();
                QuestionDetailsController controller = loader.getController();
                if (controller == null) {
                    LOGGER.error("QuestionDetailsController not found.");
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Unable to initialize QuestionDetailsController.");
                    return;
                }

                controller.loadQuestionDetails(question);
                Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
                stage.setScene(newScene);
                stage.show();
                LOGGER.info("Successfully navigated to QuestionDetails for question ID: {}", question.getQuestion_id());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to navigate for question ID: {}", question.getQuestion_id(), e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page : " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during navigation for question ID: {}", question.getQuestion_id(), e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur inattendue s'est produite : " + e.getMessage());
        }
    }

    private void updateCarousel() {
        questionCarousel.getChildren().clear();
        if (carouselPages != null && carouselPages.length > 0) {
            questionCarousel.getChildren().add(carouselPages[currentIndex]);
        } else {
            questionCarousel.getChildren().add(new Label("Aucune question disponible."));
        }
        updateIndicators();
    }

    @FXML
    private void previousQuestion() {
        if (carouselPages != null && currentIndex > 0) {
            currentIndex--;
            animateCarouselTransition(300);
        }
    }

    @FXML
    private void nextQuestion() {
        if (carouselPages != null && currentIndex < carouselPages.length - 1) {
            currentIndex++;
            animateCarouselTransition(-300);
        }
    }

    private void animateCarouselTransition(double translateX) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), questionCarousel);
        transition.setByX(translateX);
        transition.setOnFinished(e -> {
            updateCarousel();
            questionCarousel.setTranslateX(0);
        });
        transition.play();
    }

    private void updateIndicators() {
        if (carouselPages == null || carouselPages.length == 0) {
            prevButton.setVisible(false);
            nextButton.setVisible(false);
            carouselIndicators.getChildren().clear();
            return;
        }

        for (int i = 0; i < carouselIndicators.getChildren().size(); i++) {
            Label indicator = (Label) carouselIndicators.getChildren().get(i);
            indicator.getStyleClass().remove("active");
            if (i == currentIndex) {
                indicator.getStyleClass().add("active");
            }
        }

        prevButton.setVisible(currentIndex > 0);
        nextButton.setVisible(currentIndex < carouselPages.length - 1);
    }

    private void addDragSupport() {
        questionCarousel.setOnMousePressed(event -> {
            dragStartX = event.getX();
            LOGGER.debug("Drag started at X: {}", dragStartX);
        });

        questionCarousel.setOnMouseDragged(event -> {
            double deltaX = event.getX() - dragStartX;
            questionCarousel.setTranslateX(deltaX);
        });

        questionCarousel.setOnMouseReleased(event -> {
            double deltaX = event.getX() - dragStartX;
            LOGGER.debug("Drag ended with deltaX: {}", deltaX);
            if (carouselPages != null && Math.abs(deltaX) > dragThreshold) {
                if (deltaX > 0 && currentIndex > 0) {
                    previousQuestion();
                } else if (deltaX < 0 && currentIndex < carouselPages.length - 1) {
                    nextQuestion();
                } else {
                    animateCarouselTransition(-deltaX);
                }
            } else {
                animateCarouselTransition(-deltaX);
            }
        });

        questionCarousel.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            if (carouselPages != null) {
                if (deltaY > 0 && currentIndex > 0) {
                    previousQuestion();
                } else if (deltaY < 0 && currentIndex < carouselPages.length - 1) {
                    nextQuestion();
                }
            }
        });
    }

    private void addPulsatingGlow(Label label, String glowColor) {
        Timeline glowTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), new KeyValue(label.effectProperty(),
                        new DropShadow(BlurType.GAUSSIAN, Color.web(glowColor), 15, 0.5, 0, 0))),
                new KeyFrame(Duration.seconds(2), new KeyValue(label.effectProperty(),
                        new DropShadow(BlurType.GAUSSIAN, Color.web(glowColor), 30, 0.8, 0, 0)))
        );
        glowTimeline.setAutoReverse(true);
        glowTimeline.setCycleCount(Timeline.INDEFINITE);
        glowTimeline.play();
    }
    private void loadEvents() {
        List<Evenement> evenements = es.getEvenementsProches();

        eventContainer.getChildren().clear();

        for (Evenement event : evenements) {
            VBox eventCard = createEventCard(event);
            eventContainer.getChildren().add(eventCard);
        }
    }

    private VBox createEventCard(Evenement event) {
        VBox card = new VBox();
        card.getStyleClass().add("event-card");

        ImageView imageView = new ImageView();
        imageView.setImage(new Image(getClass().getResourceAsStream("/assets/image/event2.jpg")));
        imageView.setFitHeight(200);
        imageView.setFitWidth(250);

        Label title = new Label(event.getNom_event());
        title.getStyleClass().add("event-title");

        Timestamp timestamp = event.getDate_event();
        LocalDateTime localDateTime = timestamp.toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = localDateTime.format(formatter);

        Label date = new Label(formattedDate);
        date.getStyleClass().add("event-date");
        date.setStyle("-fx-text-fill: white;");

        card.getChildren().addAll(imageView, title, date);
        card.setOnMouseClicked(eventClick -> {
            try {
                String pathuser = "/Evenement/DetailsEvenement.fxml";
                String pathadmin = "/Evenement/DetailsEvenementAdmin.fxml";
                if(userRole.equals("ADMIN")){
                    path = pathadmin;
                }else {
                    path = pathuser;
                }
                FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
                if (loader.getLocation() == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "DetailsEvenement.fxml not found.");
                    return;
                }

                Parent root = loader.load();
                DetailsEvenementController controller = loader.getController();
                if (controller == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Unable to initialize DetailsEvenementController.");
                    return;
                }

                controller.initData(event);

                Stage stage = (Stage) card.getScene().getWindow();
                if (stage == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Unable to find the application stage.");
                    return;
                }

                Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
                stage.setScene(newScene);
                stage.show();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page des détails de l'événement : " + e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur inattendue s'est produite : " + e.getMessage());
            }
        });

        return card;
    }
    @FXML
    private void ButtonListeEvenements(ActionEvent event)throws Exception{
        String pathuser = "/Evenement/ListEvenement.fxml";
        String pathadmin = "/Evenement/ListeEvenementAdmin.fxml";
        if(userRole.equals("ADMIN")){
            path= pathadmin;
        }else{
            path = pathuser;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
    private void loadPromoSessions() {
        try {
            promoSessions = serviceSession.getSessionsInPromo();
            if (promoSessions != null && !promoSessions.isEmpty()) {
                displayPromoSessions();
            } else {
                Label noSessionsLabel = new Label("Aucune session en promotion trouvée.");
                noSessionsLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 16px;");
                promoSessionsContainer.getChildren().add(noSessionsLabel);
            }
        } catch (Exception e) {
            LOGGER.error("Error loading promo sessions: ", e);
            Label errorLabel = new Label("Erreur lors du chargement des sessions en promotion.");
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 16px;");
            promoSessionsContainer.getChildren().add(errorLabel);
        }
    }

    private void displayPromoSessions() {
        promoSessionsContainer.getChildren().clear();
        for (Session_game session : promoSessions) {
            VBox sessionCard = createSessionCard(session);
            promoSessionsContainer.getChildren().add(sessionCard);
        }
    }

    private VBox createSessionCard(Session_game session) {
        VBox sessionCard = new VBox(10);
        sessionCard.setStyle("-fx-background-color: #162942; " +
                "-fx-padding: 20; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5); " +
                "-fx-pref-width: 250; " +
                "-fx-max-width: 250;");

        ImageView gameImage = new ImageView();
        gameImage.setFitWidth(250);
        gameImage.setFitHeight(150);
        gameImage.setPreserveRatio(true);

        loadSessionImage(gameImage, session);

        Label gameLabel = new Label(session.getGame());
        gameLabel.setStyle("-fx-text-fill: white; " +
                "-fx-font-size: 20px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 0 5 0;");

        Label priceLabel = new Label("Prix: " + session.getprix() + " DT");
        priceLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");

        Label durationLabel = new Label("Durée: " + session.getduree_session());
        durationLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");

        Button checkAvailabilityButton = createCheckAvailabilityButton(session.getId());

        sessionCard.getChildren().addAll(gameImage, gameLabel, priceLabel, durationLabel, checkAvailabilityButton);
        return sessionCard;
    }

    private void loadSessionImage(ImageView imageView, Session_game session) {
        if (session.getImageName() != null && !session.getImageName().isEmpty()) {
            String encodedImageName = URLEncoder.encode(session.getImageName(), StandardCharsets.UTF_8).replace("+", "%20");
            String imageUrl = IMAGE_BASE_URL + encodedImageName;
            try {
                Image image = new Image(imageUrl, true);
                image.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        LOGGER.error("Error loading image from " + imageUrl);
                        setDefaultImage(imageView);
                    }
                });
                image.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == 1.0 && !image.isError()) {
                        imageView.setImage(image);
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Exception loading image from " + imageUrl, e);
                setDefaultImage(imageView);
            }
        } else {
            setDefaultImage(imageView);
        }
    }

    private void setDefaultImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream(DEFAULT_IMAGE_PATH));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            LOGGER.error("Failed to load default image", e);
            imageView.setImage(new Image("https://via.placeholder.com/250x150.png?text=Image+Introuvable"));
        }
    }

    private Button createCheckAvailabilityButton(int sessionId) {
        Button button = new Button("Voir disponibilité");
        button.setStyle("-fx-background-color: #0585e6; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 20;");

        button.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/verifier_reservation.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) button.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                LOGGER.error("Error opening reservation verification", e);
            }
        });
        return button;
    }
    @FXML
    public void openChatbotDialog() {
        if (SessionManager.getInstance().isLoggedIn()) {
            Stage chatbotStage = new Stage();
            chatbotStage.setTitle("Chatbot LevelOP");
            chatbotStage.initModality(Modality.APPLICATION_MODAL);

            VBox chatbotLayout = new VBox(10);
            chatbotLayout.setStyle("-fx-background-color: #1E1E2E; -fx-padding: 20; -fx-spacing: 10;");

            TextArea chatArea = new TextArea();
            chatArea.setStyle("-fx-background-color: #2A2C3E; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 10; -fx-font-size: 14px; -fx-text-fill: #000000; -fx-wrap-text: true;");
            chatArea.setEditable(false);
            chatArea.setWrapText(true);
            chatArea.setPrefHeight(300);

            TextField userInput = new TextField();
            userInput.setStyle("-fx-background-color: #2A2C3E; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 10; -fx-font-size: 14px; -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #6c757d;");
            userInput.setPromptText("Posez une question...");

            Button sendButton = new Button("Envoyer");
            sendButton.setStyle("-fx-background-color: #FF4081; -fx-text-fill: #FFFFFF; -fx-background-radius: 10; -fx-padding: 10 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
            sendButton.setOnAction(e -> {
                String userMessage = userInput.getText();
                if (!userMessage.isEmpty()) {
                    chatArea.appendText("Vous : " + userMessage + "\n");
                    userInput.clear();

                    new Thread(() -> {
                        String response = getChatbotResponse(userMessage);
                        Platform.runLater(() -> animateText(chatArea, "Chatbot : " + response + "\n"));
                    }).start();
                }
            });

            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #1E1E2E; -fx-background-radius: 10; -fx-padding: 10 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
            closeButton.setOnAction(e -> chatbotStage.close());

            HBox buttonBox = new HBox(10, sendButton, closeButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            chatbotLayout.getChildren().addAll(chatArea, userInput, buttonBox);
            Scene scene = new Scene(chatbotLayout, 400, 400);
            chatbotStage.setScene(scene);
            chatbotStage.show();
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Information", "Vous devez être connecté pour accéder au chatbot");
        }
    }

    private void animateText(TextArea textArea, String text) {
        final StringBuilder displayedText = new StringBuilder(textArea.getText());
        for (int i = 0; i < text.length(); i++) {
            final int index = i;
            Timeline timeline = new Timeline();
            KeyFrame keyFrame = new KeyFrame(Duration.millis(50 * (index + 1)), event -> {
                displayedText.append(text.charAt(index));
                textArea.setText(displayedText.toString());
            });
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String getChatbotResponse(String message) {
        String apiKey = dotenv.get("OPENAI_API_KEY");
        String apiUrl = dotenv.get("OPENAI_API_URL");

        if (apiKey == null || apiUrl == null) {
            return "Error: API credentials not found in environment variables. Please check your Configuration file.";
        }

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject data = new JSONObject();
            data.put("model", "gpt-4");
            data.put("messages", new org.json.JSONArray()
                    .put(new JSONObject().put("role", "user").put("content", message)));

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = data.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

        } catch (Exception e) {
            LOGGER.error("Error communicating with API", e);
            return "Erreur lors de la communication avec l'API: " + e.getMessage();
        }
    }
}
