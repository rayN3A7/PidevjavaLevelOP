package tn.esprit.Controllers.forum;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Models.*;
import tn.esprit.Services.CommentaireService;
import tn.esprit.Services.EmojiService;
import tn.esprit.Services.ReportService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.EventBus;
import tn.esprit.utils.PrivilegeEvent;
import tn.esprit.utils.ProfanityChecker;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommentCardController {

    @FXML private Label commentAuthor;
    @FXML private Label commentContent;
    @FXML private Button upvoteButton;
    @FXML private Label votesLabel;
    @FXML private TextField editCommentField;
    @FXML private ImageView crownIcon;
    @FXML private Button downvoteButton;
    @FXML private Button deleteButton;
    @FXML private Button updateButton;
    @FXML private HBox editButtonsBox;
    @FXML private Button saveButton;
    @FXML private Button reactButton;
    @FXML private HBox reactionContainer;
    @FXML private ImageView selectedEmojiImage;
    @FXML private Button replyButton;
    @FXML private VBox replyInputBox;
    @FXML private TextField replyInput;
    @FXML private Button submitReplyButton;
    @FXML private Button cancelReplyButton;
    @FXML private Button toggleRepliesButton;
    @FXML private VBox repliesContainer;
    @FXML private Button reportButton;
    private ReportService reportService = new ReportService();
    private Commentaire commentaire;
    private QuestionDetailsController questionDetailsController;
    private CommentaireService commentaireService = new CommentaireService();
    private int userId = SessionManager.getInstance().getUserId();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private static final Map<String, Image> imageCache = new HashMap<>();
    private UtilisateurService us = new UtilisateurService();
    private boolean repliesLoaded = false;
    private Popup emojiPopup;

    public void setCommentData(Commentaire commentaire, QuestionDetailsController questionDetailsController) {
        this.questionDetailsController = questionDetailsController;
        this.commentaire = commentaire;
        commentAuthor.getParent().setUserData(this);
        commentAuthor.setText(commentaire.getUtilisateur().getNickname());
        commentContent.setText(commentaire.getContenu());
        votesLabel.setText("Votes: " + commentaire.getVotes());

        upvoteButton.setOnAction(e -> handleUpvote());
        downvoteButton.setOnAction(e -> handleDownvote());
        deleteButton.setOnAction(e -> handleDeleteComment());
        updateButton.setOnAction(e -> handleUpdateComment());
        saveButton.setOnAction(e -> saveUpdatedComment());
        reactButton.setOnAction(e -> showEmojiPicker());
        replyButton.setOnAction(e -> toggleReplyInput());
        submitReplyButton.setOnAction(e -> submitReply());
        cancelReplyButton.setOnAction(e -> cancelReply());
        toggleRepliesButton.setOnAction(e -> toggleReplies());
        reportButton.setOnAction(e -> showReportForm(commentaire.getUtilisateur().getId(), commentaire.getContenu()));
        configureVisibility();
        displayReactions();
        displayUserReaction();
        updatePrivilegeUI(commentaire.getUtilisateur());
        EventBus.getInstance().addHandler(event -> {
            if (event.getUserId() == commentaire.getUtilisateur().getId()) {
                Utilisateur user = us.getOne(event.getUserId());
                if (user != null) {
                    updatePrivilegeUI(user);
                }
            }
        });

        checkForReplies();
        commentAuthor.getParent().addEventHandler(PrivilegeEvent.PRIVILEGE_CHANGED, event -> {
            if (event.getUserId() == commentaire.getUtilisateur().getId()) {
                Utilisateur user = us.getOne(event.getUserId());
                if (user != null) {
                    updatePrivilegeUI(user);
                    if (event.getUserId() == userId) {
                        UtilisateurService.PrivilegeChange change = new UtilisateurService.PrivilegeChange(
                                user.getPrivilege(), event.getNewPrivilege());
                        showPrivilegeAlert(change);
                    }
                }
            }
        });
    }

    private void configureVisibility() {
        editCommentField.setVisible(false);
        editButtonsBox.setVisible(false);
        editButtonsBox.setManaged(false);
        replyInputBox.setVisible(false);
        replyInputBox.setManaged(false);
        repliesContainer.setVisible(false);
        repliesContainer.setManaged(false);
        toggleRepliesButton.setVisible(false);
        toggleRepliesButton.setManaged(false);

        Utilisateur currentUser = us.getOne(userId);
        if (currentUser == null) {
            updateButton.setVisible(false);
            deleteButton.setVisible(false);
            replyButton.setVisible(false);
        } else {
            boolean isOwner = commentaire.getUtilisateur().getId() == userId;
            updateButton.setVisible(currentUser.getRole() == Role.ADMIN || isOwner);
            deleteButton.setVisible(currentUser.getRole() == Role.ADMIN || isOwner);
            replyButton.setVisible(true);
        }
    }

    private void showReportForm(int reportedUserId, String evidence) {
        Stage reportStage = new Stage();
        reportStage.setTitle("Report Comment");

        VBox reportForm = new VBox(10);
        reportForm.setPadding(new Insets(10));
        reportForm.setStyle("-fx-background-color: #2e2e2e; -fx-border-color: #666; -fx-border-width: 1;");

        Label title = new Label("Report this comment");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        ComboBox<ReportReason> reasonComboBox = new ComboBox<>();
        reasonComboBox.getItems().addAll(ReportReason.values());
        reasonComboBox.setPromptText("Select a reason");
        reasonComboBox.setStyle("-fx-background-color: #444; -fx-text-fill: white;");

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

    private void handleUpdateComment() {
        Utilisateur currentUser = us.getOne(userId);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && commentaire.getUtilisateur().getId() != userId)) {
            showAlert("Erreur", "Vous ne pouvez modifier que vos propres commentaires.");
            return;
        }
        editCommentField.setText(commentaire.getContenu());
        editCommentField.setVisible(true);
        editButtonsBox.setVisible(true);
        editButtonsBox.setManaged(true);
        commentContent.setVisible(false);
        updateButton.setVisible(false);
    }

    private void handleDeleteComment() {
        Utilisateur currentUser = us.getOne(userId);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && commentaire.getUtilisateur().getId() != userId)) {
            showAlert("Erreur", "Vous ne pouvez supprimer que vos propres commentaires.");
            return;
        }
        questionDetailsController.deleteComment(commentaire);
    }

    private void saveUpdatedComment() {
        String updatedContent = editCommentField.getText().trim();
        if (updatedContent.isEmpty()) return;
        executorService.submit(() -> {
            try {
                if (ProfanityChecker.containsProfanity(updatedContent)) {
                    final boolean[] proceed = {false};
                    Platform.runLater(() -> proceed[0] = showProfanityWarningAlert("Avertissement", "Contenu inapproprié. Continuer?"));
                    while (!proceed[0] && !Thread.currentThread().isInterrupted()) Thread.yield();
                    if (!proceed[0]) return;
                }
                commentaire.setContenu(updatedContent);
                commentaireService.update(commentaire, userId);
                Platform.runLater(() -> {
                    commentContent.setText(updatedContent);
                    commentContent.setVisible(true);
                    updateButton.setVisible(true);
                    editCommentField.setVisible(false);
                    editButtonsBox.setVisible(false);
                    editButtonsBox.setManaged(false);
                    checkPrivilegeChange(userId);
                    checkPrivilegeChange(commentaire.getUtilisateur().getId());
                });
            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Erreur", "Erreur réseau: " + e.getMessage()));
            } catch (SecurityException e) {
                Platform.runLater(() -> showAlert("Erreur", e.getMessage()));
            }
        });
    }

    private void handleUpvote() {
        executorService.submit(() -> {
            try {
                String currentVote = commentaireService.getUserVote(commentaire.getCommentaire_id(), userId);
                if ("UP".equals(currentVote)) {
                    Platform.runLater(() -> showAlert("Erreur", "Vous avez déjà upvoté ce commentaire."));
                    return;
                }

                commentaireService.upvoteComment(commentaire.getCommentaire_id(), userId);
                int updatedVotes = commentaireService.getVotes(commentaire.getCommentaire_id());
                commentaire.setVotes(updatedVotes);

                Platform.runLater(() -> {
                    votesLabel.setText("Votes: " + updatedVotes);
                    votesLabel.setVisible(true);
                    downvoteButton.setDisable(updatedVotes == 0);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Erreur", "Erreur lors de l'upvote: " + e.getMessage()));
            }
        });
    }

    private void handleDownvote() {
        executorService.submit(() -> {
            try {
                String currentVote = commentaireService.getUserVote(commentaire.getCommentaire_id(), userId);
                if ("DOWN".equals(currentVote)) {
                    Platform.runLater(() -> showAlert("Erreur", "Vous avez déjà downvoté ce commentaire."));
                    return;
                }

                commentaireService.downvoteComment(commentaire.getCommentaire_id(), userId);
                int updatedVotes = commentaireService.getVotes(commentaire.getCommentaire_id());
                commentaire.setVotes(updatedVotes);

                Platform.runLater(() -> {
                    votesLabel.setText("Votes: " + updatedVotes);
                    votesLabel.setVisible(true);
                    downvoteButton.setDisable(updatedVotes == 0);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Erreur", "Erreur lors du downvote: " + e.getMessage()));
            }
        });
    }

    private void checkPrivilegeChange(int affectedUserId) {
        UtilisateurService.PrivilegeChange change = us.updateUserPrivilege(affectedUserId);
        if (change.isChanged()) {
            Utilisateur updatedUser = us.getOne(affectedUserId);
            if (updatedUser != null) {
                if (commentaire.getUtilisateur().getId() == affectedUserId) {
                    updatePrivilegeUI(updatedUser);
                }
                questionDetailsController.updatePrivilegeUI(affectedUserId);
                if (affectedUserId == userId) {
                    showPrivilegeAlert(change);
                }
            }
        }
    }

    public void updatePrivilegeUI(Utilisateur user) {
        // Removed Platform.runLater as this is typically called from FX thread or after background task
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

        String privilege = user.getPrivilege() != null ? user.getPrivilege() : "regular";
        System.out.println("Updating privilege UI for user " + user.getNickname() + " to " + privilege);
        commentAuthor.setText(user.getNickname());
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

    private void showPrivilegeAlert(UtilisateurService.PrivilegeChange change) {
        if (!change.isChanged()) return;

        // Removed Platform.runLater as this is typically called from FX thread
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setHeaderText(null);
        String oldPrivilege = change.getOldPrivilege();
        String newPrivilege = change.getNewPrivilege();
        boolean isPromotion = getPrivilegeRank(newPrivilege) > getPrivilegeRank(oldPrivilege);

        alert.setTitle(isPromotion ? "Félicitations!" : "Mise à jour de privilège");
        alert.setContentText(isPromotion ?
                (newPrivilege.equals("top_contributor") ? "Vous êtes passé de Regular à Top Contributor ! Bravo pour votre contribution !" :
                        "Vous êtes maintenant un Top Fan depuis " + oldPrivilege + " ! Votre passion est récompensée !") :
                (oldPrivilege.equals("top_contributor") ? "Désolé, vous êtes redescendu de Top Contributor à Regular." :
                        "Désolé, vous êtes passé de Top Fan à " + newPrivilege + "."));

        ImageView icon = new ImageView(new Image(getClass().getResource(
                isPromotion ? (newPrivilege.equals("top_contributor") ? "/forumUI/icons/silver_crown.png" : "/forumUI/icons/crown.png") :
                        "/forumUI/icons/alert.png").toExternalForm()));
        icon.setFitHeight(60);
        icon.setFitWidth(60);
        alert.setGraphic(icon);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource(isPromotion ? "/forumUI/icons/sucessalert.png" : "/forumUI/icons/alert.png").toString()));

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("privilege-alert");

        ButtonType okButton = new ButtonType(isPromotion ? "GG!" : "OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), alert.getDialogPane());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        alert.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) fadeIn.play();
        });

        alert.showAndWait();
    }

    private void toggleReplyInput() {
        boolean isVisible = !replyInputBox.isVisible();
        replyInputBox.setVisible(isVisible);
        replyInputBox.setManaged(isVisible);
        if (isVisible) replyInput.requestFocus();
        else replyInput.clear();
    }

    private void submitReply() {
        String replyText = replyInput.getText().trim();
        if (replyText.isEmpty()) return;

        executorService.submit(() -> {
            try {
                if (ProfanityChecker.containsProfanity(replyText)) {
                    final boolean[] proceed = {false};
                    Platform.runLater(() -> proceed[0] = showProfanityWarningAlert("Avertissement", "Contenu inapproprié pourrait être signalée. Continuer?"));
                    while (!proceed[0] && !Thread.currentThread().isInterrupted()) Thread.yield();
                    if (!proceed[0]) return;
                }
                Utilisateur user = us.getOne(userId);
                if (user == null) return;

                Commentaire reply = new Commentaire();
                reply.setContenu("@" + commentaire.getUtilisateur().getNickname() + " " + replyText);
                reply.setUtilisateur(user);
                reply.setCreation_at(new java.sql.Timestamp(System.currentTimeMillis()));
                reply.setParent_commentaire_id(commentaire);
                reply.setQuestion(commentaire.getQuestion());
                commentaireService.add(reply);

                Platform.runLater(() -> {
                    replyInput.clear();
                    replyInputBox.setVisible(false);
                    replyInputBox.setManaged(false);
                    loadReplies();
                    toggleRepliesButton.setVisible(true);
                    toggleRepliesButton.setManaged(true);
                    checkPrivilegeChange(userId);
                    checkPrivilegeChange(commentaire.getUtilisateur().getId());
                });
            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Erreur", "Erreur réseau: " + e.getMessage()));
            }
        });
    }

    private void cancelReply() {
        replyInput.clear();
        replyInputBox.setVisible(false);
        replyInputBox.setManaged(false);
    }

    private void toggleReplies() {
        if (!repliesLoaded) {
            loadReplies();
            repliesLoaded = true;
        }
        boolean isVisible = !repliesContainer.isVisible();
        repliesContainer.setVisible(isVisible);
        repliesContainer.setManaged(isVisible);
        toggleRepliesButton.setText(isVisible ? "Hide Replies" : "Show Replies");
    }

    private void loadReplies() {
        executorService.submit(() -> {
            List<Commentaire> replies = commentaireService.getReplies(commentaire.getCommentaire_id());
            Platform.runLater(() -> {
                repliesContainer.getChildren().clear();
                for (Commentaire reply : replies) {
                    VBox replyCard = createReplyCard(reply);
                    repliesContainer.getChildren().add(replyCard);
                }
                toggleRepliesButton.setVisible(!replies.isEmpty());
                toggleRepliesButton.setManaged(!replies.isEmpty());
            });
        });
    }

    private VBox createReplyCard(Commentaire reply) {
        VBox replyCard = new VBox(5);
        replyCard.getStyleClass().add("comment-card");
        replyCard.setPadding(new javafx.geometry.Insets(5));
        replyCard.setUserData(this);

        Label authorLabel = new Label(reply.getUtilisateur().getNickname());
        authorLabel.getStyleClass().add("comment-author");

        Label contentLabel = new Label(reply.getContenu());
        contentLabel.getStyleClass().add("comment-content");
        contentLabel.setWrapText(true);

        HBox actions = new HBox(5);
        actions.setAlignment(Pos.CENTER_LEFT);
        Button deleteReplyButton = new Button("Delete");
        deleteReplyButton.getStyleClass().add("update-delete-button");
        deleteReplyButton.setVisible(us.getOne(userId) != null && (us.getOne(userId).getRole() == Role.ADMIN || reply.getUtilisateur().getId() == userId));
        deleteReplyButton.setOnAction(e -> deleteReply(reply));

        actions.getChildren().add(deleteReplyButton);
        replyCard.getChildren().addAll(authorLabel, contentLabel, actions);

        replyCard.setOnMouseClicked(event -> event.consume());

        return replyCard;
    }

    private void deleteReply(Commentaire reply) {
        executorService.submit(() -> {
            try {
                commentaireService.delete(reply.getCommentaire_id(), userId);
                Platform.runLater(() -> {
                    repliesContainer.getChildren().removeIf(node -> {
                        CommentCardController controller = (CommentCardController) node.getUserData();
                        return controller != null && controller.getCommentaire().getCommentaire_id() == reply.getCommentaire_id();
                    });
                    checkForReplies();
                    checkPrivilegeChange(userId);
                    checkPrivilegeChange(reply.getUtilisateur().getId());
                });
            } catch (SecurityException e) {
                Platform.runLater(() -> showAlert("Erreur", e.getMessage()));
            }
        });
    }

    private void checkForReplies() {
        executorService.submit(() -> {
            List<Commentaire> replies = commentaireService.getReplies(commentaire.getCommentaire_id());
            Platform.runLater(() -> {
                boolean hasReplies = !replies.isEmpty();
                toggleRepliesButton.setVisible(hasReplies);
                toggleRepliesButton.setManaged(hasReplies);
                if (!hasReplies) {
                    repliesContainer.setVisible(false);
                    repliesContainer.setManaged(false);
                }
            });
        });
    }

    public void displayReactions() {
        reactionContainer.getChildren().clear();
        Map<String, Integer> reactions = commentaire.getReactions();
        for (Map.Entry<String, Integer> entry : reactions.entrySet()) {
            String emojiUrl = entry.getKey();
            int count = entry.getValue();
            HBox reactionBox = new HBox(2);
            reactionBox.setAlignment(Pos.CENTER_LEFT);

            try {
                Image emojiImage = imageCache.computeIfAbsent(emojiUrl, k -> {
                    try {
                        return new Image(emojiUrl, 20, 20, true, true);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid URL or resource not found for emoji: " + emojiUrl + " - " + e.getMessage());
                        return new Image(getClass().getResourceAsStream("/forumUI/icons/like.png"), 20, 20, true, true);
                    }
                });
                if (!emojiImage.isError()) {
                    ImageView emojiIcon = new ImageView(emojiImage);
                    emojiIcon.getStyleClass().add("reaction-emoji-icon");
                    Label countLabel = new Label(String.valueOf(count));
                    countLabel.getStyleClass().add("reaction-count-label");
                    reactionBox.getChildren().addAll(emojiIcon, countLabel);
                } else {
                    System.err.println("Failed to load reaction emoji for URL: " + emojiUrl + " - " + emojiImage.getException());
                    Label fallbackLabel = new Label("👍 " + count);
                    fallbackLabel.getStyleClass().add("reaction-label");
                    reactionContainer.getChildren().add(fallbackLabel);
                    continue;
                }
            } catch (Exception e) {
                System.err.println("Error loading emoji image: " + e.getMessage());
                Label fallbackLabel = new Label("👍 " + count);
                fallbackLabel.getStyleClass().add("reaction-label");
                reactionContainer.getChildren().add(fallbackLabel);
                continue;
            }

            reactionContainer.getChildren().add(reactionBox);
        }
    }

    public void displayUserReaction() {
        String userReaction = commentaire.getUserReaction();
        if (userReaction != null && !userReaction.isEmpty()) {
            try {
                Image emojiImage = imageCache.computeIfAbsent(userReaction, k -> {
                    try {
                        return new Image(userReaction, 20, 20, true, true);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid URL or resource not found for user reaction: " + userReaction + " - " + e.getMessage());
                        return new Image(getClass().getResourceAsStream("/forumUI/icons/like.png"), 20, 20, true, true);
                    }
                });
                if (!emojiImage.isError()) selectedEmojiImage.setImage(emojiImage);
                else selectedEmojiImage.setImage(new Image(getClass().getResourceAsStream("/forumUI/icons/like.png"), 20, 20, true, true));
            } catch (Exception e) {
                System.err.println("Error loading user reaction image: " + e.getMessage());
                selectedEmojiImage.setImage(new Image(getClass().getResourceAsStream("/forumUI/icons/like.png"), 20, 20, true, true));
            }
        } else {
            selectedEmojiImage.setImage(null);
        }
    }

    private void showEmojiPicker() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.hide();
            return;
        }

        emojiPopup = new Popup();
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
                            emojiPopup.hide();
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
                    emojiPopup.getContent().add(scrollPane);
                    emojiPopup.show(reactButton, reactButton.getScene().getWindow().getX() + reactButton.localToScene(0, 0).getX(),
                            reactButton.getScene().getWindow().getY() + reactButton.localToScene(0, 0).getY() + reactButton.getHeight());
                    emojiPopup.setAutoHide(true);
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
                            emojiPopup.hide();
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
                    emojiPopup.getContent().add(scrollPane);
                    emojiPopup.show(reactButton, reactButton.getScene().getWindow().getX() + reactButton.localToScene(0, 0).getX(),
                            reactButton.getScene().getWindow().getY() + reactButton.localToScene(0, 0).getY() + reactButton.getHeight());
                    emojiPopup.setAutoHide(true);
                });
            }
        });
    }

    private void handleReaction(String emojiUrl) {
        executorService.submit(() -> {
            String existingReaction = commentaireService.getUserReaction(commentaire.getCommentaire_id(), userId);
            if (existingReaction != null) {
                commentaireService.removeReaction(commentaire.getCommentaire_id(), userId);
                Map<String, Integer> reactions = commentaire.getReactions();
                if (reactions.containsKey(existingReaction)) {
                    int currentCount = reactions.get(existingReaction);
                    if (currentCount > 1) {
                        reactions.put(existingReaction, currentCount - 1);
                    } else {
                        reactions.remove(existingReaction);
                    }
                }
            }
            commentaireService.addReaction(commentaire.getCommentaire_id(), userId, emojiUrl);
            Map<String, Integer> updatedReactions = commentaireService.getReactions(commentaire.getCommentaire_id());
            commentaire.setReactions(updatedReactions);
            commentaire.setUserReaction(emojiUrl);
            Platform.runLater(() -> {
                displayReactions();
                displayUserReaction();
                checkPrivilegeChange(userId);
                checkPrivilegeChange(commentaire.getUtilisateur().getId());
            });
        });
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
        stage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/alert.png").toExternalForm()));

        return alert.showAndWait().filter(response -> response == addAnywayButton).isPresent();
    }

    private int getPrivilegeRank(String privilege) {
        return switch (privilege) {
            case "regular" -> 0;
            case "top_contributor" -> 1;
            case "top_fan" -> 2;
            default -> -1;
        };
    }

    public Commentaire getCommentaire() {
        return commentaire;
    }
}