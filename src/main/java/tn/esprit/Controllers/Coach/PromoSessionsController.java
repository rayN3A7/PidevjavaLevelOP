package tn.esprit.Controllers.Coach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceSession;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PromoSessionsController {

    @FXML
    private FlowPane promoSessionsVBox; // Changé de VBox à FlowPane

    private final ServiceSession serviceSession = new ServiceSession();
    private static final String IMAGE_BASE_URL = "http://localhost/img/games/";
    private static final String DEFAULT_IMAGE_PATH = "/images/default-game.jpg";

    @FXML
    public void initialize() {
        showPromoSessions();
    }

    private void showPromoSessions() {
        try {
            List<Session_game> promoSessions = serviceSession.getSessionsInPromo();
            promoSessionsVBox.getChildren().clear();

            if (promoSessions == null || promoSessions.isEmpty()) {
                Label noSessionsLabel = new Label("Aucune session en promotion trouvée.");
                noSessionsLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 16px;");
                promoSessionsVBox.getChildren().add(noSessionsLabel);
                return;
            }

            for (Session_game session : promoSessions) {
                VBox sessionCard = createSessionCard(session);
                promoSessionsVBox.getChildren().add(sessionCard);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement des sessions en promotion.");
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 16px;");
            promoSessionsVBox.getChildren().add(errorLabel);
        }
    }

    private VBox createSessionCard(Session_game session) {
        VBox sessionCard = new VBox(10);
        sessionCard.setStyle("-fx-background-color: #162942; " +
                "-fx-padding: 20; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5); " +
                "-fx-pref-width: 250; " + // Largeur fixe pour uniformité dans FlowPane
                "-fx-max-width: 250;");   // Limite la largeur pour éviter l'étirement

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
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return button;
    }

    @FXML
    private void backToSearch(ActionEvent event) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/search_session.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}