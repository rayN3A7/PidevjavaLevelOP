package tn.esprit.Controllers.Coach;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import tn.esprit.Models.Reservation;
import tn.esprit.Models.Session_game;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.EmailService;
import tn.esprit.Services.ServiceReservation;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReservedSessionsController {
    @FXML
    private VBox reservationsContainer;

    private final ServiceReservation serviceReservation = new ServiceReservation();
    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        loadReservedSessions();
    }

    private void loadReservedSessions() {

        int coachId = SessionManager.getInstance().getUserId();

        List<Reservation> reservations = serviceReservation.getReservationsByCoachId(coachId);

        reservationsContainer.getChildren().clear();

        for (Reservation reservation : reservations) {
            Session_game session = reservation.getSession();
            Utilisateur client = utilisateurService.getOne(reservation.getClient_id());

            VBox reservationCard = new VBox(10);
            reservationCard.setStyle("-fx-background-color: #162942; " +
                    "-fx-padding: 20; " +
                    "-fx-background-radius: 10; " +
                    "-fx-margin: 10;");


            Label gameLabel = new Label("Jeu: " + session.getGame());
            gameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

            Label priceLabel = new Label("Prix: " + session.getprix() + " DT");
            priceLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");

            Label durationLabel = new Label("Durée: " + session.getduree_session());
            durationLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");

            Label clientLabel = new Label("Client: " + client.getNom() + " " + client.getPrenom());
            clientLabel.setStyle("-fx-text-fill: #fe0369; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label emailLabel = new Label("Email: " + client.getEmail());
            emailLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");

            Label dateLabel = new Label("Date de réservation: " + reservation.getdate_reservation());
            dateLabel.setStyle("-fx-text-fill: #8899A6; -fx-font-size: 14px;");

            reservationCard.getChildren().addAll(
                    gameLabel,
                    priceLabel,
                    durationLabel,
                    clientLabel,
                    emailLabel,
                    dateLabel
            );


            Button sendMeetLinkButton = new Button("Envoyer lien Meet");
            sendMeetLinkButton.setStyle("-fx-background-color: #0585e6; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 14px; " +
                    "-fx-padding: 8 15; " +
                    "-fx-background-radius: 20; " );

            sendMeetLinkButton.setOnAction(e -> {
                String clientEmail = client.getEmail();
                sendGoogleMeetLink(clientEmail, session);
            });


            reservationCard.getChildren().add(sendMeetLinkButton);

            reservationsContainer.getChildren().add(reservationCard);
        }
    }

    @FXML
    private void backToManagement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/SessionManagement.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendGoogleMeetLink(String clientEmail, Session_game session) {
        try {
            // Générer le lien Meet
            String meetLink = "https://meet.google.com/" + generateRandomMeetId();

            // Préparer le contenu de l'email
            String subject = "Lien Google Meet pour votre session de coaching";
            String additionalInfo = String.format(
                    "Bonjour,\n\nVoici votre lien pour la session de coaching %s :\n%s\n\nÀ bientôt !",
                    session.getGame(), meetLink
            );

            // Utiliser EmailService pour envoyer l'email automatiquement
            EmailService.sendEmail(
                    clientEmail,
                    subject,
                    "custom", // Utilisation du type "custom" existant
                    additionalInfo
            );

            showAlert("Succès",
                    "Le lien Google Meet a été envoyé automatiquement au client par email.",
                    Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Erreur",
                    "Erreur lors de l'envoi du lien Meet : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private String generateRandomMeetId() {

        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder meetId = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            meetId.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return meetId.toString();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void exportToExcel() {
        try {
            //  Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Sessions Réservées");


            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"Client", "Email", "Jeu", "Date Réservation", "Prix", "Durée"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            int coachId = SessionManager.getInstance().getUserId();
            List<Reservation> reservations = serviceReservation.getReservationsByCoachId(coachId);

            int rowNum = 1;
            for (Reservation reservation : reservations) {
                Row row = sheet.createRow(rowNum++);
                Utilisateur client = utilisateurService.getOne(reservation.getClient_id());
                Session_game session = reservation.getSession();

                row.createCell(0).setCellValue(client.getNom() + " " + client.getPrenom());
                row.createCell(1).setCellValue(client.getEmail());
                row.createCell(2).setCellValue(session.getGame());
                row.createCell(3).setCellValue(reservation.getdate_reservation().toString());
                row.createCell(4).setCellValue(session.getprix() + " DT");
                row.createCell(5).setCellValue(session.getduree_session());
            }


            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }


            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "Sessions_Reservees_" + timestamp + ".xlsx";


            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier Excel");
            fileChooser.setInitialFileName(fileName);
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            File file = fileChooser.showSaveDialog(reservationsContainer.getScene().getWindow());

            if (file != null) {
                // Sauvegarder le fichier
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
                showAlert("Succès", "Le fichier Excel a été créé avec succès!", Alert.AlertType.INFORMATION);
            }

            workbook.close();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la création du fichier Excel: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
} 