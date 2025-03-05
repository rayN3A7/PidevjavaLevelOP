package tn.esprit.Controllers.Coach;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Models.Session_game;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.ServiceSession;
import tn.esprit.Services.UtilisateurService;

public class CoachSearchController {
    @FXML
    private ComboBox<String> coachIdField;
    @FXML
    private Label resultLabel;

    private final List<Utilisateur> Coach = new ArrayList<>(); // Marqué comme final pour éviter les modifications accidentelles
    private final ServiceSession serviceSession = new ServiceSession();
    private final UtilisateurService us = new UtilisateurService(); // Marqué comme final

    @FXML
    public void initialize() {
        GetCoach();
    }

    public void GetCoach() {
        Coach.addAll(us.getByRole("coach")); // Utilisation de addAll pour éviter une réassignation
        List<String> lcoach = Coach.stream()
                .map(Utilisateur::getNom) // Méthode reference au lieu de lambda
                .toList();
        coachIdField.getItems().setAll(lcoach);
    }

    @FXML
    private void searchSessionsByCoach() {
        try {
            String Coachname = coachIdField.getValue();
            Utilisateur selectedCoach = Coach.stream()
                    .filter(coach -> coach.getNom().equals(Coachname))
                    .findFirst()
                    .orElse(null);

            if (selectedCoach != null) {
                Utilisateur e1 = us.getByEmail(selectedCoach.getEmail());
                List<Session_game> sessions = serviceSession.getSessionsByCoachId(e1.getId());

                if (sessions.isEmpty()) {
                    resultLabel.setText("Aucune session trouvée pour ce coach.");
                } else {
                    // Clear previous content
                    resultLabel.setText("");

                    VBox sessionsContainer = new VBox(15);
                    sessionsContainer.setStyle("-fx-padding: 10 0;");

                    for (Session_game session : sessions) {
                        VBox sessionCard = createSessionCard(session);
                        sessionsContainer.getChildren().add(sessionCard);
                    }

                    if (resultLabel.getParent() instanceof Pane) {
                        ((Pane) resultLabel.getParent()).getChildren().add(sessionsContainer);
                    }
                }
            } else {
                resultLabel.setText("Aucun coach sélectionné.");
            }
        } catch (Exception e) {
            resultLabel.setText("Une erreur s'est produite lors de la recherche.");
            // Remplacer printStackTrace() par un logger (exemple avec SLF4J)
            // logger.error("Erreur lors de la recherche des sessions par coach : ", e);
            e.printStackTrace(); // Temporairement conservé
        }
    }

    private VBox createSessionCard(Session_game session) {
        VBox sessionCard = new VBox(8);
        sessionCard.setStyle("-fx-background-color: #162942; " +
                "-fx-padding: 15; " +
                "-fx-background-radius: 8;");

        Label gameLabel = new Label("Jeu: " + session.getGame());
        gameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label priceLabel = new Label("Prix: " + session.getprix() + " DT");
        priceLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");

        Label durationLabel = new Label("Durée: " + session.getduree_session());
        durationLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");

        Button checkButton = createCheckButton(session.getId());

        sessionCard.getChildren().addAll(gameLabel, priceLabel, durationLabel, checkButton);
        return sessionCard;
    }

    private Button createCheckButton(int sessionId) {
        Button button = new Button("Vérifier disponibilité");
        button.setStyle("-fx-background-color: #0585e6; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 8 15; " +
                "-fx-background-radius: 20;");

        button.setOnAction(this::navigateToVerification); // Méthode reference
        return button;
    }

    private void navigateToVerification(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/verifier_reservation.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Temporairement conservé
        }
    }

    @FXML
    private void backToSessions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/search_session.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) coachIdField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Temporairement conservé
        }
    }
}