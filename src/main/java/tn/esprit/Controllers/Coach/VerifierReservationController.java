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
import tn.esprit.Services.ServiceReservation;
import tn.esprit.Services.ServiceSession;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class VerifierReservationController {

    @FXML
    private FlowPane sessionsContainer;

    private final ServiceSession serviceSession = new ServiceSession();
    private final ServiceReservation serviceReservation = new ServiceReservation();
    private static final String IMAGE_BASE_URL = "http://localhost/img/";
    private static final String DEFAULT_IMAGE_PATH = "/images/default-game.jpg";

    @FXML
    public void initialize() {
        showAllSessions();
    }

    private void showAllSessions() {
        List<Session_game> sessions = serviceSession.getAll();
        sessionsContainer.getChildren().clear();
        sessionsContainer.setHgap(15);
        sessionsContainer.setVgap(15);

        for (Session_game session : sessions) {
            VBox sessionCard = createSessionCard(session);
            sessionsContainer.getChildren().add(sessionCard);
        }
    }

    private VBox createSessionCard(Session_game session) {
        VBox sessionCard = new VBox(12);
        sessionCard.setPrefWidth(300);
        sessionCard.setStyle("-fx-background-color: #162942; " +
                "-fx-padding: 20; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");

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

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 14px;");

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setStyle("-fx-padding: 10 0 0 0;");

        Button checkButton = new Button("Vérifier la disponibilité");
        checkButton.setStyle("-fx-background-color: #0585e6; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 10px; " +
                "-fx-padding: 8 15; " +
                "-fx-background-radius: 20;");

        Button reserveButton = new Button("Réserver");
        reserveButton.setStyle("-fx-background-color: #fe0369; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 10px; " +
                "-fx-padding: 8 15; " +
                "-fx-background-radius: 20;");
        reserveButton.setVisible(false);

        final int sessionId = session.getId();
        checkButton.setOnAction(event -> {
            boolean isReserved = serviceReservation.isSessionAlreadyReserved(sessionId);
            if (isReserved) {
                statusLabel.setText("Cette session est déjà réservée");
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
                reserveButton.setVisible(false);
            } else {
                statusLabel.setText("Cette session est disponible");
                statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 14px;");
                reserveButton.setVisible(true);
            }
        });

        reserveButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/reservation.fxml"));
                Parent root = loader.load();
                ReservationController reservationController = loader.getController();
                reservationController.initData(sessionId);
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        buttonsBox.getChildren().addAll(checkButton, reserveButton);

        sessionCard.getChildren().addAll(gameImage, gameLabel, priceLabel, durationLabel, statusLabel, buttonsBox);
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

    @FXML
    private void hazem(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/reservation.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @FXML
    private void mmmm(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/search_session.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }
}