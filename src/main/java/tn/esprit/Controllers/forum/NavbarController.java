package tn.esprit.Controllers.forum;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NavbarController implements Initializable {

    @FXML
    private HBox navButtons;
    @FXML
    private Button homeButton, eventButton, shopButton, forumButton, coachingButton, loginButton,logoutButton;
    @FXML
    private Label nicknameLabel;

    private ForumController forumController;
    Boolean isAuthentifier = SessionManager.getInstance().isLoggedIn();
    private UtilisateurService us = new UtilisateurService();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(isAuthentifier) {
            homeButton.setOnAction(event -> navigateTo("Home"));
            eventButton.setOnAction(event -> navigateTo("Evenement/ListEvenement"));
            shopButton.setOnAction(event -> navigateTo("Produit/main"));
            forumButton.setOnAction(event -> {
                System.out.println("Forum button clicked!");
                navigateTo("forumUI/Forum");
            });
            coachingButton.setOnAction(event -> navigateTo("Coach/search_session"));
            loginButton.setVisible(false);
            int userId = SessionManager.getInstance().getUserId();
            Utilisateur u1 = us.getOne(userId);
            nicknameLabel.setText(u1.getNickname());
            nicknameLabel.setVisible(true);
            logoutButton.setVisible(true);
            logoutButton.setOnAction(event -> {
                SessionManager.getInstance().logout();
                navigateTo("Home");
            });
        }else {
            loginButton.setVisible(true);
        loginButton.setOnAction(event -> navigateTo("gestion Utilisateur/Login/Login"));

            nicknameLabel.setVisible(false);
            logoutButton.setVisible(false);
        }
    }

    public void setForumController(ForumController forumController) {
        this.forumController = forumController;
    }

    private void navigateTo(String page) {
        try {
            System.out.println("Navigating to: " + page);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + page + ".fxml"));
            Parent root = loader.load();

            if (page.equals("Forum") && forumController != null) {
                forumController.refreshQuestions();
            }

            Stage stage = (Stage) forumButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading " + page + ".fxml");
        }
    }
}
