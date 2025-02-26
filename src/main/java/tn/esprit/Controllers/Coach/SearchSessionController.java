package tn.esprit.Controllers.Coach;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceReservation;
import tn.esprit.Services.ServiceSession;
import tn.esprit.utils.SessionManager;

public class SearchSessionController {
    @FXML
    private VBox sessionsContainer;

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
        sessionsContainer.setSpacing(15);

        for (Session_game session : sessions) {
            VBox sessionCard = createSessionCard(session);
            sessionsContainer.getChildren().add(sessionCard);
        }
    }

    private VBox createSessionCard(Session_game session) {
        VBox sessionCard = new VBox(12);
        sessionCard.setStyle("-fx-background-color: #162942; " +
                "-fx-padding: 20; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        Label gameLabel = new Label(session.getGame());
        gameLabel.setStyle("-fx-text-fill: white; " +
                "-fx-font-size: 20px; " +
                "-fx-font-weight: bold;");

        VBox infoBox = new VBox(8);
        Label priceLabel = createInfoLabel("Prix: " + session.getprix() + " DT");
        Label durationLabel = createInfoLabel("Durée: " + session.getduree_session());

        infoBox.getChildren().addAll(priceLabel, durationLabel);

        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);
        buttonsBox.setStyle("-fx-padding: 10 0 0 0;");

        Button checkButton = createActionButton("Vérifier disponibilité", "#0585e6");
        Button reserveButton = createActionButton("Réserver", "#fe0369");
        reserveButton.setVisible(false);

        setupButtonActions(checkButton, reserveButton, session.getId());

        buttonsBox.getChildren().addAll(checkButton, reserveButton);

        sessionCard.getChildren().addAll(gameLabel, infoBox, buttonsBox);
        return sessionCard;
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
