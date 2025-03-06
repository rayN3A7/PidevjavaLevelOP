package tn.esprit.Controllers.Coach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceSession;
import tn.esprit.utils.SessionManager;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SearchSessionController {

    @FXML
    private FlowPane sessionsContainer;

    private final String userRole = SessionManager.getInstance().getRole().name();
    private final ServiceSession serviceSession = new ServiceSession();
    private static final String IMAGE_BASE_URL = "http://localhost/img/games/";
    private static final String DEFAULT_IMAGE_PATH = "/images/default-game.jpg";

    @FXML
    public void initialize() {
        showSessions();
    }

    @FXML
    private void showSessions() {
        try {
            List<Session_game> sessions = serviceSession.getAll();
            sessionsContainer.getChildren().clear();
            sessionsContainer.setHgap(15);
            sessionsContainer.setVgap(15);

            for (Session_game session : sessions) {
                VBox sessionCard = createSessionCard(session);
                sessionsContainer.getChildren().add(sessionCard);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des sessions : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createSessionCard(Session_game session) {
        VBox sessionCard = new VBox(12);
        sessionCard.setPrefWidth(300);
        sessionCard.setStyle("-fx-background-color: #162942; -fx-padding: 20; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        ImageView gameImage = new ImageView();
        gameImage.setFitWidth(250);
        gameImage.setFitHeight(150);
        gameImage.setPreserveRatio(true);

        loadSessionImage(gameImage, session);

        Label gameLabel = new Label(session.getGame());
        gameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        VBox infoBox = new VBox(8);
        Label priceLabel = createInfoLabel("Prix: " + session.getprix() + " DT");
        Label durationLabel = createInfoLabel("Durée: " + session.getduree_session());
        infoBox.getChildren().addAll(priceLabel, durationLabel);
        infoBox.setStyle("-fx-padding: 5 0;");

        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setStyle("-fx-padding: 10 0 0 0;");

        Button checkButton = createActionButton("Découvrir", "#0585e6");
        setupButtonActions(checkButton, session.getId());
        buttonsBox.getChildren().add(checkButton);

        sessionCard.getChildren().addAll(gameImage, gameLabel, infoBox, buttonsBox);
        return sessionCard;
    }

    private void loadSessionImage(ImageView imageView, Session_game session) {
        if (session.getImageName() != null && !session.getImageName().isEmpty()) {
            // Encode l'URL pour gérer les espaces et caractères spéciaux
            String encodedImageName = URLEncoder.encode(session.getImageName(), StandardCharsets.UTF_8).replace("+", "%20");
            String imageUrl = IMAGE_BASE_URL + encodedImageName;
            try {
                System.out.println("Chargement de l'image depuis : " + imageUrl);
                Image image = new Image(imageUrl, true);
                image.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        System.err.println("Erreur de chargement de l'image depuis " + imageUrl + " : " + image.getException());
                        setDefaultImage(imageView);
                    }
                });
                image.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == 1.0 && !image.isError()) {
                        imageView.setImage(image);
                    }
                });
            } catch (Exception e) {
                System.err.println("Exception lors du chargement de l'image depuis " + imageUrl + " : " + e.getMessage());
                setDefaultImage(imageView);
            }
        } else {
            setDefaultImage(imageView);
        }
    }

    private void setDefaultImage(ImageView imageView) {
        Image defaultImage = loadDefaultImage();
        imageView.setImage(defaultImage);
    }

    private Image loadDefaultImage() {
        try {
            System.out.println("Chargement de l'image par défaut depuis : " + DEFAULT_IMAGE_PATH);
            Image image = new Image(getClass().getResourceAsStream(DEFAULT_IMAGE_PATH));
            if (image.isError()) {
                throw new IllegalStateException("Erreur de chargement de l'image par défaut : " + image.getException());
            }
            return image;
        } catch (Exception e) {
            System.err.println("Échec du chargement de l'image par défaut : " + e.getMessage());

            return new Image("https://via.placeholder.com/250x150.png?text=Image+Introuvable");
        }
    }

    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");
        return label;
    }

    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 20;");
        return button;
    }

    private void setupButtonActions(Button checkButton, int sessionId) {
        checkButton.setOnAction(event -> navigateToFXML(event, "/Coach/verifier_reservation.fxml", controller -> {
            if (controller instanceof ReservationController) {
                ((ReservationController) controller).initData(sessionId);
            }
        }));
    }

    @FXML
    private void Coach(ActionEvent event) {
        if ("COACH".equals(userRole)) {
            navigateToFXML(event, "/Coach/SessionManagement.fxml", null);
        }
    }

    @FXML
    private void goToCoachSearch(ActionEvent event) {
        navigateToFXML(event, "/Coach/coach_search.fxml", null);
    }

    @FXML
    private void goToGameSearch(ActionEvent event) {
        navigateToFXML(event, "/Coach/game_search_session.fxml", null);
    }

    @FXML
    private void goToPromoSessions(ActionEvent event) {
        navigateToFXML(event, "/Coach/promo_sessions.fxml", null);
    }



    private void navigateToFXML(ActionEvent event, String fxmlPath, java.util.function.Consumer<Object> controllerCallback) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                throw new IllegalArgumentException("Fichier FXML introuvable à " + fxmlPath);
            }
            Parent root = loader.load();
            if (controllerCallback != null) {
                controllerCallback.accept(loader.getController());
            }
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de " + fxmlPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }
}