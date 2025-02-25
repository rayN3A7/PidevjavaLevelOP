package tn.esprit.Controllers.Coach;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Models.Reservation;
import tn.esprit.Models.Session_game;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.ServiceReservation;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MySessionsController {

    @FXML
    private VBox mySessionsContainer;

    private final ServiceReservation serviceReservation = new ServiceReservation();
    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        loadMyReservations();
    }

    private void loadMyReservations() {
        // Obtenir l'ID du client connecté
        int clientId = SessionManager.getInstance().getUserId();
        
        // Obtenir les réservations du client
        List<Reservation> reservations = serviceReservation.getReservationsByClientId(clientId);
        
        mySessionsContainer.getChildren().clear();
        
        for (Reservation reservation : reservations) {
            Session_game session = reservation.getSession();
            
            // Créer une carte pour chaque réservation
            VBox sessionCard = new VBox(10);
            sessionCard.setStyle("-fx-background-color: #162942; " +
                               "-fx-padding: 20; " +
                               "-fx-background-radius: 10; " +
                               "-fx-margin: 10;");
            
            // Informations de la session
            Label gameLabel = new Label("Jeu: " + session.getGame());
            gameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label priceLabel = new Label("Prix: " + session.getprix() + " DT");
            priceLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");
            
            Label durationLabel = new Label("Durée: " + session.getduree_session());
            durationLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");
            
            Label dateLabel = new Label("Date de réservation: " + reservation.getdate_reservation());
            dateLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");
            
            // Créer un HBox pour contenir les boutons
            HBox buttonContainer = new HBox(10);
            buttonContainer.setAlignment(Pos.CENTER_LEFT);
            
            // Bouton de paiement existant
            Button payButton = new Button("Payer maintenant");
            payButton.setStyle("-fx-background-color: #2ecc71; " +
                             "-fx-text-fill: white; " +
                             "-fx-font-size: 14px; " +
                             "-fx-padding: 8 20; " +
                             "-fx-background-radius: 20; ");

            // Nouveau bouton d'annulation
            Button cancelButton = new Button("Annuler la réservation");
            cancelButton.setStyle("-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 14px; " +
                                "-fx-padding: 8 20; " +
                                "-fx-background-radius: 20; "
                                );

            final int reservationId = reservation.getId();
            payButton.setOnAction(e -> handlePayment(reservationId, session.getprix()));
            cancelButton.setOnAction(e -> handleCancellation(reservation));

            buttonContainer.getChildren().addAll(payButton, cancelButton);
            
            sessionCard.getChildren().addAll(
                gameLabel,
                priceLabel,
                durationLabel,
                dateLabel,
                buttonContainer
            );
            
            mySessionsContainer.getChildren().add(sessionCard);
        }
    }

    private void handlePayment(int reservationId, double amount) {
        try {
            // Construire l'URL PayPal avec les paramètres
            String baseUrl = "https://www.sandbox.paypal.com/cgi-bin/webscr";
            String business = "votre_email_business_sandbox@test.com"; // Email PayPal sandbox
            String itemName = "Session de coaching #" + reservationId;
            String returnUrl = "http://localhost:8080/success";
            String cancelUrl = "http://localhost:8080/cancel";

            String paypalUrl = String.format("%s?cmd=_xclick&business=%s&item_name=%s&amount=%.2f&currency_code=EUR&return=%s&cancel_return=%s",
                baseUrl,
                URLEncoder.encode(business, StandardCharsets.UTF_8),
                URLEncoder.encode(itemName, StandardCharsets.UTF_8),
                amount,
                URLEncoder.encode(returnUrl, StandardCharsets.UTF_8),
                URLEncoder.encode(cancelUrl, StandardCharsets.UTF_8));

            
            Desktop.getDesktop().browse(new URI(paypalUrl));

            showAlert(
                "Redirection PayPal", 
                "Vous allez être redirigé vers PayPal pour effectuer le paiement de " + amount + " €", 
                Alert.AlertType.INFORMATION
            );

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(
                "Erreur", 
                "Erreur lors de la redirection vers PayPal. Veuillez réessayer plus tard.", 
                Alert.AlertType.ERROR
            );
        }
    }

    private void handleCancellation(Reservation reservation) {
        try {
            // Récupérer les informations du coach
            Session_game session = reservation.getSession();
            int coachId = session.getCoach_id();
            Utilisateur coach = utilisateurService.getOne(coachId);
            
            if (coach != null) {
                // Préparer le contenu de l'email
                String subject = "Annulation de réservation de session";
                String body = String.format(
                    "Bonjour,\n\nLa réservation pour la session de %s a été annulée par le client.\n\nDétails de la session :\n" +
                    "- Jeu : %s\n" +
                    "- Date : %s\n" +
                    "- Durée : %s\n\n" +
                    "Cordialement.",
                    session.getGame(),
                    session.getGame(),
                    reservation.getdate_reservation(),
                    session.getduree_session()
                );

                // Encoder les paramètres pour l'URL mailto
                subject = URLEncoder.encode(subject, "UTF-8");
                body = URLEncoder.encode(body, "UTF-8");

                // Créer l'URL mailto
                String mailtoUrl = String.format("mailto:%s?subject=%s&body=%s",
                    coach.getEmail(), subject, body);

                // Ouvrir le client email par défaut
                Desktop.getDesktop().mail(new URI(mailtoUrl));

                // Supprimer la réservation
                serviceReservation.delete(reservation);

                // Rafraîchir l'affichage
                loadMyReservations();

                showAlert("Succès", "La réservation a été annulée et le coach a été notifié.", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'annulation : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void backToSessions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/session.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) mySessionsContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la navigation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 