package tn.esprit.Controllers.forum;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NavbarController implements Initializable {

    @FXML
    private HBox navButtons;
    @FXML
    private Button homeButton, eventButton, shopButton, forumButton, coachingButton, loginButton, logoutButton, joinUs;
    @FXML
    private Label nicknameLabel;
    @FXML
    private ImageView profileImage;
    @FXML
    private StackPane profileContainer;

    private static final String UPLOAD_DIR = "C:\\xampp\\htdocs\\img\\";
    private static final String DEFAULT_AVATAR = "default-avatar.jpg";

    private ForumController forumController;
    Boolean isAuthentifier = SessionManager.getInstance().isLoggedIn();
    private UtilisateurService us = new UtilisateurService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (isAuthentifier) {
            setupNavigation();
            loginButton.setVisible(false);
            int userId = SessionManager.getInstance().getUserId();
            Utilisateur u1 = us.getOne(userId);
            nicknameLabel.setText(u1.getNickname());
            nicknameLabel.setVisible(true);
            logoutButton.setVisible(true);
            profileContainer.setVisible(true);
            loadProfileImage(u1);

            setupLogoutButton();
            setupJoinUsButton();
        } else {
            loginButton.setVisible(true);
            loginButton.setOnAction(event -> navigateTo("gestion Utilisateur/Login/Login"));
            nicknameLabel.setVisible(false);
            logoutButton.setVisible(false);
            profileContainer.setVisible(false);
        }
    }

    private void setupNavigation() {
        homeButton.setOnAction(event -> navigateTo("Home"));
        eventButton.setOnAction(event -> navigateTo("Evenement/ListEvenement"));
        shopButton.setOnAction(event -> navigateTo("Produit/main"));
        forumButton.setOnAction(event -> {
            System.out.println("Forum button clicked!");
            navigateTo("forumUI/Forum");
        });
        coachingButton.setOnAction(event -> navigateTo("Coach/search_session"));
    }

    private void setupLogoutButton() {
        logoutButton.setOnAction(event -> {
            SessionManager.getInstance().logout();
            navigateTo("gestion Utilisateur/Login/Login");
        });
    }

    private void setupJoinUsButton() {
        if (SessionManager.getInstance().getRole().equals(Role.CLIENT)||SessionManager.getInstance().getRole().equals(Role.COACH)) {
            joinUs.setText("Rejoignez-nous");
            joinUs.setOnAction(event -> navigateTo("gestion Utilisateur/addCoach/addCo"));
        } else if (SessionManager.getInstance().getRole().equals(Role.ADMIN)) {
            joinUs.setText("dashboard");
            joinUs.setOnAction(event -> navigateTo("gestion Utilisateur/addCoach/displayDemand"));
        }
    }

    private void loadProfileImage(Utilisateur user) {
        try {
            String photoPath = user.getPhoto();
            if (photoPath != null && !photoPath.isEmpty()) {
                File photoFile = new File(UPLOAD_DIR + photoPath);
                if (photoFile.exists()) {
                    Image image = new Image(photoFile.toURI().toString());
                    profileImage.setImage(image);
                    return;
                }
            }
            // Load default avatar if no profile picture exists
            File defaultAvatarFile = new File(UPLOAD_DIR + DEFAULT_AVATAR);
            if (defaultAvatarFile.exists()) {
                Image defaultImage = new Image(defaultAvatarFile.toURI().toString());
                profileImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.out.println("Error loading profile image: " + e.getMessage());
        }
    }

    @FXML
    private void handleProfileClick() {
        if (isAuthentifier) {
            navigateTo("gestion Utilisateur/Profil/Profil");
        }
    }

    public void setForumController(ForumController forumController) {
        this.forumController = forumController;
    }

    private void navigateTo(String page) {
        try {
            System.out.println("Navigating to: " + page);
            QuestionCardController.stopAllVideos();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + page + ".fxml"));
            Parent root = loader.load();

            if (page.equals("forumUI/Forum") && forumController != null) {
                forumController.refreshQuestions();
            }

            Stage stage = (Stage) forumButton.getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
