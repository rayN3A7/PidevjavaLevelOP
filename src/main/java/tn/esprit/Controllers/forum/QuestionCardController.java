package tn.esprit.Controllers.forum;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Popup;
import javafx.util.Duration;
import tn.esprit.Models.Games;
import tn.esprit.Models.Question;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.EmojiService;
import tn.esprit.Services.GamesService;
import tn.esprit.Services.QuestionService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
   // New play/pause button
    @FXML
    private HBox reactionContainer;

    private Question question;
    private ForumController forumController;
    private int userId = SessionManager.getInstance().getUserId();
    private UtilisateurService us = new UtilisateurService();
    private QuestionService questionService = new QuestionService();
    private GamesService gamesService = new GamesService();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private MediaPlayer mediaPlayer;
    private boolean isFullScreen = false;
    public void setQuestionData(Question question, ForumController forumController) {
        this.question = question;
        this.forumController = forumController;
        commentAuthor.setText(question.getUser().getNickname());
        titleLabel.setText(question.getTitle());
        titleLabel.setOnMouseClicked(event -> openQuestionDetails(question));
        titleLabel.setCursor(Cursor.HAND);

        contentLabel.setText(question.getContent());
        votesLabel.setText("Votes: " + question.getVotes());

        upvoteButton.setOnAction(e -> forumController.handleUpvote(question, votesLabel, downvoteButton));
        downvoteButton.setOnAction(e -> forumController.handleDownvote(question, votesLabel, downvoteButton));
        downvoteButton.setDisable(question.getVotes() == 0);

        updateButton.setOnAction(e -> forumController.updateQuestion(question));
        deleteButton.setOnAction(e -> forumController.deleteQuestion(question));
        reactButton.setOnAction(e -> showEmojiPicker());
        setGameIcon(question.getGame().getGame_name());
        displayReactions();
        displayUserReaction();

        loadQuestionMediaAsync();
        Utilisateur user = question.getUser();
        commentAuthor.setText(user.getNickname());
        switch (user.getPrivilege() != null ? user.getPrivilege() : "regular") {
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
    }

    private void loadQuestionMediaAsync() {
        String mediaPath = question.getMediaPath();
        String mediaType = question.getMediaType();
        if (mediaPath != null && !mediaPath.isEmpty() && mediaType != null) {
            executorService.submit(() -> {
                try {
                    File file = new File(mediaPath);
                    if (file.exists()) {
                        String fileUri = file.toURI().toString();
                        if ("image".equals(mediaType)) {
                            Image image = new Image(fileUri, 500, 350, true, true); // Match FXML sizes
                            if (!image.isError()) {
                                Platform.runLater(() -> {
                                    questionImage.setImage(image);
                                    questionImage.setVisible(true);
                                    questionImage.setManaged(true);
                                    videoWrapper.setVisible(false); // Hide video components
                                    videoWrapper.setManaged(false);
                                    mediaContainer.setVisible(true);
                                    mediaContainer.setManaged(true);
                                    if (!contentVBox.getParent().getStyleClass().contains("has-image")) {
                                        contentVBox.getParent().getStyleClass().add("has-image");
                                    }
                                });
                            } else {
                                System.err.println("Failed to load image: " + mediaPath);
                                Platform.runLater(() -> resetMediaState());
                            }
                        } else if ("video".equals(mediaType)) {
                            Media media = new Media(fileUri);
                            mediaPlayer = new MediaPlayer(media);
                            mediaPlayer.setAutoPlay(false);
                            Platform.runLater(() -> {
                                questionVideo.setMediaPlayer(mediaPlayer);
                                videoWrapper.setVisible(true);
                                videoWrapper.setManaged(true);
                                questionImage.setVisible(false); // Hide image
                                questionImage.setManaged(false);
                                mediaContainer.setVisible(true);
                                mediaContainer.setManaged(true);
                                setupVideoControls();
                                videoControlBar.prefWidthProperty().bind(questionVideo.fitWidthProperty());
                            });
                        }
                    } else {
                        System.err.println("Media file not found: " + mediaPath);
                        Platform.runLater(() -> resetMediaState());
                    }
                } catch (Exception e) {
                    System.err.println("Error loading question media: " + e.getMessage());
                    Platform.runLater(() -> resetMediaState());
                }
            });
        } else {
            Platform.runLater(() -> resetMediaState());
        }
    }
    private void setupVideoControls() {
        playPauseButton.setOnAction(event -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playPauseButton.setText("▶");
            } else {
                mediaPlayer.play();
                playPauseButton.setText("⏸");
            }
        });

        mediaPlayer.currentTimeProperty().addListener((obs, old, newValue) -> {
            if (!progressSlider.isValueChanging()) {
                double duration = mediaPlayer.getTotalDuration().toSeconds();
                double current = newValue.toSeconds();
                progressSlider.setValue(current / duration * 100);
                updateTimeLabel(current, duration);
            }
        });
        progressSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                double duration = mediaPlayer.getTotalDuration().toSeconds();
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue() * duration / 100));
            }
        });
        progressSlider.setOnMouseDragged(event -> {
            double duration = mediaPlayer.getTotalDuration().toSeconds();
            mediaPlayer.seek(Duration.seconds(progressSlider.getValue() * duration / 100));
        });

        volumeSlider.valueProperty().addListener((obs, old, newValue) -> {
            mediaPlayer.setVolume(newValue.doubleValue() / 100.0);
        });

        fullScreenButton.setOnAction(event -> toggleFullScreen());

        videoControlBar.setOpacity(1.0);
        FadeTransition fade = new FadeTransition(Duration.seconds(0.3), videoControlBar);
        fade.setToValue(0.0);
        videoWrapper.setOnMouseMoved(event -> {
            videoControlBar.setOpacity(1.0);
            fade.stop();
        });
        videoWrapper.setOnMouseExited(event -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                fade.playFromStart();
            }
        });

        mediaPlayer.setOnReady(() -> {
            progressSlider.setMax(100);
            updateTimeLabel(0, mediaPlayer.getTotalDuration().toSeconds());
        });
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
            stage.setFullScreen(true);
            questionVideo.fitWidthProperty().unbind();
            questionVideo.fitHeightProperty().unbind();
            questionVideo.setFitWidth(stage.getWidth());
            questionVideo.setFitHeight(stage.getHeight() - videoControlBar.getHeight());
            videoControlBar.prefWidthProperty().bind(stage.widthProperty());
            fullScreenButton.setText("⤹");
            isFullScreen = true;
        } else {
            stage.setFullScreen(false);
            questionVideo.setFitWidth(500);
            questionVideo.setFitHeight(350);
            videoControlBar.prefWidthProperty().bind(questionVideo.fitWidthProperty());
            fullScreenButton.setText("⛶");
            isFullScreen = false;
        }
    }

    private void resetMediaState() {
        questionImage.setImage(null);
        questionImage.setVisible(false);
        questionImage.setManaged(false);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        questionVideo.setMediaPlayer(null);
        videoWrapper.setVisible(false);
        videoWrapper.setManaged(false);
        mediaContainer.setVisible(false);
        mediaContainer.setManaged(false);
        videoControlBar.prefWidthProperty().unbind();
        contentVBox.getParent().getStyleClass().remove("has-image");
    }

    private void displayReactions() {
        reactionContainer.getChildren().clear();
        Map<String, Integer> reactions = question.getReactions();
        for (Map.Entry<String, Integer> entry : reactions.entrySet()) {
            String emojiUrl = entry.getKey();
            int count = entry.getValue();
            HBox reactionBox = new HBox(2);
            reactionBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            if (emojiUrl.contains("twemoji")) {
                Image emojiImage = new Image(emojiUrl, 32, 32, true, true);
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

    private void displayUserReaction() {
        String userReaction = question.getUserReaction();
        if (userReaction != null && !userReaction.isEmpty()) {
            if (userReaction.contains("twemoji")) {
                Image emojiImage = new Image(userReaction, 30, 30, true, true);
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
                Image fallbackImage = new Image(getClass().getResourceAsStream(path), 30, 30, true, true);
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
        Games game = gamesService.getByName(gameName); // Assuming gamesService is initialized
        if (game != null && game.getImagePath() != null && !game.getImagePath().isEmpty()) {
            File file = new File(game.getImagePath());
            if (file.exists()) {
                Image image = new Image(file.toURI().toString(), 100, 100, true, true);
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