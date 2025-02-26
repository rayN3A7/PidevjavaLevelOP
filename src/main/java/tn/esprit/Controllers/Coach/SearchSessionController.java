package tn.esprit.Controllers.Coach;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceReservation;
import tn.esprit.Services.ServiceSession;
import tn.esprit.utils.SessionManager;

public class SearchSessionController {
    @FXML
    private FlowPane sessionsContainer;

    String userRole = SessionManager.getInstance().getRole().name();

    private final ServiceSession serviceSession = new ServiceSession();
    private final ServiceReservation serviceReservation = new ServiceReservation();

    @FXML
    public void initialize() {
        showSessions();
    }

    @FXML
    private void showSessions() {
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

        // Game image
        ImageView gameImage = new ImageView();
        String imagePath = getGameImagePath(session.getGame());
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            gameImage.setImage(image);
        } catch (Exception e) {
            // Fallback to placeholder if image not found
            Rectangle placeholder = new Rectangle(250, 150, Color.DARKGRAY);
            gameImage.setFitWidth(250);
            gameImage.setFitHeight(150);
        }
        gameImage.setFitWidth(250);
        gameImage.setFitHeight(150);
        gameImage.setPreserveRatio(true);

        Label gameLabel = new Label(session.getGame());
        gameLabel.setStyle("-fx-text-fill: white; " +
                "-fx-font-size: 20px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 0 5 0;");

        VBox infoBox = new VBox(8);
        Label priceLabel = createInfoLabel("Prix: " + session.getprix() + " DT");
        Label durationLabel = createInfoLabel("Durée: " + session.getduree_session());

        infoBox.getChildren().addAll(priceLabel, durationLabel);
        infoBox.setStyle("-fx-padding: 5 0;");

        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setStyle("-fx-padding: 10 0 0 0;");

        Button checkButton = createActionButton("Vérifier disponibilité", "#0585e6");
        Button reserveButton = createActionButton("Réserver", "#fe0369");
        reserveButton.setVisible(false);

        setupButtonActions(checkButton, reserveButton, session.getId());

        buttonsBox.getChildren().addAll(checkButton, reserveButton);

        sessionCard.getChildren().addAll(gameImage, gameLabel, infoBox, buttonsBox);
        return sessionCard;
    }

    private String getGameImagePath(String gameName) {
        Map<String, String> gameImages = new HashMap<>();
        gameImages.put("league of legends", "/assets/image/lol.jpg");
        gameImages.put("Mount & Blade", "/assets/image/mountandbladebannerlord.jpg");
        gameImages.put("fortnite", "/assets/image/fortnite.jpg");
        gameImages.put("For Honor", "/assets/image/forhonor.png");
        gameImages.put("cs", "/assets/image/cs.jpg");

        return gameImages.getOrDefault(gameName, "/images/default-game.jpg");
    }

    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #8899A6; " +
                "-fx-font-size: 14px;");
        return label;
    }

    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 20; " );
        return button;
    }

    private void setupButtonActions(Button checkButton, Button reserveButton, int sessionId) {
        checkButton.setOnAction(event -> {
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
    }

    @FXML
    private void session(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/session.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
    @FXML
    private void Coach(ActionEvent event) throws Exception {
        if(userRole.equals("COACH")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/SessionManagement.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }
    }
    @FXML
    private void goToCoachSearch(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/coach_search.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
