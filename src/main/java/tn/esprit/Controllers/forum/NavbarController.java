package tn.esprit.Controllers.forum;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Models.Notification;
import tn.esprit.Models.Question;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.NotificationService;
import tn.esprit.Services.QuestionService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class NavbarController implements Initializable {

    @FXML private HBox navButtons;
    @FXML private Button homeButton, eventButton, shopButton, forumButton, coachingButton, loginButton, logoutButton, joinUs;
    @FXML private Label nicknameLabel;
    @FXML private ImageView profileImage;
    @FXML private StackPane profileContainer;
    @FXML private Button notificationButton;
    @FXML private Label notificationBadge;

    private static final String UPLOAD_DIR = "C:\\xampp\\htdocs\\img\\";
    private static final String DEFAULT_AVATAR = "default-avatar.jpg";

    private ForumController forumController;
    private final Boolean isAuthentifier = SessionManager.getInstance().isLoggedIn();
    private final UtilisateurService us = new UtilisateurService();
    private final NotificationService notificationService = new NotificationService();
    private Popup notificationPopup;

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
            setupNotificationButton(userId);
        } else {
            loginButton.setVisible(true);
            loginButton.setOnAction(event -> navigateTo("gestion Utilisateur/Login/Login"));
            nicknameLabel.setVisible(false);
            logoutButton.setVisible(false);
            profileContainer.setVisible(false);
            notificationButton.setVisible(false);
            notificationBadge.setVisible(false);
        }
    }

    private void setupNotificationButton(int userId) {
        notificationButton.setVisible(true);
        notificationButton.setOnAction(event -> showNotificationPopup(userId));
        updateNotificationBadge(userId);

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000);
                    javafx.application.Platform.runLater(() -> updateNotificationBadge(userId));
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    private void updateNotificationBadge(int userId) {
        int unreadCount = notificationService.getUnreadCount(userId);
        notificationBadge.setText(unreadCount > 0 ? String.valueOf(unreadCount) : "");
        notificationBadge.setVisible(unreadCount > 0);
    }

    private void showNotificationPopup(int userId) {
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.hide();
            return;
        }

        notificationPopup = new Popup();
        VBox notificationBox = new VBox(5);
        notificationBox.setPadding(new Insets(10));
        notificationBox.setStyle(
                "-fx-background-color: #2e2e2e; " +
                        "-fx-border-color: #ff4081; " +
                        "-fx-border-width: 2; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0.5, 0, 2);"
        );

        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
        if (notifications.isEmpty()) {
            Label noNotifications = new Label("Aucune notification");
            noNotifications.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5;");
            noNotifications.setAlignment(Pos.CENTER);
            notificationBox.getChildren().add(noNotifications);
        } else {
            for (Notification notification : notifications) {
                HBox notificationItem = new HBox(8);
                notificationItem.setAlignment(Pos.CENTER_LEFT);
                notificationItem.setPadding(new Insets(8));
                String defaultStyle = notification.isRead() ? "-fx-background-color: #444;" : "-fx-background-color: #555;";
                notificationItem.setStyle(defaultStyle);
                notificationItem.setUserData(defaultStyle); // Store the default style

                // Add hover effect
                notificationItem.setOnMouseEntered(event -> {
                    notificationItem.setStyle(
                            notification.isRead() ? "-fx-background-color: #4a4a4a;" : "-fx-background-color: #5a5a5a;"
                    );
                });
                notificationItem.setOnMouseExited(event -> {
                    notificationItem.setStyle((String) notificationItem.getUserData()); // Retrieve and apply the default style
                });

                // Add comment icon
                ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/forumUI/icons/comment.png")));
                icon.setFitWidth(16);
                icon.setFitHeight(16);

                // Message label
                Label messageLabel = new Label(notification.getMessage());
                messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                messageLabel.setWrapText(true);
                messageLabel.setMaxWidth(220);

                // Timestamp label
                Label timeLabel = new Label(
                        notification.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm dd/MM"))
                );
                timeLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px;");
                timeLabel.setMinWidth(60);
                timeLabel.setAlignment(Pos.CENTER_RIGHT);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                notificationItem.getChildren().addAll(icon, messageLabel, spacer, timeLabel);

                notificationItem.setOnMouseClicked(event -> {
                    notificationService.delete(notification.getId());
                    navigateToForum(notification.getLink());
                    notificationPopup.hide();
                    updateNotificationBadge(userId);
                    showNotificationPopup(userId);
                });

                notificationBox.getChildren().add(notificationItem);
            }
        }

        ScrollPane scrollPane = new ScrollPane(notificationBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        notificationPopup.getContent().add(scrollPane);

        // Center the popup under the bell
        double popupWidth = 320;
        double bellCenterX = notificationButton.getScene().getWindow().getX() + notificationButton.localToScene(0, 0).getX() + (notificationButton.getWidth() / 2);
        double x = bellCenterX - (popupWidth / 2);
        double y = notificationButton.getScene().getWindow().getY() + notificationButton.localToScene(0, 0).getY() + notificationButton.getHeight() + 5;
        notificationPopup.show(notificationButton, x, y);
        notificationPopup.setAutoHide(true);

        // Set popup width and max height
        scrollPane.setPrefWidth(popupWidth);
        scrollPane.setMaxHeight(300);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), scrollPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    private void navigateToForum(String link) {
        try {
            String[] parts = link.split("#comment-");
            String questionPath = parts[0];
            String questionId = questionPath.substring(questionPath.lastIndexOf("/") + 1);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/QuestionDetails.fxml"));
            Parent root = loader.load();
            QuestionDetailsController controller = loader.getController();
            Question question = new QuestionService().getOne(Integer.parseInt(questionId));
            controller.loadQuestionDetails(question);

            Stage stage = (Stage) notificationButton.getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupNavigation() {
        homeButton.setOnAction(event -> navigateTo("Home"));
        eventButton.setOnAction(event -> navigateTo("Evenement/ListEvenement"));
        shopButton.setOnAction(event -> navigateTo("Produit/main"));
        forumButton.setOnAction(event -> navigateTo("forumUI/Forum"));
        coachingButton.setOnAction(event -> navigateTo("Coach/search_session"));
    }

    private void setupLogoutButton() {
        logoutButton.setOnAction(event -> {
            SessionManager.getInstance().logout();
            navigateTo("gestion Utilisateur/Login/Login");
        });
    }

    private void setupJoinUsButton() {
        if (SessionManager.getInstance().getRole().equals(Role.CLIENT) || SessionManager.getInstance().getRole().equals(Role.COACH)) {
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