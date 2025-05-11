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
import javafx.scene.layout.*;
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
import java.util.Arrays;
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
    @FXML private Label selectedEmojiImage; // Changed from ImageView to Label
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
    private static final String IMAGE_BASE_DIR = "C:\\xampp\\htdocs\\img\\games\\";

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
                    Platform.runLater(() -> {
                        updatePrivilegeUI(user);
                        animatePrivilegeChange(crownIcon, !"regular".equals(user.getPrivilege()));
                    });
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
        dialogStage.setTitle("Partager la Question");

        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(15));
        dialogContent.setStyle("-fx-background-color: #091221; -fx-border-color: #ff4081; -fx-border-width: 2; -fx-border-radius: 10;");

        Label header = new Label("Partager sur les réseaux sociaux");
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

        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20 10 20;");
        ImageView cancelIcon = new ImageView(new Image(getClass().getResourceAsStream("/forumUI/icons/cancel.png")));
        cancelIcon.setFitHeight(20);
        cancelIcon.setFitWidth(20);
        cancelButton.setGraphic(cancelIcon);
        addButtonHoverEffect(cancelButton);

        twitterButton.setOnAction(e -> {
            shareOnSocialMedia("twitter");
            dialogStage.close();
        });
        facebookButton.setOnAction(e -> {
            shareOnSocialMedia("facebook");
            dialogStage.close();
        });
        redditButton.setOnAction(e -> {
            shareOnSocialMedia("reddit");
            dialogStage.close();
        });
        cancelButton.setOnAction(e -> dialogStage.close());

        dialogContent.getChildren().addAll(header, twitterButton, facebookButton, redditButton, cancelButton);
        dialogContent.setAlignment(Pos.CENTER);

        Scene dialogScene = new Scene(dialogContent, 300, 300);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
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
        String shareContent = question.getContent() != null ? question.getContent() : "";

        if (platform.equals("twitter")) {
            String combinedText = shareTitle + "\n" + shareContent;
            if (combinedText.length() > 280) {
                int remainingLength = 280 - (shareTitle.length() + 1 + 3);
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
            showAlert("Information", "L'API Twitter nécessite une configuration OAuth complexe pour une application locale.\nNous utilisons un partage basé sur URL à la place.");
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> openUrlInBrowser(navUrl));
            delay.play();
        } else if (platform.equals("facebook")) {
            String postText = shareTitle + "\n" + shareContent;
            navUrl = "https://www.facebook.com/sharer/sharer.php";
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(postText);
            clipboard.setContent(content);
            showAlert("Information", "Le partage sur Facebook nécessite un collage manuel.\nLe titre et le contenu de la question ont été copiés dans votre presse-papiers.\nCollez-les dans la boîte de dialogue Facebook (Ctrl+V ou Cmd+V).");
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> openUrlInBrowser(navUrl));
            delay.play();
        } else if (platform.equals("reddit")) {
            navUrl = "https://www.reddit.com/submit?selftext=true&title=" + encodeURIComponent(shareTitle) + "&text=" + encodeURIComponent(shareContent);
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(shareContent);
            clipboard.setContent(content);
            showAlert("Information", "Reddit peut ne pas pré-remplir le champ de contenu.\nLe contenu a été copié dans votre presse-papiers.\nCollez-le dans le champ 'Corps' (Ctrl+V ou Cmd+V) si nécessaire.");
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> openUrlInBrowser(navUrl));
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
                    .replace("\n", "%0A")
                    .replace("#", "%23")
                    .replace("&", "%26")
                    .replace("?", "%3F");
        } catch (java.io.UnsupportedEncodingException e) {
            System.err.println("Erreur d'encodage: " + e.getMessage());
            return input;
        }
    }

    private void openUrlInBrowser(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir l'URL dans le navigateur:\n" + e.getMessage());
        }
    }

    private void showReportForm(int reportedUserId, String evidence) {
        Stage reportStage = new Stage();
        reportStage.setTitle("Signaler la Question");

        VBox reportForm = new VBox(10);
        reportForm.setPadding(new Insets(10));
        reportForm.setStyle("-fx-background-color: #091221; -fx-border-color: #666; -fx-border-width: 1;");

        Label title = new Label("Signaler cette question");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        ComboBox<ReportReason> reasonComboBox = new ComboBox<>();
        reasonComboBox.getItems().addAll(ReportReason.values());
        reasonComboBox.setPromptText("Sélectionner une raison");
        reasonComboBox.setStyle("-fx-background-color: #091221; -fx-text-fill: white;");

        TextArea evidenceField = new TextArea(evidence);
        evidenceField.setEditable(false);
        evidenceField.setWrapText(true);
        evidenceField.setPrefHeight(100);
        evidenceField.setStyle("-fx-control-inner-background: #555; -fx-text-fill: white;");

        Button submitReportButton = new Button("Soumettre le Signalement");
        submitReportButton.setStyle("-fx-background-color: #ff4081; -fx-text-fill: white; -fx-font-size: 14px;");
        submitReportButton.setOnAction(event -> {
            ReportReason reason = reasonComboBox.getValue();
            if (reason == null) {
                showAlert("Erreur", "Veuillez sélectionner une raison pour le signalement.");
                return;
            }

            Report report = new Report(userId, reportedUserId, reason, evidence);
            report.setStatus(ReportStatus.PENDING);
            executorService.submit(() -> {
                reportService.addReport(report);
                Platform.runLater(() -> {
                    showSuccessAlert("Succès", "Signalement soumis avec succès!");
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
        Label contentLabel = new Label(message);
        contentLabel.setWrapText(true);
        alert.getDialogPane().setContent(contentLabel);
        alert.getDialogPane().setPrefWidth(400);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Label contentLabel = new Label(message);
        contentLabel.setWrapText(true);
        alert.getDialogPane().setContent(contentLabel);
        alert.getDialogPane().setPrefWidth(400);

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
                System.err.println("Erreur lors de la pause du MediaPlayer: " + e.getMessage());
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
                System.err.println("Erreur lors de la suppression du MediaPlayer: " + e.getMessage());
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
                        System.err.println("Échec du nettoyage final: " + e.getMessage());
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
        for (QuestionCardController controller : controllersCopy) {
            controller.disposeVideo();
        }
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
                    System.err.println("Fichier média introuvable: " + file.getAbsolutePath());
                    Platform.runLater(this::resetMediaState);
                    return;
                }

                String fileUri = file.toURI().toString();
                String cacheKey = mediaPath + "_" + mediaType;

                if ("image".equals(mediaType)) {
                    Image image = imageCache.computeIfAbsent(cacheKey, k -> {
                        Image img = new Image(fileUri, originalWidth, originalHeight, true, true);
                        if (img.isError()) {
                            System.err.println("Échec de la mise en cache de l'image: " + file.getAbsolutePath() + " - " + img.getException().getMessage());
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
                        System.err.println("Échec du chargement de l'image: " + file.getAbsolutePath() + " - " + image.getException());
                        Platform.runLater(() -> {
                            showFallbackMedia();
                            showAlert("Erreur", "Impossible de charger l'image:\n" + image.getException().getMessage());
                        });
                    }
                } else if ("video".equals(mediaType)) {
                    if (!isValidVideoFormat(file) || !isPlayableVideo(file)) {
                        System.err.println("Format vidéo invalide ou fichier non lisible: " + file.getAbsolutePath());
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
                            System.err.println("Erreur du MediaPlayer pour le fichier " + file.getAbsolutePath() + ": " + player.getError().getMessage());
                            Platform.runLater(() -> {
                                showFallbackMedia();
                                showAlert("Erreur", "Impossible de lire la vidéo:\n" + player.getError().getMessage());
                            });
                        });
                        player.setOnReady(() -> {
                            double width = player.getMedia().getWidth();
                            double height = player.getMedia().getHeight();
                            if (width <= 0 || height <= 0) {
                                System.err.println("Dimensions média invalides pour le fichier: " + file.getAbsolutePath());
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
                System.err.println("Erreur lors du chargement du média pour le fichier " + mediaPath + ": " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showFallbackMedia();
                    showAlert("Erreur", "Erreur lors du chargement du média:\n" + e.getMessage());
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
            testPlayer.setOnError(() -> { throw new RuntimeException("Média invalide"); });
            Thread.sleep(100);
            testPlayer.dispose();
            return true;
        } catch (Exception e) {
            System.err.println("La vidéo n'est pas lisible: " + file.getAbsolutePath() + " - " + e.getMessage());
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
            System.err.println("Échec du chargement de l'image de secours: " + fallbackImage.getException());
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
                    System.err.println("Erreur lors de la lecture du MediaPlayer: " + e.getMessage());
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
            else System.err.println("Échec du chargement de la feuille de style: /forumUI/forum.css");
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
            String emojiUnicode = entry.getKey();
            int count = entry.getValue();

            HBox reactionBox = new HBox(5);
            reactionBox.setAlignment(Pos.CENTER_LEFT);

            Label emojiLabel = new Label(emojiUnicode);
            emojiLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
            Label countLabel = new Label(String.valueOf(count));
            countLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

            reactionBox.getChildren().addAll(emojiLabel, countLabel);
            reactionContainer.getChildren().add(reactionBox);
        }
    }

    public void displayUserReaction() {
        String userReaction = question.getUserReaction();
        if (userReaction != null && !userReaction.isEmpty()) {
            selectedEmojiImage.setText(userReaction); // Use setText for Label
            selectedEmojiImage.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        } else {
            selectedEmojiImage.setText(""); // Clear the label
        }
    }

    private void showEmojiPicker() {
        Popup popup = new Popup();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #091221; -fx-border-color: #ff4081; -fx-border-width: 2;");

        VBox emojiBox = new VBox(10);
        emojiBox.setPadding(new Insets(10));
        emojiBox.setStyle("-fx-background-color: #091221;");

        executorService.submit(() -> {
            try {
                Map<String, List<EmojiService.Emoji>> categorizedEmojis = EmojiService.fetchEmojis();
                Platform.runLater(() -> {
                    for (String sentiment : Arrays.asList("positive", "negative", "neutral")) {
                        Label sectionLabel = new Label(sentiment.substring(0, 1).toUpperCase() + sentiment.substring(1));
                        sectionLabel.setStyle("-fx-text-fill: #ff4081; -fx-font-size: 14px; -fx-font-weight: bold;");
                        emojiBox.getChildren().add(sectionLabel);

                        GridPane emojiGrid = new GridPane();
                        emojiGrid.setHgap(8);
                        emojiGrid.setVgap(8);
                        emojiGrid.setPadding(new Insets(5));

                        List<EmojiService.Emoji> emojis = categorizedEmojis.get(sentiment);
                        int col = 0;
                        int row = 0;
                        for (EmojiService.Emoji emoji : emojis) {
                            Label emojiLabel = new Label(emoji.getUnicode());
                            emojiLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");
                            emojiLabel.setAlignment(Pos.CENTER);
                            emojiLabel.setPrefSize(40, 40);

                            emojiLabel.setOnMouseEntered(e -> {
                                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), emojiLabel);
                                scaleIn.setToX(1.2);
                                scaleIn.setToY(1.2);
                                scaleIn.play();
                            });
                            emojiLabel.setOnMouseExited(e -> {
                                ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), emojiLabel);
                                scaleOut.setToX(1.0);
                                scaleOut.setToY(1.0);
                                scaleOut.play();
                            });

                            emojiLabel.setOnMouseClicked(e -> {
                                forumController.handleReaction(question, emoji.getUnicode());
                                popup.hide();
                                displayReactions();
                                displayUserReaction();
                            });

                            emojiGrid.add(emojiLabel, col, row);
                            col++;
                            if (col >= 5) {
                                col = 0;
                                row++;
                            }
                        }
                        emojiBox.getChildren().add(emojiGrid);
                    }

                    scrollPane.setContent(emojiBox);
                    scrollPane.setPrefSize(250, 300);
                    popup.getContent().add(scrollPane);

                    double x = reactButton.getScene().getWindow().getX() + reactButton.localToScene(0, 0).getX();
                    double y = reactButton.getScene().getWindow().getY() + reactButton.localToScene(0, 0).getY() + reactButton.getHeight();
                    double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
                    if (y + 300 > screenHeight) {
                        y = y - 300 - reactButton.getHeight();
                    }
                    popup.show(reactButton, x, y);
                    popup.setAutoHide(true);
                });
            } catch (Exception e) {
                System.err.println("Failed to load emojis: " + e.getMessage());
                Platform.runLater(() -> showAlert("Error", "Failed to load emoji picker"));
            }
        });
    }

    private void setGameIcon(String gameName) {
        Games game = gamesService.getByName(gameName);
        if (game != null && game.getImagePath() != null && !game.getImagePath().isEmpty()) {
            String fullImagePath = IMAGE_BASE_DIR + game.getImagePath();
            File file = new File(fullImagePath);
            if (file.exists()) {
                String cacheKey = game.getImagePath();
                Image image = imageCache.computeIfAbsent(cacheKey, k -> {
                    Image img = new Image(file.toURI().toString(), 100, 100, true, true);
                    if (img.isError()) {
                        System.err.println("Échec du chargement de l'image du jeu: " + fullImagePath + " - " + img.getException());
                    }
                    return img;
                });
                if (!image.isError()) {
                    gameIcon.setImage(image);
                } else {
                    System.err.println("Échec du chargement de l'image du jeu: " + fullImagePath + " - " + image.getException());
                }
            } else {
                System.err.println("Fichier image du jeu introuvable à: " + fullImagePath);
            }
        } else {
            System.err.println("Jeu ou chemin d'image null/vide pour le jeu: " + gameName);
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
            showAlert("Erreur", "Impossible d'ouvrir les détails de la question:\n" + e.getMessage());
        }
    }
}