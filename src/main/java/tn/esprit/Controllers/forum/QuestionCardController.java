package tn.esprit.Controllers.forum;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;
import org.json.JSONObject;
import tn.esprit.Models.Question;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.json.JSONArray;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.EmojiService;
import tn.esprit.Services.QuestionService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class QuestionCardController {
    @FXML
    private Label commentAuthor;
    @FXML
    private Label titleLabel;
    @FXML
    private Label contentLabel;
    @FXML private HBox reactionContainer;

    @FXML
    private Label votesLabel;
    @FXML private Button reactButton;
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
    private Button emojiButton;
    @FXML
    private ImageView selectedEmojiImage;
    @FXML
    private Label selectedEmojiLabel;
    private Question question;
    private ForumController forumController;
    private int userId = SessionManager.getInstance().getUserId();
    private UtilisateurService us =new UtilisateurService();
    private QuestionService questionService = new QuestionService();

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
    }
   /* public void initialize() {
        emojiButton.setOnAction(event -> openEmojiPicker());
    }

    private void openEmojiPicker() {
        ContextMenu emojiMenu = new ContextMenu();
        try {
            JSONArray emojis = fetchEmojis();
            for (int i = 0; i < Math.min(10, emojis.length()); i++) {
                JSONObject emojiObject = emojis.getJSONObject(i);
                String emoji = emojiObject.getString("character"); // Fetch emoji symbol
                MenuItem emojiItem = new MenuItem(emoji);
                emojiItem.setOnAction(e -> selectedEmojiLabel.setText(emoji));
                emojiMenu.getItems().add(emojiItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        emojiMenu.show(emojiButton, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private JSONArray fetchEmojis() throws Exception {
        URL url = new URL("https://emoji-api.com/emojis?access_key=402cc29498a2c36ea7721761366fdfc7a6b20ffa");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        Scanner scanner = new Scanner(conn.getInputStream());
        String response = scanner.useDelimiter("\\A").next();
        scanner.close();

        return new JSONArray(response);
    }*/
   /*private void displayReactions() {
       reactionContainer.getChildren().clear();
       Map<String, Integer> reactions = question.getReactions();
       for (Map.Entry<String, Integer> entry : reactions.entrySet()) {
           Label reactionLabel = new Label(entry.getKey() + " " + entry.getValue());
           reactionLabel.getStyleClass().add("reaction-label"); // Use CSS class for styling
           reactionContainer.getChildren().add(reactionLabel);
       }
   }

    private void displayUserReaction() {
        // Use the userReaction field from the Question object
        String userReaction = question.getUserReaction();
        if (userReaction != null && !userReaction.isEmpty()) {
            selectedEmojiLabel.setText(userReaction);
        } else {
            selectedEmojiLabel.setText("");
        }
    }

    private void showEmojiPicker() {
        Popup popup = new Popup();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");

        VBox emojiBox = new VBox(8); // Tighter spacing for Facebook-like feel
        emojiBox.setPadding(new Insets(15));
        emojiBox.getStyleClass().add("emoji-picker"); // Use CSS class for styling

        try {
            List<Object> emojis = EmojiService.fetchEmojis();
            HBox row = new HBox(8);
            int emojiCount = 0;
            for (Object emoji : emojis) {
                if (emoji instanceof String) { // Unicode emoji
                    String unicodeEmoji = (String) emoji;
                    Button emojiButton = new Button(unicodeEmoji);
                    emojiButton.getStyleClass().add("emoji-button"); // Use CSS class for styling
                    emojiButton.setOnAction(e -> {
                        forumController.handleReaction(question, unicodeEmoji);
                        popup.hide();
                        displayReactions();
                        displayUserReaction(); // Update the selected emoji display
                    });

                    // Add animation for hover effect
                    ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), emojiButton);
                    scaleIn.setToX(1.1);
                    scaleIn.setToY(1.1);
                    ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), emojiButton);
                    scaleOut.setToX(1.0);
                    scaleOut.setToY(1.0);

                    emojiButton.setOnMouseEntered(e -> {
                        scaleIn.play();
                    });
                    emojiButton.setOnMouseExited(e -> {
                        scaleOut.play();
                    });

                    row.getChildren().add(emojiButton);
                } else if (emoji instanceof Image) { // Custom gaming emote
                    Image emoteImage = (Image) emoji;
                    ImageView imageView = new ImageView(emoteImage);
                    imageView.setFitWidth(40); // Smaller for social media compactness
                    imageView.setFitHeight(40);
                    imageView.setPreserveRatio(true);
                    imageView.setOnMouseClicked(e -> {
                        forumController.handleReaction(question, emoteImage.toString()); // Store as a string or path
                        popup.hide();
                        displayReactions();
                        displayUserReaction(); // Update the selected emoji display
                    });

                    // Add hover effect for image
                    imageView.setOnMouseEntered(e -> {
                        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), imageView);
                        scaleIn.setToX(1.1);
                        scaleIn.setToY(1.1);
                        scaleIn.play();
                    });
                    imageView.setOnMouseExited(e -> {
                        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), imageView);
                        scaleOut.setToX(1.0);
                        scaleOut.setToY(1.0);
                        scaleOut.play();
                    });

                    row.getChildren().add(imageView);
                }

                emojiCount++;
                if (emojiCount % 7 == 0) { // Create a new row every 7 emojis, like Facebook’s grid
                    emojiBox.getChildren().add(row);
                    row = new HBox(8);
                }
            }
            if (!row.getChildren().isEmpty()) {
                emojiBox.getChildren().add(row);
            }

            scrollPane.setContent(emojiBox);
            scrollPane.setPrefSize(350, 300); // Fixed size for Facebook-like picker, adjustable
        } catch (Exception e) {
            System.err.println("Failed to load emojis: " + e.getMessage());
            String[] fallbackEmojis = {"👍", "❤️", "😂", "😢", "😡", "😊", "😍"};
            HBox row = new HBox(8);
            int emojiCount = 0;
            for (String emoji : fallbackEmojis) {
                Button emojiButton = new Button(emoji);
                emojiButton.getStyleClass().add("emoji-button"); // Use CSS class for styling
                emojiButton.setOnAction(m -> {
                    forumController.handleReaction(question, emoji);
                    popup.hide();
                    displayReactions();
                    displayUserReaction(); // Update the selected emoji display
                });

                // Add animation for hover effect
                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), emojiButton);
                scaleIn.setToX(1.1);
                scaleIn.setToY(1.1);
                ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), emojiButton);
                scaleOut.setToX(1.0);
                scaleOut.setToY(1.0);

                emojiButton.setOnMouseEntered(m -> {
                    scaleIn.play();
                });
                emojiButton.setOnMouseExited(m -> {
                    scaleOut.play();
                });

                row.getChildren().add(emojiButton);
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
    }*/
   private void displayReactions() {
       reactionContainer.getChildren().clear();
       Map<String, Integer> reactions = question.getReactions();
       for (Map.Entry<String, Integer> entry : reactions.entrySet()) {
           String emojiUrl = entry.getKey();
           int count = entry.getValue();
           // Create an HBox to hold the emoji image and count label
           HBox reactionBox = new HBox(2); // Small spacing between icon and count
           reactionBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

           // Load the Twemoji image for this reaction
           if (emojiUrl.contains("twemoji")) {
               String hexcode = emojiUrl.substring(emojiUrl.lastIndexOf("/") + 1, emojiUrl.lastIndexOf(".")).replace("72x72_", "");
               Image emojiImage = new Image(emojiUrl, 32, 32, true, true); // 32x32 pixels to match selectedEmojiImage
               if (!emojiImage.isError()) {
                   ImageView emojiIcon = new ImageView(emojiImage);
                   emojiIcon.setFitWidth(32);
                   emojiIcon.setFitHeight(32);
                   emojiIcon.setPreserveRatio(true);
                   emojiIcon.getStyleClass().add("reaction-emoji-icon"); // Add style class for styling

                   // Add count label
                   Label countLabel = new Label(String.valueOf(count));
                   countLabel.getStyleClass().add("reaction-count-label"); // Add style class for styling

                   reactionBox.getChildren().addAll(emojiIcon, countLabel);
               } else {
                   System.err.println("Failed to load reaction emoji for URL: " + emojiUrl + " - " + emojiImage.getException());
                   // Fallback to text label if image fails
                   Label fallbackLabel = new Label(getEmojiNameFromUrl(emojiUrl) + " " + count);
                   fallbackLabel.getStyleClass().add("reaction-label");
                   reactionContainer.getChildren().add(fallbackLabel);
                   continue;
               }
           } else {
               // Fallback for non-Twemoji URLs (e.g., Unicode)
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
            // Map hexcode to a readable name (simplified mapping, expand as needed)
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
        return url; // Fallback for non-Twemoji URLs
    }

    private void displayUserReaction() {
        // Use the userReaction field from the Question object (emoji URL)
        String userReaction = question.getUserReaction();
        if (userReaction != null && !userReaction.isEmpty()) {
            if (userReaction.contains("twemoji")) {
                Image emojiImage = new Image(userReaction, 30, 30, true, true); // Reduced to 32x32 pixels
                if (!emojiImage.isError()) {
                    selectedEmojiImage.setImage(emojiImage);
                } else {
                    System.err.println("Failed to load selected emoji: " + userReaction + " - " + emojiImage.getException());
                    selectedEmojiImage.setImage(null); // Clear if image fails
                }
            } else {
                selectedEmojiImage.setImage(null); // Clear for non-Twemoji reactions (e.g., Unicode)
            }
        } else {
            selectedEmojiImage.setImage(null); // Clear if no reaction
        }
    }

    private void showEmojiPicker() {
        Popup popup = new Popup();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");

        VBox emojiBox = new VBox(8); // Tighter spacing for Facebook-like feel
        emojiBox.setPadding(new Insets(15));
        emojiBox.getStyleClass().add("emoji-picker"); // Use CSS class for styling

        try {
            List<Image> emojis = EmojiService.fetchEmojis(); // Now returns List<Image> from Twemoji
            HBox row = new HBox(8);
            int emojiCount = 0;
            for (Image emoji : emojis) {
                ImageView emojiImage = new ImageView(emoji);
                emojiImage.setFitWidth(30); // Match Facebook’s size (circular, 48x48 pixels) in picker
                emojiImage.setFitHeight(30);
                emojiImage.setPreserveRatio(true);
                emojiImage.setOnMouseClicked(e -> {
                    forumController.handleReaction(question, emoji.getUrl()); // Store as image URL
                    popup.hide();
                    displayReactions();
                    displayUserReaction(); // Update the selected emoji display
                });

                // Add hover effect for image
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
                if (emojiCount % 7 == 0) { // Create a new row every 7 emojis, like Facebook’s grid
                    emojiBox.getChildren().add(row);
                    row = new HBox(8);
                }
            }
            if (!row.getChildren().isEmpty()) {
                emojiBox.getChildren().add(row);
            }

            scrollPane.setContent(emojiBox);
            scrollPane.setPrefSize(250, 200); // Fixed size for Facebook-like picker, adjustable
        } catch (Exception e) {
            System.err.println("Failed to load emojis: " + e.getMessage());
            String[] fallbackPaths = {
                    "/forumUI/icons/like.png",
                    "/forumUI/icons/love.png",
                    "/forumUI/icons/haha.png",
                    "/forumUI/icons/wow.png",
                    "/forumUI/icons/sad.png",
                    "/forumUI/icons/angry.png",
                    "/forumUI/icons/applause.png", // Optional gaming-friendly fallback
                    "/forumUI/icons/fire.png",
                    "/forumUI/icons/100.png",
                    "/forumUI/icons/party.png",
                    "/forumUI/icons/ok.png",
                    "/forumUI/icons/blue_heart.png",
                    "/forumUI/icons/cool.png",
                    "/forumUI/icons/poop.png",
                    "/forumUI/icons/rocket.png",
                    "/forumUI/icons/trophy.png",
                    "/forumUI/icons/gift.png",
                    "/forumUI/icons/game.png",
                    "/forumUI/icons/die.png",
                    "/forumUI/icons/collision.png",
                    "/forumUI/icons/pray.png",
                    "/forumUI/icons/runner.png",
                    "/forumUI/icons/crown.png",
                    "/forumUI/icons/slots.png"
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
                    forumController.handleReaction(question, path); // Store as path
                    popup.hide();
                    displayReactions();
                    displayUserReaction(); // Update the selected emoji display
                });

                // Add hover effect for image
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

            System.out.println("Looking for game icon at: " + filePath); // Debugging line

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
