package tn.esprit.Controllers.forum;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Models.Commentaire;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.CommentaireService;
import tn.esprit.Services.EmojiService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.ProfanityChecker;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommentCardController {

    @FXML
    private Label commentAuthor;
    @FXML
    private Label commentContent;
    @FXML
    private Button upvoteButton;
    @FXML
    private Label votesLabel;
    @FXML
    private TextField editCommentField;
    @FXML
    private Label userLabel;
    @FXML
    private ImageView crownIcon;
    @FXML
    private Button downvoteButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button updateButton;
    @FXML
    private HBox editButtonsBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button reactButton;
    @FXML
    private HBox reactionContainer;
    @FXML
    private ImageView selectedEmojiImage;

    private Commentaire commentaire;
    private QuestionDetailsController questionDetailsController;
    private CommentaireService commentaireService = new CommentaireService();
    private int userId = SessionManager.getInstance().getUserId();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private static final Map<String, Image> imageCache = new HashMap<>();
    private UtilisateurService us = new UtilisateurService(); // Added for role checking

    public void setCommentData(Commentaire commentaire, QuestionDetailsController questionDetailsController) {
        this.questionDetailsController = questionDetailsController;
        this.commentaire = commentaire;
        commentAuthor.getParent().setUserData(this);
        commentAuthor.setText(commentaire.getUtilisateur().getNickname());
        commentContent.setText(commentaire.getContenu());

        upvoteButton.setOnAction(e -> questionDetailsController.handleUpvoteC(commentaire, votesLabel, downvoteButton));
        downvoteButton.setOnAction(e -> questionDetailsController.handleDownvoteC(commentaire, votesLabel, downvoteButton));

        editCommentField.setVisible(false);
        editButtonsBox.setVisible(false);

        // Set up update and delete actions with role-based permission checks
        updateButton.setOnAction(event -> handleUpdateComment());
        deleteButton.setOnAction(e -> handleDeleteComment());

        // Determine visibility based on user role and ownership
        Utilisateur currentUser = us.getOne(userId);
        if (currentUser == null) {
            updateButton.setVisible(false);
            deleteButton.setVisible(false);
        } else {
            boolean isOwner = commentaire.getUtilisateur().getId() == userId;
            if (currentUser.getRole() == Role.ADMIN) {
                // Admin can update/delete any comment
                updateButton.setVisible(true);
                deleteButton.setVisible(true);
            } else {
                // Client/Coach can only update/delete their own comments
                updateButton.setVisible(isOwner);
                deleteButton.setVisible(isOwner);
            }
        }

        saveButton.setOnAction(event -> saveUpdatedComment());
        reactButton.setOnAction(e -> showEmojiPicker());
        displayReactions();
        displayUserReaction();
        updatePrivilegeUI(commentaire.getUtilisateur());
    }

    private void handleUpdateComment() {
        Utilisateur currentUser = us.getOne(userId);
        if (currentUser == null) {
            showAlert("Erreur", "Utilisateur non trouvé.");
            return;
        }

        if (currentUser.getRole() != Role.ADMIN && commentaire.getUtilisateur().getId() != userId) {
            showAlert("Erreur", "Vous ne pouvez modifier que vos propres commentaires.");
            return;
        }

        enableEditMode();
    }

    private void handleDeleteComment() {
        Utilisateur currentUser = us.getOne(userId);
        if (currentUser == null) {
            showAlert("Erreur", "Utilisateur non trouvé.");
            return;
        }

        if (currentUser.getRole() != Role.ADMIN && commentaire.getUtilisateur().getId() != userId) {
            showAlert("Erreur", "Vous ne pouvez supprimer que vos propres commentaires.");
            return;
        }

        questionDetailsController.deleteComment(commentaire);
    }

    public void updatePrivilegeUI(Utilisateur user) {
        TextFlow authorFlow = new TextFlow();

        if (user.getRole() == Role.ADMIN) {
            Text adminText = new Text("Admin ");
            adminText.setStyle("-fx-fill: #009dff;");
            Text usernameText = new Text(user.getNickname());
            usernameText.setStyle("-fx-fill: white;");
            authorFlow.getChildren().addAll(adminText, usernameText);
        } else {
            Text usernameText = new Text(user.getNickname());
            usernameText.setStyle("-fx-fill: white;");
            authorFlow.getChildren().add(usernameText);
        }

        switch (user.getPrivilege() != null ? user.getPrivilege() : "regular") {
            case "top_contributor" -> {
                authorFlow.setStyle("-fx-text-fill: silver;");
                crownIcon.setImage(new Image("/forumUI/icons/silver_crown.png"));
                crownIcon.setVisible(true);
            }
            case "top_fan" -> {
                authorFlow.setStyle("-fx-text-fill: gold;");
                crownIcon.setImage(new Image("/forumUI/icons/crown.png"));
                crownIcon.setVisible(true);
            }
            default -> {
                if (user.getRole() != Role.ADMIN) {
                    authorFlow.setStyle("-fx-text-fill: white;");
                }
                crownIcon.setVisible(false);
            }
        }

        commentAuthor.setGraphic(authorFlow);
        commentAuthor.setText("");
    }

    private void enableEditMode() {
        editCommentField.setText(commentaire.getContenu());
        editCommentField.setVisible(true);
        editButtonsBox.setVisible(true);
        commentContent.setVisible(false);
        updateButton.setVisible(false);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(new Image(getClass().getResource("/forumUI/icons/alert.png").toExternalForm()));
        icon.setFitHeight(60);
        icon.setFitWidth(60);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);
        alert.showAndWait();
    }

    private void saveUpdatedComment() {
        String updatedContent = editCommentField.getText().trim();
        if (updatedContent.isEmpty()) return;

        executorService.submit(() -> {
            try {
                if (ProfanityChecker.containsProfanity(updatedContent)) {
                    final boolean[] proceed = {false}; // Use an array to update from within lambda
                    Platform.runLater(() -> {
                        proceed[0] = showProfanityWarningAlert("Avertissement",
                                "Votre commentaire contient des mots inappropriés. Vous risquez d'être banni ou signalé. Voulez-vous ajouter quand même?");
                    });
                    // Wait for the UI thread to complete (simple polling, could be improved with a semaphore or callback)
                    while (!Thread.currentThread().isInterrupted() && !proceed[0] && !Platform.isFxApplicationThread()) {
                        Thread.yield(); // Avoid busy-waiting, but this is a simple solution
                    }
                    if (!proceed[0]) return;
                }

                Utilisateur currentUser = us.getOne(userId);
                if (currentUser == null) {
                    Platform.runLater(() -> showAlert("Erreur", "Utilisateur non trouvé."));
                    return;
                }

                if (currentUser.getRole() != Role.ADMIN && commentaire.getUtilisateur().getId() != userId) {
                    Platform.runLater(() -> showAlert("Erreur", "Vous ne pouvez modifier que vos propres commentaires."));
                    return;
                }

                commentaire.setContenu(updatedContent);
                commentaireService.update(commentaire, userId);
                Platform.runLater(() -> {
                    commentContent.setText(updatedContent);
                    commentContent.setVisible(true);
                    updateButton.setVisible(true);
                    editCommentField.setVisible(false);
                    editButtonsBox.setVisible(false);
                });
            } catch (IOException e) {
                Platform.runLater(() -> showProfanityWarningAlert("Erreur Réseau", "Impossible de vérifier le contenu pour le moment. Veuillez réessayer plus tard. Détails: " + e.getMessage()));
            } catch (SecurityException e) {
                Platform.runLater(() -> showAlert("Erreur", e.getMessage()));
            }
        });
    }

    private boolean showProfanityWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(new Image(getClass().getResource("/forumUI/icons/alert.png").toExternalForm()));
        icon.setFitHeight(60);
        icon.setFitWidth(60);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType addAnywayButton = new ButtonType("Ajouter quand même", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton, addAnywayButton);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/alert.png").toString()));

        return alert.showAndWait()
                .filter(response -> response == addAnywayButton)
                .isPresent();
    }

    public void displayReactions() {
        reactionContainer.getChildren().clear();
        Map<String, Integer> reactions = commentaire.getReactions();
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
        String userReaction = commentaire.getUserReaction();
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
                            handleReaction(emoji.getUrl());
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
                            handleReaction(path);
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
                });
            }
        });
    }

    private void handleReaction(String emojiUrl) {
        executorService.submit(() -> {
            int userId = SessionManager.getInstance().getUserId();
            CommentaireService service = new CommentaireService();

            String existingReaction = service.getUserReaction(commentaire.getCommentaire_id(), userId);
            if (existingReaction != null) {
                service.removeReaction(commentaire.getCommentaire_id(), userId);
                commentaire.getReactions().remove(existingReaction);
                if (commentaire.getReactions().containsKey(existingReaction)) {
                    int currentCount = commentaire.getReactions().get(existingReaction);
                    if (currentCount > 1) {
                        commentaire.getReactions().put(existingReaction, currentCount - 1);
                    } else {
                        commentaire.getReactions().remove(existingReaction);
                    }
                }
            }

            service.addReaction(commentaire.getCommentaire_id(), userId, emojiUrl);
            Map<String, Integer> updatedReactions = service.getReactions(commentaire.getCommentaire_id());
            commentaire.setReactions(updatedReactions);
            commentaire.setUserReaction(emojiUrl);
            Platform.runLater(() -> {
                displayReactions();
                displayUserReaction();
            });
        });
    }

    public Commentaire getCommentaire() {
        return commentaire;
    }
}