package tn.esprit.Controllers.Utilisateur;


import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import tn.esprit.Models.Demande;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.DemandeService;
import tn.esprit.Services.EmailService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class DisplayDemandController {
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private VBox rejectPopup;
    @FXML
    private TextArea rejectReasonText;

    private DemandeService demandeService = new DemandeService();
    private Demande currentDemande;
    private UtilisateurService us=new UtilisateurService();


    @FXML
    public void initialize() {
        loadDemands();
    }

    private void loadDemands() {
        List<Demande> demands = demandeService.getAll();
        cardsContainer.getChildren().clear();

        for (Demande demand : demands) {
            VBox card = createDemandCard(demand);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createDemandCard(Demande demand) {
        VBox card = new VBox(10);
        card.getStyleClass().add("demand-card");

        Label gameLabel = new Label("Game: " + demand.getGame());
        gameLabel.getStyleClass().add("card-title");

        // Truncate description to two lines
        String fullDescription = demand.getDescription();
        String truncatedDescription = fullDescription;
        if (fullDescription.length() > 100) {
            truncatedDescription = fullDescription.substring(0, 97) + "...";
        }
        Label descriptionLabel = new Label(truncatedDescription);
        descriptionLabel.getStyleClass().add("card-info");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxHeight(40);

        // Add click handler for description
        descriptionLabel.setOnMouseClicked(e -> showDescriptionPopup(fullDescription));
        descriptionLabel.getStyleClass().add("clickable-text");

        // Create PDF preview
        ImageView pdfPreview = createPdfPreview(demand.getFile());
        pdfPreview.getStyleClass().add("pdf-preview");
        pdfPreview.setOnMouseClicked(e -> openPdfViewer(demand.getFile()));

        HBox buttonsBox = new HBox(10);
        Button acceptButton = new Button("Accept");
        acceptButton.getStyleClass().add("accept-button");
        acceptButton.setOnAction(e -> handleAccept(demand));

        Button refuseButton = new Button("Refuse");
        refuseButton.getStyleClass().add("refuse-button");
        refuseButton.setOnAction(e -> showRejectPopup(demand));

        buttonsBox.getChildren().addAll(acceptButton, refuseButton);

        card.getChildren().addAll(gameLabel, descriptionLabel, pdfPreview, buttonsBox);
        return card;
    }

    private ImageView createPdfPreview(byte[] pdfData) {
        try {
            PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfData));
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            // Render first page
            BufferedImage bImage = pdfRenderer.renderImageWithDPI(0, 72); // Lower DPI for preview
            WritableImage fxImage = SwingFXUtils.toFXImage(bImage, null);
            ImageView imageView = new ImageView(fxImage);

            // Set dimensions for preview
            imageView.setFitWidth(250); // Adjust these values as needed
            imageView.setFitHeight(300); // Adjust these values as needed
            imageView.setPreserveRatio(true);

            // Add hover effect
            imageView.setStyle("-fx-cursor: hand;");

            document.close();
            return imageView;
        } catch (IOException e) {
            ImageView errorImage = new ImageView(); // You could set a default/error image here
            errorImage.setFitWidth(250);
            errorImage.setFitHeight(300);
            return errorImage;
        }
    }


    private void openPdfViewer(byte[] pdfData) {
        PDDocument document = null;
        try {
            document = PDDocument.load(new ByteArrayInputStream(pdfData));
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            // Create a new stage for the PDF viewer
            Stage pdfStage = new Stage();
            pdfStage.setTitle("CV Viewer");

            // Create main container
            VBox mainContainer = new VBox(10);
            mainContainer.setStyle("-fx-background-color: #1e1e2f; -fx-padding: 20;");

            // Add close button at the top
            final PDDocument finalDocument = document; // Need final reference for lambda


            // Create toolbar
            HBox toolbar = new HBox(10);
            toolbar.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
           // toolbar.getChildren().add(closeButton);

            // Create a VBox to hold the PDF pages
            VBox pagesContainer = new VBox(10);
            ScrollPane scrollPane = new ScrollPane(pagesContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: #1e1e2f; -fx-background-color: transparent;");

            // Set maximum height for the scroll pane
            scrollPane.setMaxHeight(600);

            // Render each page of the PDF
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage bImage = pdfRenderer.renderImageWithDPI(page, 100);
                WritableImage fxImage = SwingFXUtils.toFXImage(bImage, null);
                ImageView imageView = new ImageView(fxImage);
                imageView.setFitWidth(500);
                imageView.setPreserveRatio(true);
                pagesContainer.getChildren().add(imageView);
            }

            // Add components to main container
            mainContainer.getChildren().addAll(toolbar, scrollPane);

            // Create scene with reasonable dimensions
            Scene scene = new Scene(mainContainer, 550, 700);

            // Add stylesheet
            scene.getStylesheets()
                    .add(getClass().getResource("/gestion Utilisateur/addCoach/DisplayDemand.css").toExternalForm());

            pdfStage.setScene(scene);
            pdfStage.setResizable(true);

            // Center the stage on screen
            pdfStage.centerOnScreen();
            pdfStage.show();

            // Add close handler to ensure document is closed when window is closed
            pdfStage.setOnCloseRequest(e -> {
                try {
                    finalDocument.close();
                } catch (IOException ex) {
                    System.err.println("Error closing PDF document: " + ex.getMessage());
                }
            });

        } catch (IOException e) {
            showAlert("Error", "Could not open PDF file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(new Image(getClass().getResource("/forumUI/icons/sucessalert.png").toExternalForm()));
        icon.setFitHeight(60);
        icon.setFitWidth(80);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/sucessalert.png").toString()));

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        alert.showAndWait();
    }

    private void handleAccept(Demande demand) {

        int userid=demand.getUserId();
        us.updateUserRole(userid);

        us.deleteClient(userid);
        us.addCoach(userid);

        EmailService.sendEmail(us.getEmail(userid), "Welcome to LevelOP!", "coach_accepted", "");

        demandeService.delete(demand);


        showSuccessAlert("Success", "Demand accepted successfully!");
        loadDemands();
    }

    private void showRejectPopup(Demande demand) {
        currentDemande = demand;
        rejectPopup.setVisible(true);
    }

    @FXML
    private void closeRejectPopup() {
        rejectPopup.setVisible(false);
        rejectReasonText.clear();
    }

    @FXML
    private void confirmReject() {
        int userid=currentDemande.getUserId();
        String reason = rejectReasonText.getText();
        if (reason.trim().isEmpty()) {
            showAlert("Error", "Please provide a reason for rejection");
            return;
        }

        EmailService.sendEmail(us.getEmail(userid), "Votre demande de coach a été refusée", "coach_refused", reason);
        //demandeService.delete(currentDemande);




        closeRejectPopup();
        showSuccessAlert("Success", "Demand rejected successfully!");
        loadDemands();
    }

    private void showDescriptionPopup(String description) {
        // Create a new stage for the description
        Stage popupStage = new Stage();
        popupStage.setTitle("Description complète");

        // Create the content
        VBox content = new VBox(15);
        content.setStyle("-fx-background-color: #1e1e2f; -fx-padding: 20;");

        // Add the description
        TextArea descriptionArea = new TextArea(description);
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.getStyleClass().add("description-popup-text");
        descriptionArea.setPrefRowCount(5);
        descriptionArea.setPrefColumnCount(30);

        // Add close button
        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnAction(e -> popupStage.close());

        // Add components to content
        content.getChildren().addAll(descriptionArea, closeButton);
        content.setAlignment(javafx.geometry.Pos.CENTER);

        // Create and set the scene
        Scene scene = new Scene(content);
        scene.getStylesheets()
                .add(getClass().getResource("/gestion Utilisateur/addCoach/DisplayDemand.css").toExternalForm());

        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.centerOnScreen();
        popupStage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(new Image(getClass().getResource("/forumUI/icons/alert.png").toExternalForm()));
        icon.setFitHeight(80);
        icon.setFitWidth(80);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/alert.png").toString()));

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        alert.showAndWait();
    }

}
