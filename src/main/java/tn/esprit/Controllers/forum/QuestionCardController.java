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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Models.Games;
import tn.esprit.Models.Question;
import tn.esprit.Models.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.Services.EmojiService;
import tn.esprit.Services.GamesService;
import tn.esprit.Services.QuestionService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private Question question;
    private ForumController forumController;
    private int userId = SessionManager.getInstance().getUserId();
    private UtilisateurService us = new UtilisateurService();
    private QuestionService questionService = new QuestionService();
    private GamesService gamesService = new GamesService();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final ScheduledExecutorService shutdownExecutor = Executors.newScheduledThreadPool(1);
    private static final List<QuestionCardController> allControllers = new ArrayList<>();
    private static final List<MediaPlayer> activeMediaPlayers = new ArrayList<>();
    private static final Map<String, Image> imageCache = new HashMap<>(); // Cache for images
    private static final Map<String, MediaPlayer> mediaPlayerCache = new HashMap<>(); // Cache for MediaPlayer objects
    private MediaPlayer mediaPlayer;
    private boolean isFullScreen = false;
    private Scene originalScene;
    private double originalWidth = 500;
    private double originalHeight = 350;
    private BorderPane fullScreenLayout;
    private ScrollPane scrollPane;
    private volatile boolean isPlaying = false;
    private ChangeListener<Duration> currentTimeListener;
    private ChangeListener<Boolean> valueChangingListener;
    private ChangeListener<Number> volumeListener;
    private ChangeListener<Bounds> boundsListener;
    private PauseTransition boundsDebounceTimer;
    private double savedVolume = 1.0; // Default volume (100% as per volumeSlider's initial value)
    private final double controlBarHeight = 40.0; // Fixed height for the control bar
    private final double controlBarWidthPercentage = 0.8; // 80% de la largeur de l’écran (modifiable)

    public QuestionCardController() {
        allControllers.add(this);
        boundsDebounceTimer = new PauseTransition(Duration.millis(100)); // Debounce bounds changes
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
        updateButton.setOnAction(e -> {
            stopAllVideos();
            forumController.updateQuestion(question);
        });
        deleteButton.setOnAction(e -> {
            stopAllVideos();
            forumController.deleteQuestion(question);
        });

        boolean isOwner = question.getUser().getId() == userId;
        deleteButton.setVisible(isOwner);
        updateButton.setVisible(isOwner);
        reactButton.setOnAction(e -> showEmojiPicker());

        setGameIcon(question.getGame().getGame_name());
        displayReactions();
        displayUserReaction();
        loadQuestionMediaAsync();

        updatePrivilegeUI(question.getUser());

        // Setup bounds listener for scrolling detection after the node is added to the scene
        contentVBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && scrollPane == null) {
                Platform.runLater(this::setupBoundsListener);
            }
        });

        // Initial attempt to find the ScrollPane
        setupBoundsListener();
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

            // Initial check
            Platform.runLater(this::checkIntersectionAndStopVideo);
        }

        // Remove listener when scene is unloaded
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
        if (mediaPlayer != null) {
            try {
                isPlaying = false;
                mediaPlayer.stop();
                mediaPlayer.setVolume(0.0);
                mediaPlayer.setMute(true);

                if (currentTimeListener != null) mediaPlayer.currentTimeProperty().removeListener(currentTimeListener);
                if (valueChangingListener != null) progressSlider.valueChangingProperty().removeListener(valueChangingListener);
                if (volumeListener != null) volumeSlider.valueProperty().removeListener(volumeListener);

                mediaPlayer.dispose();
                activeMediaPlayers.remove(mediaPlayer);
                mediaPlayerCache.remove(question.getMediaPath() + "_video"); // Remove from cache
            } catch (Exception e) {
                System.err.println("Error disposing MediaPlayer: " + e.getMessage());
            } finally {
                shutdownExecutor.schedule(() -> {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.setVolume(0.0);
                        mediaPlayer.setMute(true);
                        mediaPlayer.dispose();
                    }
                }, 200, TimeUnit.MILLISECONDS);

                mediaPlayer = null;
                questionVideo.setMediaPlayer(null);
                playPauseButton.setText("▶");
            }
        }
        if (isFullScreen) {
            isFullScreen = false;
            fullScreenLayout = null;
        }
    }

    public static void stopAllVideos() {
        // Create a copy of the list to avoid ConcurrentModificationException
        List<QuestionCardController> controllersCopy;
        synchronized (allControllers) {
            controllersCopy = new ArrayList<>(allControllers);
        }
        for (QuestionCardController controller : controllersCopy) {
            controller.disposeVideo();
        }
    }

    public void updatePrivilegeUI(Utilisateur user) {
        commentAuthor.setText(user.getNickname());
        switch (user.getPrivilege() != null ? user.getPrivilege() : "regular") {
            case "top_contributor" -> {
                commentAuthor.setStyle("-fx-text-fill: silver;");
                crownIcon.setImage(new Image("/forumUI/icons/silver_crown.png"));
                crownIcon.setVisible(true);
            }
            case "top_fan" -> {
                commentAuthor.setStyle("-fx-text-fill: gold;");
                crownIcon.setImage(new Image("/forumUI/icons/crown.png"));
                crownIcon.setVisible(true);
            }
            default -> {
                commentAuthor.setStyle("-fx-text-fill: white;");
                crownIcon.setVisible(false);
            }
        }
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
                        if (img.isError()) System.err.println("Failed to cache image: " + file.getAbsolutePath());
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
                        System.err.println("Failed to load image: " + file.getAbsolutePath());
                        Platform.runLater(this::resetMediaState);
                    }
                } else if ("video".equals(mediaType)) {
                    // Validate video file format before creating Media
                    if (!isValidVideoFormat(file)) {
                        System.err.println("Invalid or unsupported video format: " + file.getAbsolutePath());
                        Platform.runLater(this::resetMediaState);
                        return;
                    }

                    Media media = new Media(fileUri);
                    mediaPlayer = mediaPlayerCache.computeIfAbsent(cacheKey, k -> {
                        MediaPlayer player = new MediaPlayer(media);
                        player.setAutoPlay(false);
                        player.setOnError(() -> {
                            System.err.println("MediaPlayer error for file " + file.getAbsolutePath() + ": " + player.getError().getMessage());
                            Platform.runLater(this::resetMediaState);
                        });
                        player.setOnReady(() -> {
                            double width = player.getMedia().getWidth();
                            double height = player.getMedia().getHeight();
                            if (width <= 0 || height <= 0) {
                                System.err.println("Invalid media dimensions (width or height is 0 or negative) for file: " + file.getAbsolutePath());
                                Platform.runLater(this::resetMediaState);
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
                Platform.runLater(this::resetMediaState);
            }
        });
    }

    private boolean isValidVideoFormat(File file) {
        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".mp4")) {
            return false; // Only support MP4 for now, extend as needed
        }
        return true; // Add more robust validation (e.g., using Apache Tika or FFmpeg) if necessary
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
                for (MediaPlayer otherPlayer : new ArrayList<>(activeMediaPlayers)) {
                    if (otherPlayer != mediaPlayer) {
                        otherPlayer.pause();
                        otherPlayer.setVolume(0.0);
                        otherPlayer.setMute(true);
                    }
                }
                activeMediaPlayers.clear();
                activeMediaPlayers.add(mediaPlayer);

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

            // Safely calculate video aspect ratio, handle zero or negative values
            double videoWidth = mediaPlayer.getMedia().getWidth();
            double videoHeight = mediaPlayer.getMedia().getHeight();
            double videoAspectRatio = (videoHeight > 0) ? (videoWidth / videoHeight) : 1.0; // Default to 1.0 if height is 0 or negative

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

        try {
            List<Image> emojis = EmojiService.fetchEmojis();
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
        } catch (Exception e) {
            System.err.println("Failed to load emojis: " + e.getMessage());
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
        }

        popup.getContent().add(scrollPane);
        popup.show(reactButton, reactButton.getScene().getWindow().getX() + reactButton.localToScene(0, 0).getX(),
                reactButton.getScene().getWindow().getY() + reactButton.localToScene(0, 0).getY() + reactButton.getHeight());
    }

    private void setGameIcon(String gameName) {
        Games game = gamesService.getByName(gameName);
        if (game != null && game.getImagePath() != null && !game.getImagePath().isEmpty()) {
            File file = new File(game.getImagePath());
            if (file.exists()) {
                String cacheKey = game.getImagePath();
                Image image = imageCache.computeIfAbsent(cacheKey, k -> new Image(file.toURI().toString(), 100, 100, true, true));
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