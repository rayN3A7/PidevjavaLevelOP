package tn.esprit.Controllers.Utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.text.Text;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;

public class DashboardController implements Initializable {

    @FXML
    private FlowPane usersContainer;
    @FXML
    private Button prevPageBtn;
    @FXML
    private Button nextPageBtn;
    @FXML
    private Label pageLabel;
    @FXML
    private VBox banDialog;
    @FXML
    private ComboBox<String> banDurationCombo;
    @FXML
    private Button confirmBanBtn;
    @FXML
    private Button cancelBanBtn;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final int ITEMS_PER_PAGE = 12;
    private int currentPage = 1;
    private ObservableList<Utilisateur> allUsers;
    private Utilisateur selectedUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadUsers();
        setupPagination();
        setupBanDialog();
    }

    private void loadUsers() {
        List<Utilisateur> users = utilisateurService.getAll();
        allUsers = FXCollections.observableArrayList(users);
        displayUsers();
    }

    private void displayUsers() {
        usersContainer.getChildren().clear();
        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allUsers.size());

        for (int i = startIndex; i < endIndex; i++) {
            Utilisateur user = allUsers.get(i);
            VBox userCard = createUserCard(user);
            usersContainer.getChildren().add(userCard);
        }

        updatePaginationControls();
    }

    private VBox createUserCard(Utilisateur user) {
        VBox card = new VBox(10);
        card.getStyleClass().add("user-card");

        // User Details
        Label nameLabel = new Label(user.getPrenom() + " " + user.getNom());
        nameLabel.getStyleClass().add("title");

        Text emailText = new Text(user.getEmail());
        emailText.getStyleClass().add("info");

        Text roleText = new Text("Role: " + user.getRole().toString());
        roleText.getStyleClass().add("info");

        Text privilegeText = new Text("Privilege: " + user.getPrivilege());
        privilegeText.getStyleClass().add("info");

        // Ban Status
        String banStatus = user.isBan() ? "Banned" : "Active";
        Text banText = new Text("Status: " + banStatus);
        banText.getStyleClass().add("info");

        // Ban Button
        Button banBtn = new Button(user.isBan() ? "Unban" : "Ban");
        banBtn.getStyleClass().addAll("button", "ban-button");
        banBtn.setOnAction(e -> {
            if (user.isBan()) {
                // If user is already banned, directly unban them
                unbanUser(user);
            } else {
                // If user is not banned, show the ban dialog to select duration
                showBanDialog(user);
            }
        });

        // Report Check Button
        Button reportCheckBtn = new Button("Check Reports");
        reportCheckBtn.getStyleClass().add("button");
        reportCheckBtn.setOnAction(e -> checkUserReports(user));

        card.getChildren().addAll(nameLabel, emailText, roleText, privilegeText, banText, banBtn, reportCheckBtn);
        card.setAlignment(Pos.CENTER);

        return card;
    }

    private void setupPagination() {
        prevPageBtn.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                displayUsers();
            }
        });

        nextPageBtn.setOnAction(e -> {
            int maxPages = (int) Math.ceil(allUsers.size() / (double) ITEMS_PER_PAGE);
            if (currentPage < maxPages) {
                currentPage++;
                displayUsers();
            }
        });
    }

    private void updatePaginationControls() {
        int maxPages = (int) Math.ceil(allUsers.size() / (double) ITEMS_PER_PAGE);
        pageLabel.setText("Page " + currentPage + " of " + maxPages);
        prevPageBtn.setDisable(currentPage == 1);
        nextPageBtn.setDisable(currentPage == maxPages);
    }

    private void setupBanDialog() {
        banDialog.setVisible(false);

        confirmBanBtn.setOnAction(e -> {
            if (selectedUser != null && banDurationCombo.getValue() != null) {
                banUser(selectedUser, banDurationCombo.getValue());
                banDialog.setVisible(false);
                loadUsers(); // Refresh the user list
            }
        });

        cancelBanBtn.setOnAction(e -> {
            banDialog.setVisible(false);
            selectedUser = null;
        });
    }

    private void showBanDialog(Utilisateur user) {
        selectedUser = user;
        banDialog.setVisible(true);
    }

    private void banUser(Utilisateur user, String duration) {
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
            // Update ban status and ban end time in database using the dedicated method
            utilisateurService.updateBanStatus(user.getId(), true, banEndTime);

            // Update the local user object to reflect changes
            user.setBan(true);
            user.setBanTime(banEndTime);

            // Show confirmation alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("User Ban Status");
            alert.setHeaderText(null);
            alert.setContentText("User has been banned until " +
                    (banEndTime != null ? banEndTime.toString() : "permanently"));
            alert.showAndWait();

            // Refresh the user list to update the UI
            loadUsers();

        } catch (Exception e) {
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to ban user: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void unbanUser(Utilisateur user) {
        try {
            // Update ban status and ban end time in database using the dedicated method
            utilisateurService.updateBanStatus(user.getId(), false, null);

            // Update the local user object to reflect changes
            user.setBan(false);
            user.setBanTime(null);

            // Show confirmation alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("User Ban Status");
            alert.setHeaderText(null);
            alert.setContentText("User has been unbanned.");
            alert.showAndWait();

            // Refresh the user list to update the UI
            loadUsers();

        } catch (Exception e) {
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to unban user: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void checkUserReports(Utilisateur user) {
        List<Map<String, Object>> reports = utilisateurService.getUserReports(user.getId());

        if (reports.isEmpty()) {
            // No reports found
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("User Reports");
            alert.setHeaderText(null);
            alert.setContentText("No reports found for user: " + user.getPrenom() + " " + user.getNom());
            alert.showAndWait();
        } else {
            // Create a dialog to display reports
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Reports for " + user.getPrenom() + " " + user.getNom());
            dialog.setHeaderText("User has " + reports.size() + " report(s)");

            // Create a VBox to hold all report details
            VBox reportsContainer = new VBox(10);
            reportsContainer.setPadding(new Insets(10));

            for (Map<String, Object> report : reports) {
                VBox reportBox = new VBox(5);
                reportBox.getStyleClass().add("report-box");
                reportBox.setPadding(new Insets(10));
                reportBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");

                Label reporterLabel = new Label("Reported by: " + report.get("reporterName"));
                reporterLabel.setStyle("-fx-font-weight: bold;");

                Label reasonLabel = new Label("Reason: " + report.get("reason"));

                Label evidenceLabel = new Label("Evidence: " + report.get("evidence"));
                evidenceLabel.setWrapText(true);

                Label statusLabel = new Label("Status: " + report.get("status"));
                statusLabel.setStyle("-fx-font-weight: bold;");

                Label dateLabel = new Label("Date: " + report.get("createdAt"));

                reportBox.getChildren().addAll(reporterLabel, reasonLabel, evidenceLabel, statusLabel, dateLabel);
                reportsContainer.getChildren().add(reportBox);
            }

            // Add a ScrollPane in case there are many reports
            ScrollPane scrollPane = new ScrollPane(reportsContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);

            dialog.getDialogPane().setContent(scrollPane);

            // Add close button
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(closeButton);

            dialog.showAndWait();
        }
    }
}
