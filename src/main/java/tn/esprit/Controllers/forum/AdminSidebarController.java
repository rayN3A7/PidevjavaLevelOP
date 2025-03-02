package tn.esprit.Controllers.forum;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Models.Role;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminSidebarController implements Initializable {

    @FXML private VBox adminOptions;
    @FXML private Button homeButton, eventsButton, textsButton, channelsButton, emailButton, phoneCallButton,
            onlineChatButton, websiteButton, gamesDashboardButton, createdButton, openButton,
            respondedButton, solvedButton;

    private Boolean isAdmin = SessionManager.getInstance().getRole() == Role.ADMIN;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (isAdmin) {
            // Set up navigation for all buttons
            homeButton.setOnAction(event -> navigateTo("Home"));
            eventsButton.setOnAction(event -> navigateTo("Evenement/ListEvenement"));
            textsButton.setOnAction(event -> navigateTo("Texts"));
            channelsButton.setOnAction(event -> navigateTo("Channels"));
            emailButton.setOnAction(event -> navigateTo("Email"));
            phoneCallButton.setOnAction(event -> navigateTo("PhoneCall"));
            onlineChatButton.setOnAction(event -> navigateTo("OnlineChat"));
            websiteButton.setOnAction(event -> navigateTo("Website"));
            gamesDashboardButton.setOnAction(event -> navigateTo("forumUI/AdminDashboard"));
            createdButton.setOnAction(event -> navigateTo("Tickets/Created"));
            openButton.setOnAction(event -> navigateTo("Tickets/Open"));
            respondedButton.setOnAction(event -> navigateTo("Tickets/Responded"));
            solvedButton.setOnAction(event -> navigateTo("Tickets/Solved"));

            adminOptions.setVisible(true);
            adminOptions.setManaged(true);
        } else {
            adminOptions.setVisible(false);
            adminOptions.setManaged(false);
        }
    }

    private void navigateTo(String page) {
        try {
            System.out.println("Navigating to: " + page);
            QuestionCardController.stopAllVideos();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + page + ".fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) adminOptions.getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to " + page + ": " + e.getMessage());
        }
    }
}