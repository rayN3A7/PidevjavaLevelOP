package tn.esprit.Controllers.Coach;

import java.io.File;
import java.net.MalformedURLException;
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
import javafx.scene.control.Alert;
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
import tn.esprit.Services.ServiceSession;
import tn.esprit.utils.SessionManager;

public class SessionManagementController {

    @FXML private FlowPane sessionsContainer;
    private int currentCoachId = SessionManager.getInstance().getUserId();
    private String roleuser = SessionManager.getInstance().getRole().name();
    private final ServiceSession serviceSession = new ServiceSession();
    private static final String IMAGE_BASE_DIR = "C:\\xampp\\htdocs\\img\\games\\"; // Directory path
    private static final String DEFAULT_IMAGE_PATH = "/images/default-game.jpg"; // Resource path for default image

    @FXML
    public void initialize() {
        showSessions();
    }

    @FXML
    private void navigateToAddSession(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/AddSession.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        if (session.getImageName() != null && !session.getImageName().isEmpty()) {
            String imagePath = IMAGE_BASE_DIR + session.getImageName();
            try {
                Image image = loadImageFromFile(imagePath);
                gameImage.setImage(image);
            } catch (Exception e) {
                setDefaultImage(gameImage);
                System.err.println("Erreur lors du chargement de l'image personnalisée : " + imagePath + " - Utilisation de l'image par défaut.");
            }
        } else {
            setDefaultImage(gameImage);
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

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setStyle("-fx-padding: 10 0 0 0;");

        if (session.getCoach_id() == currentCoachId) {
            Button updateButton = createActionButton("Modifier", "#0585e6");
            Button deleteButton = createActionButton("Supprimer", "#fe0369");

            updateButton.setOnAction(event -> navigateToUpdate(session.getId()));
            deleteButton.setOnAction(event -> deleteSession(session));

            buttonsBox.getChildren().addAll(updateButton, deleteButton);
        } else {
            Label ownerLabel = createInfoLabel("Session d'un autre coach");
            buttonsBox.getChildren().add(ownerLabel);
        }

        sessionCard.getChildren().addAll(gameImage, gameLabel, infoBox, buttonsBox);
        return sessionCard;
    }

    private Image loadImageFromFile(String filePath) throws MalformedURLException {
        File file = new File(filePath);
        if (file.exists()) {
            return new Image(file.toURI().toURL().toString());
        } else {
            throw new IllegalArgumentException("Fichier image non trouvé : " + filePath);
        }
    }

    private void setDefaultImage(ImageView imageView) {
        Image defaultImage = loadDefaultImage();
        if (defaultImage != null) {
            imageView.setImage(defaultImage);
        } else {
            // Utiliser une image codée en dur ou une couleur de fond
            imageView.setImage(new Image("/images/fallback-image.jpg")); // Assurez-vous que cette image existe
            System.err.println("Utilisation de l'image de secours : /images/fallback-image.jpg");
        }
    }

    private Image loadDefaultImage() {
        try {
            System.out.println("Tentative de chargement de l'image par défaut : " + DEFAULT_IMAGE_PATH);
            return new Image(getClass().getResourceAsStream(DEFAULT_IMAGE_PATH));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image par défaut : " + DEFAULT_IMAGE_PATH);
            return null; // Ou utilisez une image codée en dur si nécessaire
        }
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
                "-fx-background-radius: 20;");
        return button;
    }

    private void navigateToUpdate(int sessionId) {
        try {
            Session_game session = serviceSession.getSessionById(sessionId);
            if (session.getCoach_id() != currentCoachId) {
                showAlert("Erreur", "Vous ne pouvez pas modifier une session qui ne vous appartient pas", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/SessionUpdate.fxml"));
            Parent root = loader.load();

            SessionUpdateController controller = loader.getController();
            controller.initData(sessionId);

            Scene scene = new Scene(root);
            Stage stage = (Stage) sessionsContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la navigation vers la mise à jour", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void deleteSession(Session_game session) {
        try {
            if (session.getCoach_id() != currentCoachId) {
                showAlert("Erreur", "Vous ne pouvez pas supprimer une session qui ne vous appartient pas", Alert.AlertType.ERROR);
                return;
            }

            serviceSession.delete(session);
            showSessions();
            showAlert("Succès", "Session supprimée avec succès", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la suppression", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void viewReservedSessions(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/reserved_sessions.fxml"));
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