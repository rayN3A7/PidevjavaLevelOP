package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NavbarController implements Initializable {

    @FXML
    private HBox navButtons;
    @FXML
    private Button homeButton, eventButton, shopButton, forumButton, coachingButton, loginButton;

    private ForumController forumController; // ForumController to be injected

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        homeButton.setOnAction(event -> navigateTo("Home"));
        eventButton.setOnAction(event -> navigateTo("Event"));
        shopButton.setOnAction(event -> navigateTo("Shop"));
        forumButton.setOnAction(event -> {
            System.out.println("Forum button clicked!"); // Debugging
            navigateTo("Forum");
        });
        coachingButton.setOnAction(event -> navigateTo("Coaching"));
        loginButton.setOnAction(event -> navigateTo("Login"));
    }

    public void setForumController(ForumController forumController) {
        this.forumController = forumController;
    }

    private void navigateTo(String page) {
        try {
            System.out.println("Navigating to: " + page); // Debugging

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + page + ".fxml"));
            Parent root = loader.load();

            // Inject ForumController into NavbarController if needed
            if (page.equals("Forum") && forumController != null) {
                forumController.refreshQuestions(); // Ensure it refreshes if coming from another page
            }

            // Get the current stage (main window)
            Stage stage = (Stage) forumButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading " + page + ".fxml");
        }
    }
}
