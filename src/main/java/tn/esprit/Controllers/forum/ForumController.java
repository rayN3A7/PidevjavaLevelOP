package tn.esprit.Controllers.forum;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Models.Question;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.QuestionService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.EventBus;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ForumController implements Initializable {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();
    private static final int FADE_DURATION_MS = 300;
    private static final String ALERT_CSS = "/forumUI/alert.css";
    private static final String ALERT_ICON = "/forumUI/icons/alert.png";
    private static final String SUCCESS_ICON = "/forumUI/icons/sucessalert.png";
    private static final int QUESTIONS_PER_PAGE = 4;

    @FXML private VBox questionCardContainer;
    @FXML private Button addQuestionButton;
    @FXML private TextField searchField;
    @FXML private BorderPane mainLayout;
    @FXML private Button navigateToQuestionFormButton;
    @FXML private Button loadMoreButton;
    private final QuestionService questionService = new QuestionService();
    private final UtilisateurService us = new UtilisateurService();
    private final int userId = SessionManager.getInstance().getUserId();
    private final Map<Question, Parent> questionCardMap = new HashMap<>();
    private ObservableList<Question> allQuestions = FXCollections.observableArrayList();
    private int currentQuestionCount = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadInitialQuestions();
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterQuestionsRealTime(newVal));
        setupFloatingAnimation();
        EventBus.getInstance().addHandler(event -> {
            CompletableFuture.supplyAsync(() -> us.getOne(event.getUserId()), EXECUTOR_SERVICE)
                    .thenAcceptAsync(user -> {
                        if (user != null) {
                            updatePrivilegeUI(event.getUserId());
                            if (event.getUserId() == userId) {
                                UtilisateurService.PrivilegeChange change = new UtilisateurService.PrivilegeChange(user.getPrivilege(), event.getNewPrivilege());
                                showPrivilegeAlert(change);
                            }
                        }
                    }, Platform::runLater);
        });
    }

    private void setupFloatingAnimation() {
        applyFloatingAnimation(addQuestionButton);
        applyFloatingAnimation(navigateToQuestionFormButton);
        applyFloatingAnimation(loadMoreButton);
    }

    private void applyFloatingAnimation(Button button) {
        TranslateTransition floatTransition = new TranslateTransition(Duration.seconds(2), button);
        floatTransition.setFromY(0);
        floatTransition.setToY(-10);
        floatTransition.setCycleCount(TranslateTransition.INDEFINITE);
        floatTransition.setAutoReverse(true);
        floatTransition.setRate(1.0);
        floatTransition.play();
    }

    public void loadInitialQuestions() {
        CompletableFuture.supplyAsync(() -> {
                    List<Question> questions = questionService.getAll();
                    System.out.println("Loaded questions from DB: " + (questions != null ? questions.size() : "null"));
                    return questions;
                }, EXECUTOR_SERVICE)
                .thenAcceptAsync(questions -> {
                    if (questions == null || questions.isEmpty()) {
                        System.out.println("No questions found in database.");
                        questionCardContainer.getChildren().setAll(new Label("Aucune question disponible."));
                        loadMoreButton.setDisable(true);
                        return;
                    }
                    allQuestions.setAll(questions);
                    questionCardContainer.getChildren().clear();
                    questionCardMap.clear();
                    currentQuestionCount = 0;
                    loadMoreQuestions();
                }, Platform::runLater)
                .exceptionally(throwable -> {
                    System.err.println("Error loading questions: " + throwable.getMessage());
                    Platform.runLater(() -> showAlert("Erreur", "Erreur lors du chargement des questions: " + throwable.getMessage()));
                    return null;
                });
    }

    @FXML
    private void loadMoreQuestions() {
        System.out.println("Loading more questions. Current count: " + currentQuestionCount + ", Total: " + allQuestions.size());
        int startIndex = currentQuestionCount;
        int endIndex = Math.min(startIndex + QUESTIONS_PER_PAGE, allQuestions.size());
        if (startIndex >= allQuestions.size()) {
            loadMoreButton.setDisable(true);
            return;
        }

        for (int i = startIndex; i < endIndex; i++) {
            Question question = allQuestions.get(i);
            addQuestionCard(question);
        }
        currentQuestionCount = endIndex;
        filterQuestionsRealTime(searchField.getText().trim().toLowerCase());
        loadMoreButton.setDisable(currentQuestionCount >= allQuestions.size());
        System.out.println("Loaded " + (endIndex - startIndex) + " more questions. New count: " + currentQuestionCount);
    }

    private void filterQuestionsRealTime(String searchText) {
        String query = searchText.trim().toLowerCase();
        CompletableFuture.supplyAsync(() -> {
                    return questionCardMap.entrySet().stream()
                            .filter(entry -> {
                                Question question = entry.getKey();
                                String gameName = question.getGame() != null && question.getGame().getGame_name() != null
                                        ? question.getGame().getGame_name().toLowerCase()
                                        : "";
                                String gameType = question.getGame() != null && question.getGame().getGameType() != null
                                        ? question.getGame().getGameType().toLowerCase()
                                        : "";
                                String title = question.getTitle() != null ? question.getTitle().toLowerCase() : "";
                                String content = question.getContent() != null ? question.getContent().toLowerCase() : "";
                                String authorNickname = question.getUser() != null && question.getUser().getNickname() != null
                                        ? question.getUser().getNickname().toLowerCase()
                                        : "";
                                return query.isEmpty() ||
                                        gameName.contains(query) ||
                                        gameType.contains(query) ||
                                        title.contains(query) ||
                                        content.contains(query) ||
                                        authorNickname.contains(query);
                            })
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                }, EXECUTOR_SERVICE)
                .thenAcceptAsync(filteredCards -> {
                    questionCardMap.forEach((question, card) -> {
                        boolean matches = filteredCards.containsKey(question);
                        card.setVisible(matches);
                        card.setManaged(matches);
                    });
                    questionCardContainer.requestLayout();
                }, Platform::runLater);
    }

    public void refreshQuestions() {
        QuestionCardController.stopAllVideos();
        loadInitialQuestions();
    }

    private void addQuestionCard(Question question) {
        CompletableFuture.supplyAsync(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/QuestionCard.fxml"));
                Parent card = loader.load();
                QuestionCardController controller = loader.getController();
                controller.setQuestionData(question, this);
                card.setUserData(controller);
                return card;
            } catch (IOException e) {
                System.err.println("Erreur lors de l'ajout de la carte de question: " + e.getMessage());
                return null;
            }
        }, EXECUTOR_SERVICE).thenAcceptAsync(card -> {
            if (card != null) {
                questionCardMap.put(question, card);
                questionCardContainer.getChildren().add(card);
                card.setVisible(true);
                filterQuestionsRealTime(searchField.getText().trim().toLowerCase());
            }
        }, Platform::runLater);
    }

    public void handleUpvote(Question question, Label votesLabel, Button downvoteButton) {
        CompletableFuture.runAsync(() -> {
            questionService.upvoteQuestion(question.getQuestion_id());
            int updatedVotes = questionService.getVotes(question.getQuestion_id());
            question.setVotes(updatedVotes);
            Platform.runLater(() -> {
                votesLabel.setText("Votes: " + updatedVotes);
                downvoteButton.setDisable(updatedVotes == 0);
            });
        }, EXECUTOR_SERVICE).exceptionally(t -> {
            Platform.runLater(() -> showAlert("Erreur", "Erreur lors de l'upvote:\n" + t.getMessage()));
            return null;
        });
    }

    public void handleDownvote(Question question, Label votesLabel, Button downvoteButton) {
        if (question.getVotes() <= 0) return;
        CompletableFuture.runAsync(() -> {
            questionService.downvoteQuestion(question.getQuestion_id());
            int updatedVotes = questionService.getVotes(question.getQuestion_id());
            question.setVotes(updatedVotes);
            Platform.runLater(() -> {
                votesLabel.setText("Votes: " + updatedVotes);
                downvoteButton.setDisable(updatedVotes == 0);
            });
        }, EXECUTOR_SERVICE).exceptionally(t -> {
            Platform.runLater(() -> showAlert("Erreur", "Erreur lors du downvote:\n" + t.getMessage()));
            return null;
        });
    }

    public void showPrivilegeAlert(UtilisateurService.PrivilegeChange change) {
        if (!change.isChanged()) return;
        boolean isPromotion = getPrivilegeRank(change.getNewPrivilege()) > getPrivilegeRank(change.getOldPrivilege());
        String title = isPromotion ? "Félicitations!" : "Mise à jour";
        String message = getPrivilegeMessage(change, isPromotion);
        showStyledAlert(title, message, isPromotion ? SUCCESS_ICON : ALERT_ICON, isPromotion ? SUCCESS_ICON : ALERT_ICON,
                isPromotion ? "GG!" : "OK", 60, 60);
    }

    private String getPrivilegeMessage(UtilisateurService.PrivilegeChange change, boolean isPromotion) {
        return isPromotion ?
                switch (change.getNewPrivilege()) {
                    case "top_contributor" -> "Vous êtes Top Contributor !";
                    case "top_fan" -> "Vous êtes Top Fan depuis " + change.getOldPrivilege() + " !";
                    default -> "Privilege mis à jour !";
                } :
                switch (change.getOldPrivilege()) {
                    case "top_contributor" -> "Vous êtes redevenu Regular.";
                    case "top_fan" -> "Vous êtes passé à " + change.getNewPrivilege() + ".";
                    default -> "Privilege mis à jour.";
                };
    }

    private int getPrivilegeRank(String privilege) {
        return switch (privilege) {
            case "regular" -> 0;
            case "top_contributor" -> 1;
            case "top_fan" -> 2;
            default -> -1;
        };
    }

    public void updateQuestion(Question question) {
        Utilisateur currentUser = us.getOne(userId);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && question.getUser().getId() != userId)) {
            showAlert("Erreur", "Permission refusée.");
            return;
        }
        CompletableFuture.runAsync(() -> questionService.update(question), EXECUTOR_SERVICE)
                .thenComposeAsync(v -> CompletableFuture.supplyAsync(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/UpdateQuestionForm.fxml"));
                        Parent root = loader.load();
                        UpdateQuestionController controller = loader.getController();
                        controller.loadQuestionData(question);
                        return root;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, EXECUTOR_SERVICE))
                .thenAcceptAsync(root -> {
                    Stage stage = (Stage) questionCardContainer.getScene().getWindow();
                    stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
                    stage.show();
                }, Platform::runLater);
    }
    public void deleteQuestion(Question question) {
        Utilisateur currentUser = us.getOne(userId);
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && question.getUser().getId() != userId)) {
            showAlert("Erreur", "Permission refusée.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmer la suppression");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Voulez-vous vraiment supprimer cette question et tous ses commentaires ? ");

        ImageView icon = new ImageView(new Image(getClass().getResource(ALERT_ICON).toExternalForm()));
        icon.setFitHeight(80);
        icon.setFitWidth(80);
        confirmationAlert.setGraphic(icon);

        confirmationAlert.getDialogPane().getStylesheets().add(getClass().getResource(ALERT_CSS).toExternalForm());
        confirmationAlert.getDialogPane().getStyleClass().add("gaming-alert");

        Stage alertStage = (Stage) confirmationAlert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResource(ALERT_ICON).toExternalForm()));

        ButtonType deleteButton = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmationAlert.getButtonTypes().setAll(deleteButton, cancelButton);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION_MS), confirmationAlert.getDialogPane());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        confirmationAlert.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) fadeIn.play();
        });

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == deleteButton) {
            CompletableFuture.runAsync(() -> questionService.delete(question.getQuestion_id(), userId), EXECUTOR_SERVICE)
                    .thenAcceptAsync(v -> {
                        Parent card = questionCardMap.get(question);
                        if (card != null) {
                            questionCardContainer.getChildren().remove(card);
                            questionCardMap.remove(question);
                            allQuestions.remove(question);
                            currentQuestionCount--;
                            loadMoreButton.setDisable(currentQuestionCount >= allQuestions.size());

                        }
                    }, Platform::runLater)
                    .exceptionally(t -> {
                        Platform.runLater(() -> showAlert("Erreur", "Erreur lors de la suppression: " + t.getMessage()));
                        return null;
                    });
        }
    }

    public void updatePrivilegeUI(int affectedUserId) {
        CompletableFuture.runAsync(() -> {
            questionCardContainer.getChildren().stream()
                    .filter(node -> node instanceof Parent)
                    .forEach(node -> {
                        QuestionCardController controller = (QuestionCardController) node.getUserData();
                        if (controller != null && controller.getQuestion().getUser().getId() == affectedUserId) {
                            Utilisateur user = us.getOne(affectedUserId);
                            if (user != null) Platform.runLater(() -> controller.updatePrivilegeUI(user));
                        }
                    });
        }, EXECUTOR_SERVICE);
    }

    @FXML
    private void navigateToAddQuestion() {
        QuestionCardController.stopAllVideos();
        CompletableFuture.supplyAsync(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/QuestionForm.fxml"));
                return loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, EXECUTOR_SERVICE).thenAcceptAsync(root -> {
            Stage stage = (Stage) questionCardContainer.getScene().getWindow();
            stage.setScene(new Scene((Parent) root, stage.getWidth(), stage.getHeight()));
            stage.show();
        }, Platform::runLater);
    }

    public void handleReaction(Question question, String emojiUrl) {
        CompletableFuture.runAsync(() -> {
            questionService.addReaction(question.getQuestion_id(), userId, emojiUrl);
            Map<String, Integer> updatedReactions = questionService.getReactions(question.getQuestion_id());
            question.setReactions(updatedReactions);
            question.setUserReaction(emojiUrl);
            Platform.runLater(() -> {
                questionCardContainer.getChildren().stream()
                        .filter(node -> node instanceof Parent)
                        .forEach(node -> {
                            QuestionCardController controller = (QuestionCardController) node.getUserData();
                            if (controller != null && controller.getQuestion().equals(question)) {
                                // Reaction handling can be updated here if needed
                            }
                        });
            });
        }, EXECUTOR_SERVICE);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Label contentLabel = new Label(message);
        contentLabel.setWrapText(true);
        alert.getDialogPane().setContent(contentLabel);
        alert.getDialogPane().setPrefWidth(400);

        ImageView icon = new ImageView(new Image(getClass().getResource(ALERT_ICON).toExternalForm()));
        icon.setFitHeight(80);
        icon.setFitWidth(80);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource(ALERT_CSS).toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource(ALERT_ICON).toExternalForm()));

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION_MS), alert.getDialogPane());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        alert.showAndWait();
    }

    private void showStyledAlert(String title, String message, String iconPath, String stageIconPath, String buttonText, double iconHeight, double iconWidth) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Label contentLabel = new Label(message);
        contentLabel.setWrapText(true);
        alert.getDialogPane().setContent(contentLabel);
        alert.getDialogPane().setPrefWidth(400);

        ImageView icon = new ImageView(new Image(getClass().getResource(iconPath).toExternalForm()));
        icon.setFitHeight(iconHeight);
        icon.setFitWidth(iconWidth);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource(ALERT_CSS).toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource(stageIconPath).toExternalForm()));

        ButtonType okButton = new ButtonType(buttonText, ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION_MS), alert.getDialogPane());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        alert.showAndWait();
    }
}