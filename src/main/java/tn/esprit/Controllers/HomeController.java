package tn.esprit.Controllers;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
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
import tn.esprit.Controllers.forum.QuestionDetailsController;
import tn.esprit.Models.Question;
import tn.esprit.Services.QuestionService;
import tn.esprit.utils.SessionManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

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

    @FXML
    public void initialize() {
        LOGGER.info("Initializing HomeController...");
        loadTopQuestions();
        initializeCarousel();
        updateCarousel();
        addPulsatingGlow(titleLabel, "#FFFFFF");
        addPulsatingGlow(title2Label, "#FF4081");
        addDragSupport();
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
        card.getStyleClass().add("modern-question-card"); // Updated style class for modern design

        // Game Image (Circular with Border)
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
        } else {
            LOGGER.warn("No game image available for question ID: {}. Game: {}, ImagePath: {}",
                    question.getQuestion_id(),
                    question.getGame() != null ? question.getGame().getGame_name() : "null",
                    question.getGame() != null ? question.getGame().getImagePath() : "null");
        }
        gameImage.setMouseTransparent(true);
        imageContainer.getChildren().add(gameImage);

        // Title and Author
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

        // Fixed card size for consistency
        card.setMinWidth(300);
        card.setMaxWidth(300);
        card.setMinHeight(100);
        card.setMaxHeight(100);

        // Make the card clickable
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
        }
        updateIndicators();
    }

    @FXML
    private void previousQuestion() {
        if (currentIndex > 0) {
            currentIndex--;
            animateCarouselTransition(300);
        }
    }

    @FXML
    private void nextQuestion() {
        if (currentIndex < carouselPages.length - 1) {
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
            if (Math.abs(deltaX) > dragThreshold) {
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
            if (deltaY > 0 && currentIndex > 0) {
                previousQuestion();
            } else if (deltaY < 0 && currentIndex < carouselPages.length - 1) {
                nextQuestion();
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
