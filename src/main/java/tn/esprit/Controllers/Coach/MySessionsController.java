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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
                    "Impossible de charger les réservations : " + e.getMessage());
        }
    }

    private void loadMyReservations() {
        try {
            int clientId = SessionManager.getInstance().getUserId();
            if (clientId <= 0) {
                showAlert("Erreur", "Utilisateur non connecté");
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
            showAlert("Erreur", "Erreur lors du chargement des réservations : " + e.getMessage());
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
            showAlert("Erreur", "Impossible de retourner aux sessions : " + e.getMessage());
        }
    }


    private void handlePayPalPayment(int reservationId, double amount) {
        try {

            String baseUrl = "https://www.sandbox.paypal.com/cgi-bin/webscr";
            String business = "votre_email_business_sandbox@test.com";
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

            showSuccessAlert(
                    "Redirection PayPal",
                    "Vous allez être redirigé vers PayPal pour effectuer le paiement de " + amount + " €"

            );

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(
                    "Erreur",
                    "Erreur lors de la redirection vers PayPal. Veuillez réessayer plus tard."

            );
        }
    }

    private void handleStripePayment(int reservationId, double amount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/stripe_payment_form.fxml"));
            Parent root = loader.load();


            StripePaymentFormController controller = loader.getController();
            controller.initData(reservationId, amount);


            Stage stage = new Stage();
            stage.setTitle("Paiement Stripe");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur",
                    "Erreur lors de l'ouverture du formulaire de paiement : " + e.getMessage()
            );
        }
    }

    private void handleCancellation(Reservation reservation) {
        try {

            Session_game session = reservation.getSession();
            int coachId = session.getCoach_id();
            Utilisateur coach = utilisateurService.getOne(coachId);

            if (coach != null) {

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


                EmailService.sendEmail(
                        coach.getEmail(),
                        subject,
                        "custom",
                        additionalInfo
                );


                serviceReservation.delete(reservation);


                loadMyReservations();

                showSuccessAlert("Succès",
                        "La réservation a été annulée et le coach a été notifié par email."
                );
            }
        } catch (Exception e) {
            showAlert("Erreur",
                    "Erreur lors de l'annulation : " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
    private void showAlert(String title, String message) {
        showStyledAlert(title, message, "/forumUI/icons/alert.png", "/forumUI/icons/alert.png", "OK", 80, 80);
    }

    private void showSuccessAlert(String title, String message) {
        showStyledAlert(title, message, "/forumUI/icons/sucessalert.png", "/forumUI/icons/sucessalert.png", "OK", 60, 80);
    }
    private void showStyledAlert (String title, String message, String iconPath, String stageIconPath,
                                  String buttonText, double iconHeight, double iconWidth) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(new Image(getClass().getResource(iconPath).toExternalForm()));
        icon.setFitHeight(iconHeight);
        icon.setFitWidth(iconWidth);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource(stageIconPath).toExternalForm()));

        ButtonType okButton = new ButtonType(buttonText, ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        alert.showAndWait();
    }
}