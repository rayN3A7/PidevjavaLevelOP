package tn.esprit.Controllers.forum;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.stage.Popup;
import javafx.util.Duration;
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
import tn.esprit.Services.EmojiService;
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
    @FXML
    private Label commentAuthor;
    @FXML
    private Label titleLabel;
    @FXML
    private Label contentLabel;
    @FXML
    private ImageView questionImage; // Ensure this field exists
    @FXML
    private HBox reactionContainer;

    @FXML
    private Label votesLabel;
    @FXML
    private Button reactButton;
    @FXML
    private Button upvoteButton;
    @FXML
    private Button downvoteButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private ImageView gameIcon;
    @FXML
    private VBox contentVBox;
    @FXML
    private ImageView selectedEmojiImage; // Updated to ImageView for graphical emoji
    @FXML
    private Label selectedEmojiLabel; // Ensure this is included if used
    private Question question;
    private ForumController forumController;
    private int userId = SessionManager.getInstance().getUserId();
    private UtilisateurService us = new UtilisateurService();
    private QuestionService questionService = new QuestionService();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
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

        // Load image and adjust card size dynamically
        loadQuestionImageAsync();
    }

    private void loadQuestionImageAsync() {
        String imagePath = question.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            executorService.submit(() -> {
                try {
                    System.out.println("Attempting to load image from path (async): " + imagePath);
                    File file = new File(imagePath);
                    if (file.exists()) {
                        String fileUri = file.toURI().toString();
                        Image image = new Image(fileUri, 200, 150, true, true);
                        if (!image.isError()) {
                            Platform.runLater(() -> {
                                questionImage.setImage(image);
                                questionImage.setVisible(true);
                                questionImage.setManaged(true);
                                // Apply the has-image class to the question-card HBox
                                if (!contentVBox.getParent().getStyleClass().contains("has-image")) {
                                    contentVBox.getParent().getStyleClass().add("has-image");
                                }
                                System.out.println("Successfully loaded image (async): " + imagePath);
                            });
                        } else {
                            System.err.println("Failed to load image (async): " + imagePath + " - " + image.getException());
                            Platform.runLater(() -> resetImageState());
                        }
                    } else {
                        System.err.println("Image file not found (async): " + imagePath);
                        Platform.runLater(() -> resetImageState());
                    }
                } catch (Exception e) {
                    System.err.println("Error loading question image (async): " + e.getMessage());
                    Platform.runLater(() -> resetImageState());
                }
            });
        } else {
            System.out.println("No image path provided for question (async): " + question.getQuestion_id());
            Platform.runLater(() -> resetImageState());
        }
    }

    private void resetImageState() {
        questionImage.setImage(null);
        questionImage.setVisible(false);
        questionImage.setManaged(false);
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
        if (gameName != null && !gameName.isEmpty()) {
            String formattedGameName = gameName.toLowerCase().replace(" ", "_");
            String filePath = "/forumUI/icons/" + formattedGameName + ".png";

            System.out.println("Looking for game icon at: " + filePath);

            try {
                Image image = new Image(getClass().getResourceAsStream(filePath));
                if (image.isError()) {
                    throw new Exception("Image load error");
                }
                gameIcon.setImage(image);

                switch (formattedGameName) {
                    case "valorant":
                        gameIcon.setFitWidth(100);
                        gameIcon.setFitHeight(80);
                        break;
                    case "overwatch":
                        gameIcon.setFitWidth(100);
                        gameIcon.setFitHeight(100);
                        break;
                    case "league_of_legends":
                        gameIcon.setFitWidth(100);
                        gameIcon.setFitHeight(100);
                        break;
                    default:
                        gameIcon.setFitWidth(100);
                        gameIcon.setFitHeight(100);
                        break;
                }
            } catch (Exception e) {
                System.out.println("Game icon not found for: " + gameName + " at path: " + filePath);
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