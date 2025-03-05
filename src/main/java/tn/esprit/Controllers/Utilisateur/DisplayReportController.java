package tn.esprit.Controllers.Utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.Models.Report;
import tn.esprit.Models.ReportStatus;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.ReportService;
import tn.esprit.Services.UtilisateurService;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DisplayReportController implements Initializable {

    @FXML
    private VBox reportsContainer;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label reportCountLabel;

    @FXML
    private Button backButton;

    private Utilisateur currentUser;
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final ReportService reportService = new ReportService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up back button action
        backButton.setOnAction(e -> goBackToDashboard());
    }

    /**
     * Initialize the controller with user data
     * 
     * @param user The user whose reports to display
     */
    public void initData(Utilisateur user) {
        this.currentUser = user;

        // Set user information
        userNameLabel.setText(user.getPrenom() + " " + user.getNom());

        // Load and display reports
        loadReports();
    }

    /**
     * Load and display reports for the current user
     */
    private void loadReports() {
        // Clear existing reports
        reportsContainer.getChildren().clear();

        // Get reports for the user using ReportService
        List<Report> reports = new ArrayList<>();
        try {
            reports = reportService.getReportsByReportedUserId(currentUser.getId());
        } catch (Exception e) {
            System.err.println("Error loading reports for user " + currentUser.getId() + ": " + e.getMessage());
            // Continue with empty reports list
        }

        // Update report count label
        reportCountLabel.setText("Total Reports: " + reports.size());

        if (reports.isEmpty()) {
            // Display message if no reports
            Label noReportsLabel = new Label("Aucun rapport  pour cet utilisateur");
            noReportsLabel.getStyleClass().add("no-reports-label");
            reportsContainer.getChildren().add(noReportsLabel);
            return;
        }

        // Create a card for each report
        for (Report report : reports) {
            VBox reportCard = createReportCard(report);
            reportsContainer.getChildren().add(reportCard);
        }
    }

    /**
     * Create a card view for a single report
     * 
     * @param report The report data
     * @return A VBox containing the report card
     */
    private VBox createReportCard(Report report) {
        VBox card = new VBox(10);
        card.getStyleClass().add("report-card");
        card.setPadding(new Insets(15));

        // Report ID and date
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("Report #" );
        idLabel.getStyleClass().add("report-id");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = report.getCreatedAt().toLocalDateTime().format(formatter);
        Label dateLabel = new Label("Date: " + formattedDate);
        dateLabel.getStyleClass().add("report-date");

        headerBox.getChildren().addAll(idLabel, spacer, dateLabel);

        // Get reporter name
        UtilisateurService userService = new UtilisateurService();
        Utilisateur reporter = userService.getOne(report.getReporterId());
        String reporterName = reporter != null ? reporter.getPrenom() + " " + reporter.getNom()
                : "Unknown (ID: " + report.getReporterId() + ")";

        // Reporter info
        Label reporterLabel = new Label("Rapporté par: " + reporterName);
        reporterLabel.getStyleClass().add("reporter-name");

        // Reason
        Label reasonLabel = new Label("Raison: " + report.getReason().name());
        reasonLabel.getStyleClass().add("report-reason");

        // Evidence
        Label evidenceTitle = new Label("Preuve:");
        evidenceTitle.getStyleClass().add("evidence-title");

        TextArea evidenceArea = new TextArea(report.getEvidence());
        evidenceArea.setEditable(false);
        evidenceArea.setWrapText(true);
        evidenceArea.setPrefHeight(80);
        evidenceArea.getStyleClass().add("evidence-area");

        // Status
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Label statusTitle = new Label("Status:");
        statusTitle.getStyleClass().add("status-title");




        // Ban user button
        Button banButton = new Button(currentUser.isBan() ? "Unban User" : "Ban User");
        banButton.getStyleClass().add("ban-button");
        banButton.setOnAction(e -> {
            if (currentUser.isBan()) {
                unbanUser();
            } else {
                showBanDialog();
            }
        });

        // Add all elements to the card
        card.getChildren().addAll(
                headerBox,
                reporterLabel,
                reasonLabel,
                evidenceTitle,
                evidenceArea,
                statusBox,
                banButton);

        return card;
    }

    /**
     * Update the status of a report
     * 
     * @param reportId  The ID of the report to update
     * @param newStatus The new status to set
     */
    private void updateReportStatus(int reportId, ReportStatus newStatus) {
        try {
            reportService.updateReportStatus(reportId, newStatus);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Status Updated");
            alert.setHeaderText(null);
            alert.setContentText("Report status has been updated successfully.");
            alert.showAndWait();

            // Reload reports to reflect changes
            loadReports();
        } catch (Exception e) {
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to update report status: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Show dialog to select ban duration
     */
    private void showBanDialog() {
        // Create dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Ban User");
        dialog.setHeaderText("Select ban duration for " + currentUser.getPrenom() + " " + currentUser.getNom());

        // Set up buttons
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Create content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        ComboBox<String> durationCombo = new ComboBox<>();
        durationCombo.getItems().addAll("1 Day", "1 Week", "1 Month", "Permanent");
        durationCombo.setValue("1 Day");

        content.getChildren().add(durationCombo);
        dialog.getDialogPane().setContent(content);

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return durationCombo.getValue();
            }
            return null;
        });

        // Show dialog and handle result
        dialog.showAndWait().ifPresent(duration -> banUser(duration));
    }

    /**
     * Ban the user for the specified duration
     * 
     * @param duration The ban duration
     */
    private void banUser(String duration) {
        // Calculate ban end time
        LocalDateTime banEndTime = LocalDateTime.now();

        switch (duration) {
            case "1 Day":
                banEndTime = banEndTime.plusDays(1);
                break;
            case "1 Week":
                banEndTime = banEndTime.plusWeeks(1);
                break;
            case "1 Month":
                banEndTime = banEndTime.plusMonths(1);
                break;
            case "Permanent":
                banEndTime = null; // Use null for permanent ban
                break;
        }

        try {
            // Update ban status in database
            utilisateurService.updateBanStatus(currentUser.getId(), true, banEndTime);

            // Update the local user object
            currentUser.setBan(true);
            currentUser.setBanTime(banEndTime);

            // Show confirmation alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("User Ban Status");
            alert.setHeaderText(null);
            alert.setContentText("User has been banned until " +
                    (banEndTime != null ? banEndTime.toString() : "permanently"));
            alert.showAndWait();

            // Reload reports to update UI
            loadReports();

        } catch (Exception e) {
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to ban user: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Unban the current user
     */
    private void unbanUser() {
        try {
            // Update ban status in database
            utilisateurService.updateBanStatus(currentUser.getId(), false, null);

            // Update the local user object
            currentUser.setBan(false);
            currentUser.setBanTime(null);

            // Show confirmation alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("User Ban Status");
            alert.setHeaderText(null);
            alert.setContentText("User has been unbanned.");
            alert.showAndWait();

            // Reload reports to update UI
            loadReports();

        } catch (Exception e) {
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to unban user: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Go back to the dashboard screen
     */
    private void goBackToDashboard() {
        // Close the current window
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}
