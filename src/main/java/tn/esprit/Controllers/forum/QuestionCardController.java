package tn.esprit.Controllers.forum;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Models.*;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.Services.*;
import tn.esprit.utils.EventBus;
import tn.esprit.utils.PrivilegeEvent;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class QuestionCardController {
    @FXML private Label commentAuthor;
    @FXML private Label titleLabel;
    @FXML private Label contentLabel;
    @FXML private VBox mediaContainer;
    @FXML private ImageView questionImage;
    @FXML private VBox videoWrapper;
    @FXML private MediaView questionVideo;
    @FXML private HBox videoControlBar;
    @FXML private Button playPauseButton;
    @FXML private Slider progressSlider;
    @FXML private Label timeLabel;
    @FXML private Slider volumeSlider;
    @FXML private Button fullScreenButton;
    @FXML private Label votesLabel;
    @FXML private Button reactButton;
    @FXML private Button upvoteButton;
    @FXML private Button downvoteButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private ImageView gameIcon;
    @FXML private VBox contentVBox;
    @FXML private ImageView crownIcon;
    @FXML private ImageView selectedEmojiImage;
    @FXML private HBox reactionContainer;
    @FXML private Button reportButton;
    @FXML private Button shareButton;

    private final ReportService reportService = new ReportService();
    private Question question;
    private ForumController forumController;
    private final int userId = SessionManager.getInstance().getUserId();
    private final UtilisateurService us = new UtilisateurService();
    private final QuestionService questionService = new QuestionService();
    private final GamesService gamesService = new GamesService();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final ScheduledExecutorService shutdownExecutor = Executors.newScheduledThreadPool(1);
    private static final List<QuestionCardController> allControllers = Collections.synchronizedList(new ArrayList<>());
    private static final List<MediaPlayer> activeMediaPlayers = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private static final Map<String, MediaPlayer> mediaPlayerCache = new ConcurrentHashMap<>();
    private MediaPlayer mediaPlayer;
    private boolean isFullScreen = false;
    private Scene originalScene;
    private final double originalWidth = 500;
    private final double originalHeight = 350;
    private BorderPane fullScreenLayout;
    private ScrollPane scrollPane;
    private volatile boolean isPlaying = false;
    private ChangeListener<Duration> currentTimeListener;
    private ChangeListener<Boolean> valueChangingListener;
    private ChangeListener<Number> volumeListener;
    private ChangeListener<Bounds> boundsListener;
    private final PauseTransition boundsDebounceTimer;
    private double savedVolume = 1.0;
    private final double controlBarHeight = 40.0;
    private final double controlBarWidthPercentage = 0.8;

    public QuestionCardController() {
        synchronized (allControllers) {
            allControllers.add(this);
        }
        boundsDebounceTimer = new PauseTransition(Duration.millis(100));
    }

    public void setQuestionData(Question question, ForumController forumController) {
        this.question = question;
        this.forumController = forumController;
        Parent root = contentVBox.getParent();
        if (root != null) {
            root.setUserData(this);
        }
        commentAuthor.setText(question.getUser().getNickname());
        titleLabel.setText(question.getTitle());
        titleLabel.setOnMouseClicked(event -> {
            stopAllVideos();
            openQuestionDetails(question);
        });
        titleLabel.setCursor(Cursor.HAND);
        contentLabel.setText(question.getContent());
        votesLabel.setText("Votes: " + question.getVotes());

        upvoteButton.setOnAction(e -> forumController.handleUpvote(question, votesLabel, downvoteButton));
        downvoteButton.setOnAction(e -> forumController.handleDownvote(question, votesLabel, downvoteButton));
        downvoteButton.setDisable(question.getVotes() == 0);
        shareButton.setOnAction(e -> showShareDialog());
        updateButton.setOnAction(e -> {
            stopAllVideos();
            forumController.updateQuestion(question);
        });
        deleteButton.setOnAction(e -> {
            stopAllVideos();
            forumController.deleteQuestion(question);
        });

        Utilisateur currentUser = us.getOne(userId);
        if (currentUser == null) {
            updateButton.setVisible(false);
            deleteButton.setVisible(false);
        } else {
            boolean isOwner = question.getUser().getId() == userId;
            if (currentUser.getRole() == Role.ADMIN) {
                updateButton.setVisible(true);
                deleteButton.setVisible(true);
            } else {
                updateButton.setVisible(isOwner);
                deleteButton.setVisible(isOwner);
            }
        }

        reactButton.setOnAction(e -> showEmojiPicker());
        reportButton.setOnAction(e -> showReportForm(question.getUser().getId(), question.getContent()));
        setGameIcon(question.getGame().getGame_name());
        displayReactions();
        displayUserReaction();
        loadQuestionMediaAsync();

        updatePrivilegeUI(question.getUser());

        EventBus.getInstance().addHandler(event -> {
            if (event.getUserId() == question.getUser().getId()) {
                Utilisateur user = us.getOne(event.getUserId());
                if (user != null) {
                    updatePrivilegeUI(user);
                }
            }
        });
        contentVBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && scrollPane == null) {
                Platform.runLater(this::setupBoundsListener);
            }
        });

        setupBoundsListener();
        contentVBox.addEventHandler(PrivilegeEvent.PRIVILEGE_CHANGED, event -> {
            if (event.getUserId() == question.getUser().getId()) {
                Utilisateur user = us.getOne(event.getUserId());
                if (user != null) {
                    updatePrivilegeUI(user);
                    if (event.getUserId() == userId) {
                        UtilisateurService.PrivilegeChange change = new UtilisateurService.PrivilegeChange(
                                user.getPrivilege(), event.getNewPrivilege());
                        forumController.showPrivilegeAlert(change);
                    }
                }
            }
        });
    }

    private void showShareDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Share Question");

        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(15));
        dialogContent.setStyle("-fx-background-color: #091221; -fx-border-color: #ff4081; -fx-border-width: 2; -fx-border-radius: 10;");

        Label header = new Label("Share on Social Media");
        header.setStyle("-fx-text-fill: #ff4081; -fx-font-size: 18px; -fx-font-weight: bold;");

        Button twitterButton = new Button("Twitter");
        twitterButton.setStyle("-fx-background-color: #1DA1F2; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20 10 20;");
        ImageView twitterIcon = new ImageView(new Image(getClass().getResourceAsStream("/forumUI/icons/twitter.png")));
        twitterIcon.setFitHeight(20);
        twitterIcon.setFitWidth(20);
        twitterButton.setGraphic(twitterIcon);
        addButtonHoverEffect(twitterButton);

        Button facebookButton = new Button("Facebook");
        facebookButton.setStyle("-fx-background-color: #3B5998; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20 10 20;");
        ImageView facebookIcon = new ImageView(new Image(getClass().getResourceAsStream("/forumUI/icons/facebook.png")));
        facebookIcon.setFitHeight(20);
        facebookIcon.setFitWidth(20);
        facebookButton.setGraphic(facebookIcon);
        addButtonHoverEffect(facebookButton);

        Button redditButton = new Button("Reddit");
        redditButton.setStyle("-fx-background-color: #FF4500; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20 10 20;");
        ImageView redditIcon = new ImageView(new Image(getClass().getResourceAsStream("/forumUI/icons/reddit.png")));
        redditIcon.setFitHeight(20);
        redditIcon.setFitWidth(20);
        redditButton.setGraphic(redditIcon);
        addButtonHoverEffect(redditButton);

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20 10 20;");
        ImageView cancelIcon = new ImageView(new Image(getClass().getResourceAsStream("/forumUI/icons/cancel.png")));
        cancelIcon.setFitHeight(20);
        cancelIcon.setFitWidth(20);
        cancelButton.setGraphic(cancelIcon);
        addButtonHoverEffect(cancelButton);

        twitterButton.setOnAction(e -> {
            System.out.println("Twitter button clicked, attempting to close stage...");
            shareOnSocialMedia("twitter");
            Platform.runLater(() -> {
                System.out.println("Closing stage for Twitter...");
                dialogStage.close();
            });
        });
        facebookButton.setOnAction(e -> {
            System.out.println("Facebook button clicked, attempting to close stage...");
            shareOnSocialMedia("facebook");
            Platform.runLater(() -> {
                System.out.println("Closing stage for Facebook...");
                dialogStage.close();
            });
        });
        redditButton.setOnAction(e -> {
            System.out.println("Reddit button clicked, attempting to close stage...");
            shareOnSocialMedia("reddit");
            Platform.runLater(() -> {
                System.out.println("Closing stage for Reddit...");
                dialogStage.close();
            });
        });
        cancelButton.setOnAction(e -> {
            System.out.println("Cancel button clicked, attempting to close stage...");
            Platform.runLater(() -> {
                System.out.println("Closing stage for Cancel...");
                dialogStage.close();
            });
        });

        dialogContent.getChildren().addAll(header, twitterButton, facebookButton, redditButton, cancelButton);
        dialogContent.setAlignment(Pos.CENTER);

        Scene dialogScene = new Scene(dialogContent, 300, 300);
        dialogStage.setScene(dialogScene);

        dialogStage.setOnCloseRequest(event -> {
            System.out.println("Close request triggered via 'X' button, attempting to close stage...");
            Platform.runLater(() -> {
                System.out.println("Closing stage via 'X' button...");
                dialogStage.close();
            });
        });

        System.out.println("Opening share stage...");
        dialogStage.showAndWait();
        System.out.println("Share stage closed or should have closed...");
    }
    private void addButtonHoverEffect(Button button) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), button);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), button);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), button);
        fadeIn.setToValue(0.9);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), button);
        fadeOut.setToValue(1.0);

        button.setOnMouseEntered(e -> {
            scaleIn.playFromStart();
            fadeIn.playFromStart();
        });

        button.setOnMouseExited(e -> {
            scaleOut.playFromStart();
            fadeOut.playFromStart();
        });
    }

    private void shareOnSocialMedia(final String platform) {
        String shareTitle = question.getTitle();
        String shareContent = question.getContent() != null ? question.getContent() : ""; // Ensure content isn't null

        System.out.println("Platform: " + platform);
        System.out.println("Share Title: " + shareTitle);
        System.out.println("Share Content: " + shareContent);

        if (platform.equals("twitter")) {
            String combinedText = shareTitle + "\n" + shareContent;
            if (combinedText.length() > 280) {
                int remainingLength = 280 - (shareTitle.length() + 1 + 3); // 1 for newline, 3 for "..."
                if (remainingLength > 0) {
                    shareContent = shareContent.substring(0, Math.min(remainingLength, shareContent.length())) + "...";
                } else {
                    shareTitle = shareTitle.substring(0, 277) + "...";
                    shareContent = "";
                }
            }
        } else if (platform.equals("facebook")) {
            String combinedText = shareTitle + "\n" + shareContent;
            if (combinedText.length() > 500) {
                int remainingLength = 500 - (shareTitle.length() + 1 + 3);
                if (remainingLength > 0) {
                    shareContent = shareContent.substring(0, Math.min(remainingLength, shareContent.length())) + "...";
                } else {
                    shareTitle = shareTitle.substring(0, 496) + "...";
                    shareContent = "";
                }
            }
        } else if (platform.equals("reddit")) {
            String combinedText = shareTitle + "\n" + shareContent;
            if (combinedText.length() > 1000) {
                int remainingLength = 1000 - (shareTitle.length() + 1 + 3);
                if (remainingLength > 0) {
                    shareContent = shareContent.substring(0, Math.min(remainingLength, shareContent.length())) + "...";
                } else {
                    shareTitle = shareTitle.substring(0, 996) + "...";
                    shareContent = "";
                }
            }
        }

        String navUrl;
        if (platform.equals("twitter")) {
            String tweetText = shareTitle + "\n" + shareContent;
            navUrl = "https://twitter.com/intent/tweet?text=" + encodeURIComponent(tweetText);

            Stage alertStage = new Stage();
            alertStage.setTitle("Information");

            VBox alertContent = new VBox(10);
            alertContent.setPadding(new Insets(15));
            alertContent.setAlignment(Pos.CENTER);
            alertContent.setStyle("-fx-background-color: #091221; -fx-border-color: #ff4081; -fx-border-width: 2; -fx-border-radius: 10;");

            ImageView icon = new ImageView(new Image(getClass().getResource("/forumUI/icons/alert.png").toExternalForm()));
            icon.setFitHeight(80);
            icon.setFitWidth(80);

            Label messageLabel = new Label("Twitter API free tier requires OAuth setup, which is complex for a local app. Using URL-based sharing instead.");
            messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            messageLabel.setWrapText(true);

            alertContent.getChildren().addAll(icon, messageLabel);

            Scene alertScene = new Scene(alertContent, 300, 150);
            alertScene.getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
            alertContent.getStyleClass().add("gaming-alert");

            alertStage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/alert.png").toString()));
            alertStage.setScene(alertScene);
            alertStage.show();

            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> {
                openUrlInBrowser(navUrl);
                Platform.runLater(() -> alertStage.close());
            });
            delay.play();
        } else if (platform.equals("facebook")) {
            String postText = shareTitle + "\n" + shareContent;
            navUrl = "https://www.facebook.com/sharer/sharer.php";
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(postText);
            clipboard.setContent(content);

            Stage alertStage = new Stage();
            alertStage.setTitle("Information");

            VBox alertContent = new VBox(10);
            alertContent.setPadding(new Insets(15));
            alertContent.setAlignment(Pos.CENTER);
            alertContent.setStyle("-fx-background-color: #091221; -fx-border-color: #ff4081; -fx-border-width: 2; -fx-border-radius: 10;");

            ImageView icon = new ImageView(new Image(getClass().getResource("/forumUI/icons/alert.png").toExternalForm()));
            icon.setFitHeight(80);
            icon.setFitWidth(80);

            Label messageLabel = new Label("Facebook sharing requires manual pasting. The question title and content have been copied to your clipboard. Paste them into the Facebook dialog (Ctrl+V or Cmd+V).");
            messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            messageLabel.setWrapText(true);

            alertContent.getChildren().addAll(icon, messageLabel);

            Scene alertScene = new Scene(alertContent, 300, 150);
            alertScene.getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
            alertContent.getStyleClass().add("gaming-alert");

            alertStage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/alert.png").toString()));
            alertStage.setScene(alertScene);
            alertStage.show();

            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> {
                openUrlInBrowser(navUrl);
                Platform.runLater(() -> alertStage.close());
            });
            delay.play();
        } else if (platform.equals("reddit")) {
            navUrl = "https://www.reddit.com/submit?selftext=true&title=" + encodeURIComponent(shareTitle) + "&text=" + encodeURIComponent(shareContent);
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(shareContent);
            clipboard.setContent(content);

            Stage alertStage = new Stage();
            alertStage.setTitle("Information");

            VBox alertContent = new VBox(10);
            alertContent.setPadding(new Insets(15));
            alertContent.setAlignment(Pos.CENTER);
            alertContent.setStyle("-fx-background-color: #091221; -fx-border-color: #ff4081; -fx-border-width: 2; -fx-border-radius: 10;");

            ImageView icon = new ImageView(new Image(getClass().getResource("/forumUI/icons/alert.png").toExternalForm()));
            icon.setFitHeight(80);
            icon.setFitWidth(80);

            Label messageLabel = new Label("Reddit may not pre-fill the content field. The content has been copied to your clipboard. Paste it into the 'Corps' field (Ctrl+V or Cmd+V) if needed.");
            messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            messageLabel.setWrapText(true);

            alertContent.getChildren().addAll(icon, messageLabel);

            Scene alertScene = new Scene(alertContent, 300, 150);
            alertScene.getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
            alertContent.getStyleClass().add("gaming-alert");

            alertStage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/alert.png").toString()));
            alertStage.setScene(alertScene);
            alertStage.show();

            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> {
                openUrlInBrowser(navUrl);
                Platform.runLater(() -> alertStage.close());
            });
            delay.play();
        }
    }

    private String encodeURIComponent(String input) {
        try {
            return java.net.URLEncoder.encode(input, "UTF-8")
                    .replace("+", "%20")
                    .replace("!", "%21")
                    .replace("'", "%27")
                    .replace("(", "%28")
                    .replace(")", "%29")
                    .replace("~", "%7E")
                    .replace("\n", "%0A") // Ensure newlines are encoded
                    .replace("#", "%23")  // Encode additional special characters
                    .replace("&", "%26")
                    .replace("?", "%3F");
        } catch (java.io.UnsupportedEncodingException e) {
            System.err.println("Encoding error: " + e.getMessage());
            return input;
        }
    }

    private void openUrlInBrowser(String url) {
        try {
            System.out.println("Opening URL: " + url);
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            showAlert("Erreur", "Unable to open the URL in the browser: " + e.getMessage());
        }
    }

    private void showReportForm(int reportedUserId, String evidence) {
        Stage reportStage = new Stage();
        reportStage.setTitle("Report Question");

        VBox reportForm = new VBox(10);
        reportForm.setPadding(new Insets(10));
        reportForm.setStyle("-fx-background-color: #091221; -fx-border-color: #666; -fx-border-width: 1;");

        Label title = new Label("Report this question");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        ComboBox<ReportReason> reasonComboBox = new ComboBox<>();
        reasonComboBox.getItems().addAll(ReportReason.values());
        reasonComboBox.setPromptText("Select a reason");
        reasonComboBox.setStyle("-fx-background-color: #091221; -fx-text-fill: white;");

        TextArea evidenceField = new TextArea(evidence);
        evidenceField.setEditable(false);
        evidenceField.setWrapText(true);
        evidenceField.setPrefHeight(100);
        evidenceField.setStyle("-fx-control-inner-background: #555; -fx-text-fill: white;");

        Button submitReportButton = new Button("Submit Report");
        submitReportButton.setStyle("-fx-background-color: #ff4081; -fx-text-fill: white; -fx-font-size: 14px;");
        submitReportButton.setOnAction(event -> {
            ReportReason reason = reasonComboBox.getValue();
            if (reason == null) {
                showAlert("Erreur", "Please select a reason for the report.");
                return;
            }

            Report report = new Report(userId, reportedUserId, reason, evidence);
            report.setStatus(ReportStatus.PENDING);
            executorService.submit(() -> {
                reportService.addReport(report);
                Platform.runLater(() -> {
                    showSuccessAlert("Success", "Report submitted successfully!");
                    reportStage.close();
                });
            });
        });

        reportForm.getChildren().addAll(title, reasonComboBox, evidenceField, submitReportButton);

        Scene scene = new Scene(reportForm, 300, 250);
        scene.getStylesheets().add(getClass().getResource("/forumUI/forum.css").toExternalForm());
        reportStage.setScene(scene);
        reportStage.show();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");
        alert.showAndWait();
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

    private void setupBoundsListener() {
        scrollPane = findParentScrollPane(contentVBox);
        if (scrollPane != null) {
            boundsListener = (observable, oldValue, newValue) -> {
                boundsDebounceTimer.setOnFinished(event -> checkIntersectionAndStopVideo());
                boundsDebounceTimer.playFromStart();
            };
            scrollPane.boundsInParentProperty().addListener(boundsListener);
            scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                boundsDebounceTimer.setOnFinished(event -> checkIntersectionAndStopVideo());
                boundsDebounceTimer.playFromStart();
            });

            Platform.runLater(this::checkIntersectionAndStopVideo);
        }

        contentVBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null && newScene == null) {
                if (scrollPane != null && boundsListener != null) {
                    scrollPane.boundsInParentProperty().removeListener(boundsListener);
                }
                stopVideo();
            }
        });
    }

    private ScrollPane findParentScrollPane(Parent node) {
        Parent current = node;
        while (current != null) {
            if (current instanceof ScrollPane) {
                return (ScrollPane) current;
            }
            current = current.getParent();
        }
        return null;
    }

    private void checkIntersectionAndStopVideo() {
        if (!isPlaying || mediaPlayer == null || mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            return;
        }

        if (!isFullyInViewport()) {
            stopVideo();
        }
    }

    private boolean isFullyInViewport() {
        if (scrollPane == null || !contentVBox.isVisible() || contentVBox.getScene() == null) {
            return false;
        }

        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double contentHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
        double vvalue = scrollPane.getVvalue();
        double viewportTop = vvalue * (contentHeight - viewportHeight);
        double viewportBottom = viewportTop + viewportHeight;

        Bounds cardBounds = contentVBox.localToParent(contentVBox.getBoundsInLocal());
        double cardTop = cardBounds.getMinY();
        double cardBottom = cardBounds.getMaxY();

        return !(cardBottom <= viewportTop || cardTop >= viewportBottom);
    }

    public void stopVideo() {
        if (mediaPlayer != null) {
            try {
                isPlaying = false;
                savedVolume = mediaPlayer.getVolume();
                mediaPlayer.pause();
                mediaPlayer.setVolume(0.0);
                mediaPlayer.setMute(true);
                playPauseButton.setText("▶");

                if (isFullScreen) {
                    toggleFullScreen();
                }
            } catch (Exception e) {
                System.err.println("Error pausing MediaPlayer: " + e.getMessage());
            }
        }
    }

    public void disposeVideo() {
        if (mediaPlayer == null) {
            if (isFullScreen) {
                isFullScreen = false;
                fullScreenLayout = null;
            }
            return;
        }

        Platform.runLater(() -> {
            try {
                isPlaying = false;
                mediaPlayer.stop();
                mediaPlayer.setVolume(0.0);
                mediaPlayer.setMute(true);

                if (currentTimeListener != null) {
                    mediaPlayer.currentTimeProperty().removeListener(currentTimeListener);
                    currentTimeListener = null;
                }
                if (valueChangingListener != null) {
                    progressSlider.valueChangingProperty().removeListener(valueChangingListener);
                    valueChangingListener = null;
                }
                if (volumeListener != null) {
                    volumeSlider.valueProperty().removeListener(volumeListener);
                    volumeListener = null;
                }

                mediaPlayer.dispose();

                synchronized (activeMediaPlayers) {
                    activeMediaPlayers.remove(mediaPlayer);
                }
                mediaPlayerCache.remove(question.getMediaPath() + "_video");

                questionVideo.setMediaPlayer(null);
                mediaPlayer = null;
                playPauseButton.setText("▶");

                if (isFullScreen) {
                    isFullScreen = false;
                    fullScreenLayout = null;
                }
            } catch (Exception e) {
                System.err.println("Error disposing MediaPlayer: " + e.getMessage());
            }
        });

        shutdownExecutor.schedule(() -> {
            Platform.runLater(() -> {
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.dispose();
                        mediaPlayer = null;
                    } catch (Exception e) {
                        System.err.println("Final cleanup failed: " + e.getMessage());
                    }
                }
            });
        }, 200, TimeUnit.MILLISECONDS);
    }

    public static void stopAllVideos() {
        List<QuestionCardController> controllersCopy;
        synchronized (allControllers) {
            controllersCopy = new ArrayList<>(allControllers);
        }
        Platform.runLater(() -> {
            for (QuestionCardController controller : controllersCopy) {
                controller.disposeVideo();
            }
        });
    }

    public void updatePrivilegeUI(Utilisateur user) {
        TextFlow authorFlow = new TextFlow();
        Text usernameText = new Text(user.getNickname());
        usernameText.setStyle("-fx-fill: white;");

        if (user.getRole() == Role.ADMIN) {
            Text adminText = new Text("Admin ");
            adminText.setStyle("-fx-fill: #009dff;");
            authorFlow.getChildren().addAll(adminText, usernameText);
        } else {
            authorFlow.getChildren().add(usernameText);
        }
        commentAuthor.setText(user.getNickname());
        String privilege = user.getPrivilege() != null ? user.getPrivilege() : "regular";
        switch (privilege) {
            case "top_contributor":
                commentAuthor.setStyle("-fx-text-fill: silver;");
                crownIcon.setImage(new Image("/forumUI/icons/silver_crown.png"));
                crownIcon.setVisible(true);
                break;
            case "top_fan":
                commentAuthor.setStyle("-fx-text-fill: gold;");
                crownIcon.setImage(new Image("/forumUI/icons/crown.png"));
                crownIcon.setVisible(true);
                break;
            default:
                commentAuthor.setStyle("-fx-text-fill: white;");
                crownIcon.setVisible(false);
                break;
        }

        commentAuthor.setGraphic(authorFlow);
        commentAuthor.setText("");
    }

    private void animatePrivilegeChange(ImageView crownIcon, boolean isVisible) {
        FadeTransition fade = new FadeTransition(Duration.millis(500), crownIcon);
        ScaleTransition scale = new ScaleTransition(Duration.millis(500), crownIcon);

        if (isVisible) {
            crownIcon.setVisible(true);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            scale.setFromX(0.5);
            scale.setFromY(0.5);
            scale.setToX(1.0);
            scale.setToY(1.0);
        } else {
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            scale.setFromX(1.0);
            scale.setFromY(1.0);
            scale.setToX(0.5);
            scale.setToY(0.5);
            fade.setOnFinished(e -> crownIcon.setVisible(false));
        }

        fade.play();
        scale.play();
    }

    public Question getQuestion() {
        return question;
    }

    private void loadQuestionMediaAsync() {
        String mediaPath = question.getMediaPath();
        String mediaType = question.getMediaType();
        if (mediaPath == null || mediaPath.isEmpty() || mediaType == null) {
            Platform.runLater(this::resetMediaState);
            return;
        }

        executorService.submit(() -> {
            try {
                String baseDir = "C:\\xampp\\htdocs\\img";
                File file = new File(baseDir, mediaPath);
                if (!file.exists()) {
                    System.err.println("Media file not found: " + file.getAbsolutePath());
                    Platform.runLater(this::resetMediaState);
                    return;
                }

                String fileUri = file.toURI().toString();
                String cacheKey = mediaPath + "_" + mediaType;

                if ("image".equals(mediaType)) {
                    Image image = imageCache.computeIfAbsent(cacheKey, k -> {
                        Image img = new Image(fileUri, originalWidth, originalHeight, true, true);
                        if (img.isError()) {
                            System.err.println("Failed to cache image: " + file.getAbsolutePath() + " - " + img.getException().getMessage());
                        }
                        return img;
                    });

                    if (!image.isError()) {
                        Platform.runLater(() -> {
                            questionImage.setImage(image);
                            questionImage.setVisible(true);
                            questionImage.setManaged(true);
                            videoWrapper.setVisible(false);
                            videoWrapper.setManaged(false);
                            mediaContainer.setVisible(true);
                            mediaContainer.setManaged(true);
                        });
                    } else {
                        System.err.println("Failed to load image: " + file.getAbsolutePath() + " - " + image.getException());
                        Platform.runLater(() -> {
                            showFallbackMedia();
                            showAlert("Erreur", "Impossible de charger l'image: " + image.getException().getMessage());
                        });
                    }
                } else if ("video".equals(mediaType)) {
                    if (!isValidVideoFormat(file) || !isPlayableVideo(file)) {
                        System.err.println("Invalid or unplayable video format: " + file.getAbsolutePath());
                        Platform.runLater(() -> {
                            showFallbackMedia();
                            showAlert("Erreur", "Format vidéo non valide ou fichier corrompu.");
                        });
                        return;
                    }

                    Media media = new Media(fileUri);
                    mediaPlayer = mediaPlayerCache.computeIfAbsent(cacheKey, k -> {
                        MediaPlayer player = new MediaPlayer(media);
                        player.setAutoPlay(false);
                        player.setOnError(() -> {
                            System.err.println("MediaPlayer error for file " + file.getAbsolutePath() + ": " + player.getError().getMessage());
                            Platform.runLater(() -> {
                                showFallbackMedia();
                                showAlert("Erreur", "Impossible de lire la vidéo: " + player.getError().getMessage());
                            });
                        });
                        player.setOnReady(() -> {
                            double width = player.getMedia().getWidth();
                            double height = player.getMedia().getHeight();
                            if (width <= 0 || height <= 0) {
                                System.err.println("Invalid media dimensions for file: " + file.getAbsolutePath());
                                Platform.runLater(this::showFallbackMedia);
                            } else {
                                player.pause();
                                player.setMute(true);
                            }
                        });
                        return player;
                    });

                    Platform.runLater(() -> {
                        questionVideo.setMediaPlayer(mediaPlayer);
                        questionVideo.setFitWidth(originalWidth);
                        questionVideo.setFitHeight(originalHeight);
                        videoWrapper.setVisible(true);
                        videoWrapper.setManaged(true);
                        questionImage.setVisible(false);
                        questionImage.setManaged(false);
                        mediaContainer.setVisible(true);
                        mediaContainer.setManaged(true);
                        setupVideoControls();
                        initializeVideoListeners();
                    });
                }
            } catch (Exception e) {
                System.err.println("Error loading question media for file " + mediaPath + ": " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showFallbackMedia();
                    showAlert("Erreur", "Erreur lors du chargement du média: " + e.getMessage());
                });
            }
        });
    }

    private boolean isValidVideoFormat(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".mp4");
    }

    private boolean isPlayableVideo(File file) {
        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer testPlayer = new MediaPlayer(media);
            testPlayer.setOnError(() -> { throw new RuntimeException("Invalid media"); });
            Thread.sleep(100);
            testPlayer.dispose();
            return true;
        } catch (Exception e) {
            System.err.println("Video is not playable: " + file.getAbsolutePath() + " - " + e.getMessage());
            return false;
        }
    }

    private void showFallbackMedia() {
        Image fallbackImage = imageCache.computeIfAbsent("video_error", k ->
                new Image(getClass().getResource("/forumUI/icons/videoError.jpg").toExternalForm(), originalWidth, originalHeight, true, true)
        );
        if (!fallbackImage.isError()) {
            questionImage.setImage(fallbackImage);
            questionImage.setVisible(true);
            questionImage.setManaged(true);
            videoWrapper.setVisible(false);
            videoWrapper.setManaged(false);
            mediaContainer.setVisible(true);
            mediaContainer.setManaged(true);
        } else {
            System.err.println("Failed to load fallback image: " + fallbackImage.getException());
            resetMediaState();
        }
    }

    private void setupVideoControls() {
        videoControlBar.setPrefWidth(originalWidth);
        videoControlBar.setMinWidth(originalWidth);
        videoControlBar.setMaxWidth(originalWidth);
        videoControlBar.setPrefHeight(controlBarHeight);
        videoControlBar.setMinHeight(controlBarHeight);
        videoControlBar.setMaxHeight(controlBarHeight);

        playPauseButton.setOnAction(event -> {
            if (mediaPlayer == null) return;
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                isPlaying = false;
                playPauseButton.setText("▶");
            } else {
                synchronized (activeMediaPlayers) {
                    for (MediaPlayer otherPlayer : new ArrayList<>(activeMediaPlayers)) {
                        if (otherPlayer != mediaPlayer) {
                            otherPlayer.pause();
                            otherPlayer.setVolume(0.0);
                            otherPlayer.setMute(true);
                        }
                    }
                    activeMediaPlayers.clear();
                    activeMediaPlayers.add(mediaPlayer);
                }

                try {
                    mediaPlayer.play();
                    isPlaying = true;
                    playPauseButton.setText("⏸");
                    mediaPlayer.setVolume(savedVolume);
                    mediaPlayer.setMute(false);
                    volumeSlider.setValue(savedVolume * 100);
                } catch (Exception e) {
                    System.err.println("Error playing MediaPlayer: " + e.getMessage());
                }
            }
        });

        fullScreenButton.setOnAction(event -> toggleFullScreen());

        videoControlBar.setOpacity(1.0);
    }

    private void initializeVideoListeners() {
        if (mediaPlayer == null) return;

        currentTimeListener = (obs, old, newValue) -> {
            if (!progressSlider.isValueChanging() && mediaPlayer != null) {
                double duration = mediaPlayer.getTotalDuration().toSeconds();
                double current = newValue.toSeconds();
                progressSlider.setValue(duration > 0 ? (current / duration * 100) : 0);
                updateTimeLabel(current, duration);
            }
        };
        mediaPlayer.currentTimeProperty().addListener(currentTimeListener);

        valueChangingListener = (obs, wasChanging, isChanging) -> {
            if (!isChanging && mediaPlayer != null) {
                double duration = mediaPlayer.getTotalDuration().toSeconds();
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue() * duration / 100));
            }
        };
        progressSlider.valueChangingProperty().addListener(valueChangingListener);

        progressSlider.setOnMouseDragged(event -> {
            if (mediaPlayer != null) {
                double duration = mediaPlayer.getTotalDuration().toSeconds();
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue() * duration / 100));
            }
        });

        volumeListener = (obs, old, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue() / 100.0);
                mediaPlayer.setMute(false);
                savedVolume = mediaPlayer.getVolume();
            }
        };
        volumeSlider.valueProperty().addListener(volumeListener);
    }

    private void updateTimeLabel(double current, double duration) {
        int currentMins = (int) (current / 60);
        int currentSecs = (int) (current % 60);
        int durationMins = (int) (duration / 60);
        int durationSecs = (int) (duration % 60);
        timeLabel.setText(String.format("%d:%02d / %d:%02d", currentMins, currentSecs, durationMins, durationSecs));
    }
    private void showStyledAlert(String title, String message, String iconPath, String stageIconPath,
                                 String buttonText, double iconHeight, double iconWidth) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(new Image(getClass().getResource(iconPath).toExternalForm()));
        icon.setFitHeight(iconHeight);
        icon.setFitWidth(iconWidth);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource(stageIconPath).toString()));

        ButtonType okButton = new ButtonType(buttonText, ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), alert.getDialogPane());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        alert.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) fadeIn.play();
        });

        alert.showAndWait();
    }
    private void toggleFullScreen() {
        Stage stage = (Stage) questionVideo.getScene().getWindow();
        if (!isFullScreen) {
            boolean wasPlaying = mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
            if (wasPlaying) mediaPlayer.pause();

            originalScene = stage.getScene();

            fullScreenLayout = new BorderPane();
            fullScreenLayout.setStyle("-fx-background-color: black;");

            videoWrapper.getChildren().removeAll(questionVideo, videoControlBar);

            StackPane videoContainer = new StackPane();
            videoContainer.setAlignment(Pos.CENTER);
            videoContainer.getChildren().add(questionVideo);
            fullScreenLayout.setCenter(videoContainer);

            fullScreenLayout.setBottom(videoControlBar);
            BorderPane.setAlignment(videoControlBar, Pos.BOTTOM_CENTER);

            double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
            double reducedWidth = screenWidth * controlBarWidthPercentage;

            videoControlBar.setPrefWidth(reducedWidth);
            videoControlBar.setMinWidth(reducedWidth);
            videoControlBar.setMaxWidth(reducedWidth);
            videoControlBar.setPrefHeight(controlBarHeight);
            videoControlBar.setMinHeight(controlBarHeight);
            videoControlBar.setMaxHeight(controlBarHeight);

            Scene fullScreenScene = new Scene(fullScreenLayout, screenWidth, screenHeight);
            URL cssResource = getClass().getResource("/forumUI/forum.css");
            if (cssResource != null) fullScreenScene.getStylesheets().add(cssResource.toExternalForm());
            else System.err.println("Failed to load stylesheet: /forumUI/forum.css");
            stage.setScene(fullScreenScene);

            double videoWidth = mediaPlayer.getMedia().getWidth();
            double videoHeight = mediaPlayer.getMedia().getHeight();
            double videoAspectRatio = (videoHeight > 0) ? (videoWidth / videoHeight) : 1.0;

            double availableHeight = screenHeight - controlBarHeight;
            double calculatedVideoWidth, calculatedVideoHeight;

            if ((screenWidth / videoAspectRatio) <= availableHeight) {
                calculatedVideoWidth = screenWidth;
                calculatedVideoHeight = screenWidth / videoAspectRatio;
            } else {
                calculatedVideoHeight = availableHeight;
                calculatedVideoWidth = availableHeight * videoAspectRatio;
            }

            questionVideo.setFitWidth(calculatedVideoWidth);
            questionVideo.setFitHeight(calculatedVideoHeight);
            questionVideo.setPreserveRatio(true);

            videoControlBar.setVisible(true);
            videoControlBar.setManaged(true);
            videoControlBar.setOpacity(1.0);
            fullScreenButton.setVisible(true);
            fullScreenButton.setManaged(true);

            fullScreenButton.setText("⤹");
            stage.setFullScreen(true);
            isFullScreen = true;

            videoControlBar.setOnMouseEntered(null);
            videoControlBar.setOnMouseExited(null);
            fullScreenLayout.setOnMouseEntered(null);
            fullScreenLayout.setOnMouseExited(null);

            fullScreenScene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    toggleFullScreen();
                    event.consume();
                }
            });

            if (wasPlaying) {
                mediaPlayer.play();
                isPlaying = true;
            }
        } else {
            boolean wasPlaying = mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
            if (wasPlaying) mediaPlayer.pause();

            stage.setScene(originalScene);
            stage.setFullScreen(false);

            if (fullScreenLayout != null) {
                fullScreenLayout.getChildren().removeAll(questionVideo, videoControlBar);
                videoWrapper.getChildren().setAll(questionVideo, videoControlBar);
                fullScreenLayout.setOnMouseEntered(null);
                fullScreenLayout.setOnMouseExited(null);
                fullScreenLayout = null;
            }

            questionVideo.fitWidthProperty().unbind();
            questionVideo.fitHeightProperty().unbind();
            questionVideo.setFitWidth(originalWidth);
            questionVideo.setFitHeight(originalHeight);

            videoControlBar.prefWidthProperty().unbind();
            videoControlBar.setPrefWidth(originalWidth);
            videoControlBar.setMinWidth(originalWidth);
            videoControlBar.setMaxWidth(originalWidth);
            videoControlBar.setPrefHeight(controlBarHeight);
            videoControlBar.setMinHeight(controlBarHeight);
            videoControlBar.setMaxHeight(controlBarHeight);

            videoControlBar.setOpacity(1.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), videoControlBar);
            fadeIn.setToValue(1.0);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), videoControlBar);
            fadeOut.setToValue(0.0);
            videoWrapper.setOnMouseEntered(event -> {
                fadeOut.stop();
                fadeIn.playFromStart();
            });
            videoWrapper.setOnMouseExited(event -> {
                if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    fadeIn.stop();
                    fadeOut.playFromStart();
                }
            });

            mediaContainer.setVisible(true);
            mediaContainer.setManaged(true);
            videoWrapper.setVisible(true);
            videoWrapper.setManaged(true);
            questionVideo.setVisible(true);
            questionVideo.setManaged(true);
            videoControlBar.setVisible(true);
            videoControlBar.setManaged(true);

            fullScreenButton.setText("⛶");
            isFullScreen = false;

            if (wasPlaying) {
                mediaPlayer.play();
                isPlaying = true;
            }
        }
    }

    private void resetMediaState() {
        questionImage.setImage(null);
        questionImage.setVisible(false);
        questionImage.setManaged(false);
        if (mediaPlayer != null) {
            disposeVideo();
        }
        questionVideo.setMediaPlayer(null);
        videoWrapper.setVisible(false);
        videoWrapper.setManaged(false);
        mediaContainer.setVisible(false);
        mediaContainer.setManaged(false);
        contentVBox.getParent().getStyleClass().remove("has-image");
    }

    public void displayReactions() {
        reactionContainer.getChildren().clear();
        Map<String, Integer> reactions = question.getReactions();
        for (Map.Entry<String, Integer> entry : reactions.entrySet()) {
            String emojiUrl = entry.getKey();
            int count = entry.getValue();
            HBox reactionBox = new HBox(2);
            reactionBox.setAlignment(Pos.CENTER_LEFT);

            if (emojiUrl.contains("twemoji")) {
                Image emojiImage = imageCache.computeIfAbsent(emojiUrl, k -> new Image(emojiUrl, 32, 32, true, true));
                if (!emojiImage.isError()) {
                    ImageView emojiIcon = new ImageView(emojiImage);
                    emojiIcon.setFitWidth(32);
                    emojiIcon.setFitHeight(32);
                    emojiIcon.setPreserveRatio(true);
                    emojiIcon.getStyleClass().add("reaction-emoji-icon");

                    Label countLabel = new Label(String.valueOf(count));
                    countLabel.getStyleClass().add("reaction-count-label");

                    reactionBox.getChildren().addAll(emojiIcon, countLabel);
                } else {
                    System.err.println("Failed to load reaction emoji for URL: " + emojiUrl + " - " + emojiImage.getException());
                    Label fallbackLabel = new Label(getEmojiNameFromUrl(emojiUrl) + " " + count);
                    fallbackLabel.getStyleClass().add("reaction-label");
                    reactionContainer.getChildren().add(fallbackLabel);
                    continue;
                }
            } else {
                Label fallbackLabel = new Label(getEmojiNameFromUrl(emojiUrl) + " " + count);
                fallbackLabel.getStyleClass().add("reaction-label");
                reactionContainer.getChildren().add(fallbackLabel);
                continue;
            }

            reactionContainer.getChildren().add(reactionBox);
        }
    }

    private String getEmojiNameFromUrl(String url) {
        if (url.contains("twemoji")) {
            String hexcode = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".")).replace("72x72_", "");
            return switch (hexcode.toLowerCase()) {
                case "1f44d" -> "Like";
                case "2764" -> "Love";
                case "1f602" -> "Haha";
                case "1f62e" -> "Sad";
                case "1f620" -> "Angry";
                case "1f60d" -> "Wow";
                case "1f44f" -> "Applause";
                case "1f525" -> "Fire";
                case "1f4af" -> "100";
                case "1f389" -> "Party";
                case "1f44c" -> "OK";
                case "1f499" -> "Blue Heart";
                case "1f60a" -> "Cool";
                case "1f4a9" -> "Poop";
                case "1f680" -> "Rocket";
                case "1f3c6" -> "Trophy";
                case "1f381" -> "Gift";
                case "1f3ae" -> "Game";
                case "1f3b2" -> "Die";
                case "1f4a5" -> "Collision";
                case "1f64f" -> "Pray";
                case "1f3c3" -> "Runner";
                case "1f451" -> "Crown";
                case "1f3b0" -> "Slots";
                default -> hexcode;
            };
        }
        return url;
    }

    public void displayUserReaction() {
        String userReaction = question.getUserReaction();
        if (userReaction != null && !userReaction.isEmpty()) {
            if (userReaction.contains("twemoji")) {
                Image emojiImage = imageCache.computeIfAbsent(userReaction, k -> new Image(userReaction, 30, 30, true, true));
                if (!emojiImage.isError()) {
                    selectedEmojiImage.setImage(emojiImage);
                } else {
                    System.err.println("Failed to load selected emoji: " + userReaction + " - " + emojiImage.getException());
                    selectedEmojiImage.setImage(null);
                }
            } else {
                selectedEmojiImage.setImage(null);
            }
        } else {
            selectedEmojiImage.setImage(null);
        }
    }

    private void showEmojiPicker() {
        Popup popup = new Popup();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");

        VBox emojiBox = new VBox(8);
        emojiBox.setPadding(new Insets(15));
        emojiBox.getStyleClass().add("emoji-picker");

        executorService.submit(() -> {
            try {
                List<Image> emojis = EmojiService.fetchEmojis();
                Platform.runLater(() -> {
                    HBox row = new HBox(8);
                    int emojiCount = 0;
                    for (Image emoji : emojis) {
                        ImageView emojiImage = new ImageView(emoji);
                        emojiImage.setFitWidth(30);
                        emojiImage.setFitHeight(30);
                        emojiImage.setPreserveRatio(true);
                        emojiImage.setOnMouseClicked(e -> {
                            forumController.handleReaction(question, emoji.getUrl());
                            popup.hide();
                            displayReactions();
                            displayUserReaction();
                        });

                        emojiImage.setOnMouseEntered(e -> {
                            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), emojiImage);
                            scaleIn.setToX(1.1);
                            scaleIn.setToY(1.1);
                            scaleIn.play();
                        });
                        emojiImage.setOnMouseExited(e -> {
                            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), emojiImage);
                            scaleOut.setToX(1.0);
                            scaleOut.setToY(1.0);
                            scaleOut.play();
                        });

                        row.getChildren().add(emojiImage);
                        emojiCount++;
                        if (emojiCount % 7 == 0) {
                            emojiBox.getChildren().add(row);
                            row = new HBox(8);
                        }
                    }
                    if (!row.getChildren().isEmpty()) {
                        emojiBox.getChildren().add(row);
                    }

                    scrollPane.setContent(emojiBox);
                    scrollPane.setPrefSize(250, 200);
                    popup.getContent().add(scrollPane);
                    popup.show(reactButton, reactButton.getScene().getWindow().getX() + reactButton.localToScene(0, 0).getX(),
                            reactButton.getScene().getWindow().getY() + reactButton.localToScene(0, 0).getY() + reactButton.getHeight());
                    popup.setAutoHide(true);
                });
            } catch (Exception e) {
                System.err.println("Failed to load emojis: " + e.getMessage());
                Platform.runLater(() -> {
                    String[] fallbackPaths = {
                            "/forumUI/icons/like.png", "/forumUI/icons/love.png", "/forumUI/icons/haha.png", "/forumUI/icons/wow.png",
                            "/forumUI/icons/sad.png", "/forumUI/icons/angry.png", "/forumUI/icons/applause.png", "/forumUI/icons/fire.png",
                            "/forumUI/icons/100.png", "/forumUI/icons/party.png", "/forumUI/icons/ok.png", "/forumUI/icons/blue_heart.png",
                            "/forumUI/icons/cool.png", "/forumUI/icons/poop.png", "/forumUI/icons/rocket.png", "/forumUI/icons/trophy.png",
                            "/forumUI/icons/gift.png", "/forumUI/icons/game.png", "/forumUI/icons/die.png", "/forumUI/icons/collision.png",
                            "/forumUI/icons/pray.png", "/forumUI/icons/runner.png", "/forumUI/icons/crown.png", "/forumUI/icons/slots.png"
                    };
                    HBox row = new HBox(8);
                    int emojiCount = 0;
                    for (String path : fallbackPaths) {
                        Image fallbackImage = imageCache.computeIfAbsent(path, k -> new Image(getClass().getResourceAsStream(path), 30, 30, true, true));
                        ImageView emojiImage = new ImageView(fallbackImage);
                        emojiImage.setFitWidth(30);
                        emojiImage.setFitHeight(30);
                        emojiImage.setPreserveRatio(true);
                        emojiImage.setOnMouseClicked(m -> {
                            forumController.handleReaction(question, path);
                            popup.hide();
                            displayReactions();
                            displayUserReaction();
                        });

                        emojiImage.setOnMouseEntered(m -> {
                            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), emojiImage);
                            scaleIn.setToX(1.1);
                            scaleIn.setToY(1.1);
                            scaleIn.play();
                        });
                        emojiImage.setOnMouseExited(m -> {
                            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), emojiImage);
                            scaleOut.setToX(1.0);
                            scaleOut.setToY(1.0);
                            scaleOut.play();
                        });

                        row.getChildren().add(emojiImage);
                        emojiCount++;
                        if (emojiCount % 7 == 0) {
                            emojiBox.getChildren().add(row);
                            row = new HBox(8);
                        }
                    }
                    if (!row.getChildren().isEmpty()) {
                        emojiBox.getChildren().add(row);
                    }

                    scrollPane.setContent(emojiBox);
                    scrollPane.setPrefSize(350, 300);
                    popup.getContent().add(scrollPane);
                    popup.show(reactButton, reactButton.getScene().getWindow().getX() + reactButton.localToScene(0, 0).getX(),
                            reactButton.getScene().getWindow().getY() + reactButton.localToScene(0, 0).getY() + reactButton.getHeight());
                    popup.setAutoHide(true);
                });
            }
        });
    }

    private void setGameIcon(String gameName) {
        Games game = gamesService.getByName(gameName);
        if (game != null && game.getImagePath() != null && !game.getImagePath().isEmpty()) {
            File file = new File(game.getImagePath());
            if (file.exists()) {
                String cacheKey = game.getImagePath();
                Image image = imageCache.computeIfAbsent(cacheKey, k -> {
                    Image img = new Image(file.toURI().toString(), 100, 100, true, true);
                    if (img.isError()) {
                        System.err.println("Failed to load game image: " + game.getImagePath() + " - " + img.getException());
                    }
                    return img;
                });
                if (!image.isError()) {
                    gameIcon.setImage(image);
                } else {
                    System.err.println("Failed to load game image: " + game.getImagePath());
                }
            } else {
                System.out.println("Game image file not found: " + game.getImagePath());
            }
        }
    }

    private void openQuestionDetails(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/QuestionDetails.fxml"));
            Parent root = loader.load();
            QuestionDetailsController controller = loader.getController();
            controller.loadQuestionDetails(question);
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}