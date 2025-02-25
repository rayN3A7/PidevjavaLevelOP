package tn.esprit.Controllers.Coach;

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

import java.util.ArrayList;
import java.util.List;

public class CoachSearchController {
    @FXML
    private ComboBox<String> coachIdField;
    @FXML
    private Label resultLabel;
    
    private List<Utilisateur> Coach = new ArrayList<>();
    private final ServiceSession serviceSession = new ServiceSession();
    private UtilisateurService us = new UtilisateurService();

    @FXML
    public void initialize() {
        GetCoach();
    }

    public void GetCoach() {
        Coach = us.getByRole("coach");
        List<String> lcoach = Coach.stream().map(Utilisateur::getNom)
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

                    resultLabel.setText("");
                    

                    VBox sessionsContainer = new VBox(15);
                    sessionsContainer.setStyle("-fx-padding: 10 0;");

                    for (Session_game session : sessions) {

                        VBox sessionCard = new VBox(8);
                        sessionCard.setStyle("-fx-background-color: #162942; " +
                                          "-fx-padding: 15; " +
                                          "-fx-background-radius: 8;");

                        // Session details
                        Label gameLabel = new Label("Jeu: " + session.getGame());
                        gameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
                        
                        Label priceLabel = new Label("Prix: " + session.getprix() + " DT");
                        priceLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");
                        
                        Label durationLabel = new Label("Durée: " + session.getduree_session());
                        durationLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");

                        // Create check availability button
                        Button checkButton = new Button("Vérifier disponibilité");
                        checkButton.setStyle("-fx-background-color: #0585e6; " +
                                          "-fx-text-fill: white; " +
                                          "-fx-font-size: 14px; " +
                                          "-fx-padding: 8 15; " +
                                          "-fx-background-radius: 20; "
                                          );


                        final int sessionId = session.getId();
                        checkButton.setOnAction(event -> navigateToVerification(event));


                        sessionCard.getChildren().addAll(
                            gameLabel,
                            priceLabel,
                            durationLabel,
                            checkButton
                        );

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
            e.printStackTrace();
        }
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
} 