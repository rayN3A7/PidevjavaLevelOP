package tn.esprit.Controllers.Evenement;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.Services.EmailService;
import tn.esprit.Services.Evenement.CategorieEvService;
import tn.esprit.Services.Evenement.EvenementService;
import tn.esprit.utils.SessionManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DetailsEvenementController {
    private final CategorieEvService ces = new CategorieEvService();
    private final EvenementService es = new EvenementService();
    @FXML
    private Label eventNameLabel,eventDateLabel,eventLieuLabel,eventNBPLabel,eventCatLabel;
    @FXML
    private Label TimeEvent;
    @FXML
    private Button reserverButton;
    private Evenement currentEvent;
    @FXML
    private VBox hbox;
    @FXML
    private ImageView qrCodeImageView;

private String userRole = SessionManager.getInstance().getRole().name();
    public void initData(Evenement event) {
        if (event != null) {
            currentEvent = event;
            eventNameLabel.setText(event.getNom_event());

            Timestamp timestamp = event.getDate_event();

            LocalDateTime dateTime = timestamp.toLocalDateTime();

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            eventDateLabel.setText(dateTime.format(dateFormatter));

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            String formattedTime = dateTime.format(timeFormatter);
            TimeEvent.setText(formattedTime);

            eventLieuLabel.setText(event.getLieu_event());
            eventNBPLabel.setText(String.valueOf(event.getMax_places_event()));
            eventCatLabel.setText(ces.getNomCategorieEvent(event.getCategorie_id()));
            if(userRole.equals("ADMIN")){
                Button exporterPDF = new Button("Exporter Liste des Utilisateurs en PDF");
                exporterPDF.setOnAction(e -> exporterPDF());
                exporterPDF.getStyleClass().add("export-button");
                exporterPDF.setMaxWidth(Double.MAX_VALUE);
                reserverButton.setVisible(false);
                hbox.getChildren().add(exporterPDF);
            }

            generateQRCode(event);
        }
    }
    @FXML
    private void RetourButtonVersListeEvenement(ActionEvent event) throws IOException {
        if(userRole.equals("ADMIN")){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListeEvenementAdmin.fxml"));
            Parent signInRoot = loader.load();
            Scene signInScene = new Scene(signInRoot);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(signInScene);
            window.show();
        }else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListEvenement.fxml"));
            Parent signInRoot = loader.load();
            Scene signInScene = new Scene(signInRoot);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(signInScene);
            window.show();
        }
    }
    @FXML
    private void reserverPlace(ActionEvent e1) {
        int userId = SessionManager.getInstance().getUserId();
        String userEmail = SessionManager.getInstance().getEmail();

        if (currentEvent == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun événement sélectionné !");
            return;
        }

        if (es.reservationExiste(userId, currentEvent.getId())) {
            showAlert(Alert.AlertType.WARNING, "Réservation", "Vous avez déjà réservé une place pour cet événement.");
            return;
        }

        if (currentEvent.getMax_places_event() <= 0) {
            showAlert(Alert.AlertType.ERROR, "Réservation", "Il n'y a plus de places disponibles pour cet événement.");
            return;
        }

        boolean success = es.reserverPlace(userId, currentEvent.getId());

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Réservation", "Votre place a été réservée avec succès !");

            try {
                String subject = "Confirmation de réservation pour " + currentEvent.getNom_event();
                EmailService.sendEmailWithTemplate(userEmail, subject, currentEvent, ces);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'envoi d'email", "Votre réservation a été prise en compte, mais l'email de confirmation n'a pas pu être envoyé.");
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Échec", "La réservation a échoué. Vérifiez la disponibilité des places.");
        }
    }
    private void showAlert(Alert.AlertType type, String titre, String contenu) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
    @FXML
    public void exporterPDF() {
        String eventName = eventNameLabel.getText();
        List<String[]> users = es.getListeUtilisateursReserves(currentEvent.getId());

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName(eventName.replaceAll("\\s+", "_") + "_reservations.pdf");
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.setLeading(20f);

                // En-tête avec titre centré
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(200, 750);
                contentStream.showText("Réservations - " + eventName);
                contentStream.endText();

                // Ligne de séparation sous le titre
                contentStream.moveTo(50, 740);
                contentStream.lineTo(550, 740);
                contentStream.setLineWidth(1.5f);
                contentStream.stroke();

                // Position de départ
                int y = 700;
                int rowHeight = 25;
                int colWidth = 160;

                // Dessiner un fond gris pour l'en-tête
                contentStream.setNonStrokingColor(0.78f, 0.78f, 0.78f);
                contentStream.addRect(50, y - rowHeight, colWidth * 3, rowHeight);
                contentStream.fill();
                contentStream.setNonStrokingColor(0, 0, 0);

                // Dessiner l'en-tête du tableau
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                drawTableCell(contentStream, 50, y, colWidth, rowHeight, "Nom");
                drawTableCell(contentStream, 210, y, colWidth, rowHeight, "Prénom");
                drawTableCell(contentStream, 370, y, colWidth, rowHeight, "Email");
                y -= rowHeight;

                // Dessiner les données du tableau
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                for (String[] user : users) {
                    if (y < 50) break; // Évite de sortir de la page
                    drawTableCell(contentStream, 50, y, colWidth, rowHeight, user[0]); // Nom
                    drawTableCell(contentStream, 210, y, colWidth, rowHeight, user[1]); // Prénom
                    drawTableCell(contentStream, 370, y, colWidth, rowHeight, user[2]); // Email
                    y -= rowHeight;
                }

                // Ajout d'un pied de page
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 30);
                contentStream.showText("Généré par LevelOP - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                contentStream.endText();

                contentStream.close();
                document.save(file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Méthode utilitaire pour dessiner une cellule de tableau
    private void drawTableCell(PDPageContentStream contentStream, float x, float y, float width, float height, String text) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(x + 5, y + 5);
        contentStream.showText(text);
        contentStream.endText();

        // Dessiner les bordures
        contentStream.moveTo(x, y);
        contentStream.lineTo(x + width, y);
        contentStream.lineTo(x + width, y - height);
        contentStream.lineTo(x, y - height);
        contentStream.lineTo(x, y);
        contentStream.stroke();
    }
    private void generateQRCode(Evenement event) {
        try {
            // Construire l'URL avec les paramètres
            String baseUrl = "https://feresad.github.io/event-qr-code/";
            String url = baseUrl + "?id=" + event.getId() +
                    "&nom=" + URLEncoder.encode(event.getNom_event(), "UTF-8") +
                    "&date=" + URLEncoder.encode(event.getDate_event().toString(), "UTF-8") +
                    "&lieu=" + URLEncoder.encode(event.getLieu_event(), "UTF-8") +
                    "&places=" + event.getMax_places_event();
            System.out.println("URL générée : " + url);
            // Générer le QR Code avec l'URL
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            Image qrImage = SwingFXUtils.toFXImage(bufferedImage, null);

            qrCodeImageView.setImage(qrImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
