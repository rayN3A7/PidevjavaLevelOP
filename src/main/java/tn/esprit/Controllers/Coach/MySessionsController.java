package tn.esprit.Controllers.Coach;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
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

import java.awt.Desktop;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/session.fxml"));
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
            String paypalUrl = String.format("https://www.paypal.com/cgi-bin/webscr" +
                            "?cmd=_xclick" +
                            "&business=%s" +
                            "&item_name=%s" +
                            "&amount=%.2f" +
                            "¤cy_code=EUR",
                    "your-paypal-email@example.com", // Remplacez par votre email PayPal
                    "Réservation #" + reservationId,
                    amount);

            Desktop.getDesktop().browse(new URI(paypalUrl));
            showAlert("Paiement PayPal", "Redirection vers PayPal pour le paiement.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du paiement PayPal : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleStripePayment(int reservationId, double amount) {
        try {
            // Vérifier que le montant est valide (minimum 0.50 EUR, soit 50 cents)
            long amountInCents = Math.max((long) (amount * 100), 50);

            // Créer un Checkout Session pour rediriger l'utilisateur vers la page Stripe
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://your-public-url/payment/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://your-public-url/payment/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("eur")
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Réservation #" + reservationId)
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            String checkoutUrl = session.getUrl();

            // Ouvrir l'URL dans le navigateur par défaut
            Desktop.getDesktop().browse(new URI(checkoutUrl));

            showAlert("Paiement initié",
                    "La page de paiement Stripe va s'ouvrir dans votre navigateur.",
                    Alert.AlertType.INFORMATION);
        } catch (StripeException se) {
            showAlert("Erreur Stripe",
                    "Problème avec Stripe : " + se.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur",
                    "Erreur inattendue lors du paiement Stripe : " + e.getMessage(),
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
}