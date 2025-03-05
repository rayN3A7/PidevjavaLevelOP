package tn.esprit.Controllers.Coach;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.stripe.Stripe;

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
import tn.esprit.Services.EmailService;
import tn.esprit.Services.ServiceReservation;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

public class MySessionsController {

    @FXML
    private VBox mySessionsContainer;

    private final ServiceReservation serviceReservation = new ServiceReservation();
    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        try {
            // Initialiser Stripe avec votre clé API (utilisez une clé test pour le développement)
            Stripe.apiKey = "sk_test_51QwnFBQo8eHPYc0vBA9m5i0nBxdhefRpQrwYyk8VAPRg21d2UKSRiUJsR7T7VIFAlyeiDuQaZRwSCeQveeOETK9q00BDEThZIx"; // Remplacez par votre clé test réelle
            loadMyReservations();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur d'initialisation",
                    "Impossible de charger les réservations : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void loadMyReservations() {
        try {
            int clientId = SessionManager.getInstance().getUserId();
            if (clientId <= 0) {
                showAlert("Erreur", "Utilisateur non connecté", Alert.AlertType.WARNING);
                return;
            }

            List<Reservation> reservations = serviceReservation.getReservationsByClientId(clientId);
            if (reservations == null || reservations.isEmpty()) {
                mySessionsContainer.getChildren().clear();
                mySessionsContainer.getChildren().add(new Label("Aucune réservation trouvée."));
                return;
            }

            mySessionsContainer.getChildren().clear();
            for (Reservation reservation : reservations) {
                Session_game session = reservation.getSession();
                if (session == null) {
                    continue; // Ignorer si la session est nulle
                }

                VBox sessionCard = new VBox(10);
                sessionCard.setStyle("-fx-background-color: #162942; " +
                        "-fx-padding: 20; " +
                        "-fx-background-radius: 10; " +
                        "-fx-margin: 10;");

                Label gameLabel = new Label("Jeu: " + session.getGame());
                gameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

                Label priceLabel = new Label("Prix: " + session.getprix() + " DT");
                priceLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");

                Label durationLabel = new Label("Durée: " + session.getduree_session());
                durationLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");

                Label dateLabel = new Label("Date de réservation: " + reservation.getdate_reservation());
                dateLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");

                HBox buttonContainer = new HBox(10);
                buttonContainer.setAlignment(Pos.CENTER_LEFT);

                Button paypalButton = new Button("Payer avec PayPal");
                paypalButton.setStyle("-fx-background-color: #0070ba; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 20;");
                paypalButton.setOnAction(e -> handlePayPalPayment(reservation.getId(), session.getprix()));

                Button stripeButton = new Button("Payer avec Stripe");
                stripeButton.setStyle("-fx-background-color: #5469d4; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 20;");
                stripeButton.setOnAction(e -> handleStripePayment(reservation.getId(), session.getprix()));

                Button cancelButton = new Button("Annuler la réservation");
                cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 20;");
                cancelButton.setOnAction(e -> handleCancellation(reservation));

                buttonContainer.getChildren().addAll(paypalButton, stripeButton, cancelButton);
                sessionCard.getChildren().addAll(gameLabel, priceLabel, durationLabel, dateLabel, buttonContainer);
                mySessionsContainer.getChildren().add(sessionCard);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des réservations : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void backToSessions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/search_session.fxml"));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("Le fichier session.fxml n'a pas été trouvé.");
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) mySessionsContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner aux sessions : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handlePayPalPayment(int reservationId, double amount) {
        try {

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

            // Ouvrir le navigateur par défaut avec l'URL PayPal
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

    private void handleStripePayment(int reservationId, double amount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/stripe_payment_form.fxml"));
            Parent root = loader.load();

            // Get the controller and initialize data
            StripePaymentFormController controller = loader.getController();
            controller.initData(reservationId, amount);

            // Create and configure the new stage
            Stage stage = new Stage();
            stage.setTitle("Paiement Stripe");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur",
                    "Erreur lors de l'ouverture du formulaire de paiement : " + e.getMessage(),
                    Alert.AlertType.ERROR);
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
                String additionalInfo = String.format(
                        "La réservation pour la session de %s a été annulée par le client.\n\n" +
                                "Détails de la session :\n" +
                                "- Jeu : %s\n" +
                                "- Date : %s\n" +
                                "- Durée : %s",
                        session.getGame(),
                        session.getGame(),
                        reservation.getdate_reservation(),
                        session.getduree_session()
                );

                // Utiliser EmailService pour envoyer l'email automatiquement
                EmailService.sendEmail(
                        coach.getEmail(),
                        subject,
                        "custom", // Nouveau type pour message personnalisé
                        additionalInfo
                );

                // Supprimer la réservation
                serviceReservation.delete(reservation);

                // Recharger la liste des réservations
                loadMyReservations();

                showAlert("Succès",
                        "La réservation a été annulée et le coach a été notifié par email.",
                        Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erreur",
                    "Erreur lors de l'annulation : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}