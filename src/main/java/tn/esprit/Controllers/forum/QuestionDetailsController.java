package tn.esprit.Controllers.forum;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.Models.*;
import tn.esprit.Services.CommentaireService;
import tn.esprit.Services.GamesService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.PrivilegeEvent;
import tn.esprit.utils.ProfanityChecker;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class QuestionDetailsController implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionDetailsController.class);
    private static final int THREAD_POOL_SIZE = 2;
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    @FXML private Label questionTitle;
    @FXML private Label questionContent;
    @FXML private Label questionVotes;
    @FXML private TextField commentInput;
    @FXML private VBox commentContainer;
    @FXML private ImageView gameImageView;

    private Question currentQuestion;
    private final UtilisateurService utilisateurService;
    private final CommentaireService commentaireService;
    private final GamesService gamesService;
    private final int userId;
    private final Map<String, Image> imageCache;
    private final Map<Integer, StackPane> commentCardCache;
    private volatile boolean isShutdown;
    private final FXMLLoader forumLoader;

    public QuestionDetailsController() {
        utilisateurService = new UtilisateurService();
        commentaireService = new CommentaireService();
        gamesService = new GamesService();
        userId = SessionManager.getInstance().getUserId();
        imageCache = new HashMap<>();
        commentCardCache = new HashMap<>();
        isShutdown = false;
        forumLoader = new FXMLLoader(getClass().getResource("/forumUI/Forum.fxml"));
    }

    @FXML
    public void loadQuestionDetails(Question question) {
        this.currentQuestion = question;
        questionTitle.setText(question.getTitle());
        questionContent.setText(question.getContent());
        questionVotes.setText("Votes: " + question.getVotes());

        loadGameImageAsync();
        loadCommentsAsync();
        setupPrivilegeEventHandler();
        utilisateurService.setEventTarget(commentContainer);
    }

    private void loadGameImageAsync() {
        CompletableFuture.runAsync(() -> {
            Games game = gamesService.getByName(currentQuestion.getGame().getGame_name());
            if (game == null || game.getImagePath() == null || game.getImagePath().isEmpty()) return;

            File file = new File(game.getImagePath());
            if (!file.exists()) return;

            String cacheKey = game.getImagePath();
            Image image = imageCache.computeIfAbsent(cacheKey, k -> new Image(file.toURI().toString(), 200, 150, true, true));
            if (!image.isError()) {
                Platform.runLater(() -> gameImageView.setImage(image));
            } else {
                LOGGER.warn("Failed to load game image: {}", game.getImagePath());
            }
        }, EXECUTOR_SERVICE);
    }

    private void setupPrivilegeEventHandler() {
        commentContainer.addEventHandler(PrivilegeEvent.PRIVILEGE_CHANGED, event -> {
            Utilisateur user = utilisateurService.getOne(event.getUserId());
            if (user != null) {
                updatePrivilegeUI(event.getUserId());
                if (event.getUserId() == userId) {
                    UtilisateurService.PrivilegeChange change = new UtilisateurService.PrivilegeChange(user.getPrivilege(), event.getNewPrivilege());
                    showPrivilegeAlert(change);
                }
            }
        });
    }

    private void loadCommentsAsync() {
        CompletableFuture.supplyAsync(commentaireService::getAll, EXECUTOR_SERVICE)
                .thenAcceptAsync(comments -> {
                    commentContainer.getChildren().clear();
                    comments.stream()
                            .filter(comment -> comment.getQuestion() != null &&
                                    comment.getQuestion().getQuestion_id() == currentQuestion.getQuestion_id() &&
                                    comment.getParent_commentaire_id() == null)
                            .forEach(this::createCommentCard);
                }, Platform::runLater)
                .exceptionally(throwable -> {
                    LOGGER.error("Failed to load comments", throwable);
                    showAlert("Erreur", "Failed to load comments: " + throwable.getMessage());
                    return null;
                });
    }

    private void createCommentCard(Commentaire comment) {
        StackPane commentCard = commentCardCache.computeIfAbsent(comment.getCommentaire_id(), id -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/CommentCard.fxml"));
                StackPane card = loader.load();
                CommentCardController controller = loader.getController();
                controller.setCommentData(comment, this);
                card.setUserData(controller);
                return card;
            } catch (IOException e) {
                LOGGER.error("Error creating comment card for comment ID: {}", comment.getCommentaire_id(), e);
                return null;
            }
        });

        if (commentCard != null) {
            commentContainer.getChildren().add(commentCard);
        }
    }

    @FXML
    public void postComment() {
        Utilisateur user = utilisateurService.getOne(userId);
        if (user == null) {
            showAlert("Erreur", "Utilisateur non trouvé.");
            return;
        }

        String commentText = commentInput.getText().trim();
        if (commentText.isEmpty()) {
            showAlert("Erreur", "Vous ne pouvez pas ajouter un commentaire vide.");
            return;
        }

        postCommentAsync(user, commentText);
    }

    private void postCommentAsync(Utilisateur user, String commentText) {
        CompletableFuture.supplyAsync(() -> {
                    try {
                        if (ProfanityChecker.containsProfanity(commentText)) {
                            AtomicBoolean proceed = new AtomicBoolean(false);
                            Platform.runLater(() -> proceed.set(showProfanityWarningAlert(
                                    "Avertissement", "Votre commentaire contient des mots inappropriés. Voulez-vous ajouter quand même?"
                            )));
                            while (!proceed.get() && !Thread.currentThread().isInterrupted()) {
                                Thread.onSpinWait();
                            }
                            if (!proceed.get()) return null;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    Commentaire comment = new Commentaire();
                    comment.setContenu(commentText);
                    comment.setUtilisateur(user);
                    comment.setCreation_at(new Timestamp(System.currentTimeMillis()));
                    comment.setQuestion(currentQuestion);
                    commentaireService.add(comment);
                    return comment;
                }, EXECUTOR_SERVICE)
                .thenAcceptAsync(comment -> {
                    if (comment != null) {
                        commentInput.clear();
                        loadCommentsAsync();
                        updateUserPrivilege(userId);
                    }
                }, Platform::runLater)
                .exceptionally(throwable -> {
                    LOGGER.error("Failed to post comment", throwable);
                    showAlert("Erreur", "Erreur lors de la vérification du contenu: " + throwable.getMessage());
                    return null;
                });
    }

    public void deleteComment(Commentaire comment) {
        if (comment.getUtilisateur().getId() != userId) {
            showAlert("Erreur", "Vous ne pouvez supprimer que vos propres commentaires.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            commentaireService.delete(comment.getCommentaire_id(), userId);
            Platform.runLater(() -> {
                commentContainer.getChildren().removeIf(node -> {
                    CommentCardController controller = (CommentCardController) node.getUserData();
                    return controller != null && controller.getCommentaire().getCommentaire_id() == comment.getCommentaire_id();
                });
                commentCardCache.remove(comment.getCommentaire_id());
                updateUserPrivilege(userId);
                checkPrivilegeChange(comment.getUtilisateur().getId());
            });
        }, EXECUTOR_SERVICE).exceptionally(throwable -> {
            LOGGER.error("Failed to delete comment", throwable);
            showAlert("Erreur", "Erreur lors de la suppression: " + throwable.getMessage());
            return null;
        });
    }

    private void updateUserPrivilege(int userId) {
        UtilisateurService.PrivilegeChange change = utilisateurService.updateUserPrivilege(userId);
        if (change.isChanged()) {
            Utilisateur updatedUser = utilisateurService.getOne(userId);
            if (updatedUser != null) {
                updatePrivilegeUI(userId);
                if (userId == this.userId) {
                    showPrivilegeAlert(change);
                }
            }
        }
    }

    private void checkPrivilegeChange(int affectedUserId) {
        updateUserPrivilege(affectedUserId);
    }

    public void refreshQuestions() {
        loadCommentsAsync();
    }

    private void showPrivilegeAlert(UtilisateurService.PrivilegeChange change) {
        if (!change.isChanged()) return;

        boolean isPromotion = getPrivilegeRank(change.getNewPrivilege()) > getPrivilegeRank(change.getOldPrivilege());
        String title = isPromotion ? "Félicitations!" : "Mise à jour de privilège";
        String message = getPrivilegeMessage(change, isPromotion);
        String iconPath = isPromotion ?
                (change.getNewPrivilege().equals("top_contributor") ? "/forumUI/icons/silver_crown.png" : "/forumUI/icons/crown.png") :
                "/forumUI/icons/alert.png";
        String stageIconPath = isPromotion ? "/forumUI/icons/sucessalert.png" : "/forumUI/icons/alert.png";

        showStyledAlert(title, message, iconPath, stageIconPath, isPromotion ? "GG!" : "OK", 60, 60);
    }

    private String getPrivilegeMessage(UtilisateurService.PrivilegeChange change, boolean isPromotion) {
        return isPromotion ?
                (switch (change.getNewPrivilege()) {
                    case "top_contributor" -> "Vous êtes passé de Regular à Top Contributor ! Bravo pour votre contribution !";
                    case "top_fan" -> "Vous êtes maintenant un Top Fan depuis " + change.getOldPrivilege() + " ! Votre passion est récompensée !";
                    default -> "Privilege mis à jour !";
                }) :
                (switch (change.getOldPrivilege()) {
                    case "top_contributor" -> "Désolé, vous êtes redescendu de Top Contributor à Regular.";
                    case "top_fan" -> "Désolé, vous êtes passé de Top Fan à " + change.getNewPrivilege() + ".";
                    default -> "Privilege mis à jour.";
                });
    }

    private int getPrivilegeRank(String privilege) {
        return switch (privilege) {
            case "regular" -> 0;
            case "top_contributor" -> 1;
            case "top_fan" -> 2;
            default -> -1;
        };
    }

    private void showAlert(String title, String message) {
        showStyledAlert(title, message, "/forumUI/icons/alert.png", "/forumUI/icons/alert.png", "OK", 80, 80);
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

    @FXML
    private void goToForumPage(ActionEvent event) {
        try {
            Parent forumView = forumLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(forumView));
            stage.show();
        } catch (IOException e) {
            LOGGER.error("Failed to navigate to forum page", e);
        }
    }

    public void handleReactionC(Commentaire commentaire, String emojiUrl) {
        CompletableFuture.runAsync(() -> {
            String existingReaction = commentaireService.getUserReaction(commentaire.getCommentaire_id(), userId);
            if (existingReaction != null) {
                commentaireService.removeReaction(commentaire.getCommentaire_id(), userId);
                updateReactionCount(commentaire, existingReaction, -1);
            }

            commentaireService.addReaction(commentaire.getCommentaire_id(), userId, emojiUrl);
            Map<String, Integer> updatedReactions = commentaireService.getReactions(commentaire.getCommentaire_id());
            commentaire.setReactions(updatedReactions);
            commentaire.setUserReaction(emojiUrl);

            Platform.runLater(() -> {
                updateCommentCardReactions(commentaire);
                checkPrivilegeChange(userId);
                checkPrivilegeChange(commentaire.getUtilisateur().getId());
            });
        }, EXECUTOR_SERVICE).exceptionally(throwable -> {
            LOGGER.error("Failed to handle reaction", throwable);
            showAlert("Erreur", "Failed to handle reaction: " + throwable.getMessage());
            return null;
        });
    }

    private void updateReactionCount(Commentaire commentaire, String reaction, int delta) {
        Map<String, Integer> reactions = commentaire.getReactions();
        int currentCount = reactions.getOrDefault(reaction, 0) + delta;
        if (currentCount <= 0) {
            reactions.remove(reaction);
        } else {
            reactions.put(reaction, currentCount);
        }
    }

    private void updateCommentCardReactions(Commentaire commentaire) {
        commentContainer.getChildren().stream()
                .filter(node -> node instanceof StackPane)
                .forEach(node -> {
                    CommentCardController controller = (CommentCardController) node.getUserData();
                    if (controller != null && controller.getCommentaire().equals(commentaire)) {
                        controller.displayReactions();
                        controller.displayUserReaction();
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

        return alert.showAndWait().filter(response -> response == addAnywayButton).isPresent();
    }

    public void updatePrivilegeUI(int affectedUserId) {
        Platform.runLater(() -> {
            LOGGER.debug("Updating privilege UI for user ID: {}", affectedUserId);
            commentContainer.getChildren().stream()
                    .filter(node -> node instanceof StackPane)
                    .forEach(node -> {
                        CommentCardController controller = (CommentCardController) node.getUserData();
                        if (controller != null && controller.getCommentaire().getUtilisateur().getId() == affectedUserId) {
                            Utilisateur user = utilisateurService.getOne(affectedUserId);
                            if (user != null) {
                                LOGGER.debug("Updating comment card for user: {}", user.getNickname());
                                controller.updatePrivilegeUI(user);
                            } else {
                                LOGGER.warn("Failed to fetch user data for ID: {}", affectedUserId);
                            }
                        }
                    });
        });
    }

    @Override
    public void close() {
        if (!isShutdown) {
            EXECUTOR_SERVICE.shutdown();
            isShutdown = true;
        }
    }
}