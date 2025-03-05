package tn.esprit.Controllers.forum;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.Models.Games;
import tn.esprit.Models.Question;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.GamesService;
import tn.esprit.Services.QuestionService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.EventBus;
import tn.esprit.utils.ProfanityChecker;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AddQuestionController implements Initializable, AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddQuestionController.class);
    private static final String DESTINATION_DIR = "C:\\xampp\\htdocs\\img";
    private static final List<String> VALID_MEDIA_EXTENSIONS = List.of("png", "jpg", "jpeg", "gif", "mp4");
    private static final int THREAD_POOL_SIZE = 2;
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    @FXML private TextField titleField;
    @FXML private TextArea contentField;
    @FXML private Button submitButton;
    @FXML private VBox questionCardContainer;
    @FXML private Button uploadMediaButton;
    @FXML private ImageView uploadedImageView;
    @FXML private ComboBox<String> gameComboBox;

    private final UtilisateurService utilisateurService;
    private final GamesService gamesService;
    private final QuestionService questionService;
    private final int userId;
    private String lastMediaPath;
    private String lastMediaType;
    private volatile boolean isShutdown;
    private final FXMLLoader forumLoader;

    public AddQuestionController() {
        utilisateurService = new UtilisateurService();
        gamesService = new GamesService();
        questionService = new QuestionService();
        userId = SessionManager.getInstance().getUserId();
        forumLoader = new FXMLLoader(getClass().getResource("/forumUI/Forum.fxml"));
        isShutdown = false;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadGamesAsync();
        EventBus.getInstance().addHandler(event -> {
            if (event.getUserId() == userId) {
                Utilisateur updatedUser = utilisateurService.getOne(userId);
                if (updatedUser != null) {
                    UtilisateurService.PrivilegeChange change = new UtilisateurService.PrivilegeChange(
                            updatedUser.getPrivilege(), event.getNewPrivilege());
                    showPrivilegeAlert(change);
                }
            }
        });
    }

    private void loadGamesAsync() {
        CompletableFuture.supplyAsync(gamesService::getAll, EXECUTOR_SERVICE)
                .thenAcceptAsync(games -> {
                    if (games != null && !games.isEmpty()) {
                        gameComboBox.getItems().setAll(
                                games.stream().map(Games::getGame_name).toList()
                        );
                    } else {
                        LOGGER.warn("No games found to populate ComboBox.");
                        gameComboBox.getItems().clear();
                        gameComboBox.setPromptText("Aucun jeu disponible");
                    }
                }, Platform::runLater)
                .exceptionally(this::handleAsyncError);
    }

    @FXML
    private void handleUploadMedia(ActionEvent event) {
        File selectedFile = selectMediaFile();
        if (selectedFile == null || !VALID_MEDIA_EXTENSIONS.contains(getFileExtension(selectedFile))) {
            showAlert("Erreur", "Type de fichier non supporté ou invalide. Formats acceptés : " + String.join(", ", VALID_MEDIA_EXTENSIONS));
            return;
        }

        uploadMediaAsync(selectedFile);
    }

    private File selectMediaFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un média pour la question");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers média", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.mp4")
        );
        Stage stage = (Stage) uploadMediaButton.getScene().getWindow();
        return fileChooser.showOpenDialog(stage);
    }

    private void uploadMediaAsync(File selectedFile) {
        CompletableFuture.runAsync(() -> {
            try {
                Path destinationPath = Paths.get(DESTINATION_DIR);
                if (!Files.exists(destinationPath)) Files.createDirectories(destinationPath);
                String fileName = "question_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = destinationPath.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath);

                lastMediaPath = fileName;
                String fileExtension = getFileExtension(selectedFile);

                Platform.runLater(() -> processUploadedMedia(selectedFile, fileExtension, fileName));
            } catch (IOException e) {
                LOGGER.error("Échec du téléchargement du média: {}", selectedFile.getName(), e);
                Platform.runLater(() -> showAlert("Erreur", "Échec du téléchargement du média: " + e.getMessage()));
            }
        }, EXECUTOR_SERVICE);
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        return (lastIndex == -1) ? "" : name.substring(lastIndex + 1).toLowerCase();
    }

    private void processUploadedMedia(File selectedFile, String fileExtension, String fileName) {
        if ("mp4".equals(fileExtension)) {
            lastMediaType = "video";
            uploadedImageView.setImage(null);
            showSuccessAlert("Succès", "Vidéo téléchargée avec succès : " + fileName);
        } else {
            lastMediaType = "image";
            Image image = new Image(selectedFile.toURI().toString(), 200, 150, true, true);
            if (!image.isError()) {
                uploadedImageView.setImage(image);
                showSuccessAlert("Succès", "Image téléchargée avec succès : " + fileName);
            } else {
                LOGGER.error("Échec du chargement de la prévisualisation de l'image: {}", image.getException().getMessage());
                showAlert("Erreur", "Échec du chargement de la prévisualisation de l'image : " + image.getException().getMessage());
            }
        }
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        String title = titleField.getText().trim();
        String content = contentField.getText().trim();
        String selectedGame = gameComboBox.getValue();

        if (!validateInput(title, content, selectedGame)) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }

        submitQuestionAsync(title, content, selectedGame);
    }

    private boolean validateInput(String title, String content, String selectedGame) {
        return !title.isEmpty() && !content.isEmpty() && selectedGame != null && !selectedGame.trim().isEmpty();
    }

    private void submitQuestionAsync(String title, String content, String selectedGame) {
        CompletableFuture.supplyAsync(() -> {
                    try {
                        if (ProfanityChecker.containsProfanity(title) || ProfanityChecker.containsProfanity(content)) {
                            AtomicBoolean proceed = new AtomicBoolean(false);
                            Platform.runLater(() -> proceed.set(showProfanityWarningAlert(
                                    "Avertissement", "Contenu inapproprié pourrait être signalé. Continuer?"
                            )));
                            while (!proceed.get() && !Thread.currentThread().isInterrupted()) {
                                Thread.onSpinWait();
                            }
                            if (!proceed.get()) return null;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Erreur lors de la vérification du contenu: " + e.getMessage(), e);
                    }

                    Games game = gamesService.getByName(selectedGame);
                    if (game == null) {
                        Platform.runLater(() -> showAlert("Erreur", "Le jeu sélectionné n'existe pas."));
                        return null;
                    }

                    Utilisateur user = utilisateurService.getOne(userId);
                    if (user == null) {
                        Platform.runLater(() -> showAlert("Erreur", "Utilisateur non trouvé pour ID: " + userId));
                        return null;
                    }

                    String mediaPath = lastMediaPath;
                    String mediaType = lastMediaType != null ? lastMediaType : "image";
                    Question question = new Question(title, content, game, user, 0,
                            new Timestamp(System.currentTimeMillis()), mediaPath, mediaType);

                    questionService.add(question);
                    return question;
                }, EXECUTOR_SERVICE)
                .thenAcceptAsync(this::handleSubmissionResult, Platform::runLater)
                .exceptionally(this::handleAsyncError);
    }

    private void handleSubmissionResult(Question question) {
        if (question == null) return;

        showSuccessAlert("Succès", "Question ajoutée avec succès !");
        clearForm();
        navigateToForumPage();
    }

    private Void handleAsyncError(Throwable e) {
        if (e instanceof IOException) {
            LOGGER.error("Erreur réseau lors de la soumission", e);
            showAlert("Erreur Réseau", "Impossible de vérifier le contenu: " + e.getMessage());
        } else {
            LOGGER.error("Échec de la soumission de la question", e);
            showAlert("Erreur", "Échec de l'ajout de la question: " + e.getMessage());
        }
        return null;
    }

    private void navigateToForumPage() {
        try {
            Parent root = forumLoader.load();
            ForumController forumController = forumLoader.getController();
            if (forumController == null) {
                LOGGER.error("ForumController non initialisé après le chargement de Forum.fxml");
                showAlert("Erreur", "Impossible de charger la page du forum.");
                return;
            }

            Stage stage = (Stage) submitButton.getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.show();

            forumController.loadInitialQuestions();
        } catch (IOException e) {
            LOGGER.error("Échec de la navigation vers la page du forum", e);
            showAlert("Erreur", "Échec de la navigation vers le forum: " + e.getMessage());
        }
    }

    private void clearForm() {
        titleField.clear();
        contentField.clear();
        gameComboBox.setValue(null);
        lastMediaPath = null;
        lastMediaType = null;
        uploadedImageView.setImage(null);
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
                    default -> "Privilège mis à jour !";
                }) :
                (switch (change.getOldPrivilege()) {
                    case "top_contributor" -> "Désolé, vous êtes redescendu de Top Contributor à Regular.";
                    case "top_fan" -> "Désolé, vous êtes passé de Top Fan à " + change.getNewPrivilege() + ".";
                    default -> "Privilège mis à jour.";
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

    private void showSuccessAlert(String title, String message) {
        showStyledAlert(title, message, "/forumUI/icons/sucessalert.png", "/forumUI/icons/sucessalert.png", "OK", 60, 80);
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
        stage.getIcons().add(new Image(getClass().getResource(stageIconPath).toExternalForm()));

        ButtonType okButton = new ButtonType(buttonText, ButtonBar.ButtonData.OK_DONE);
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

    @Override
    public void close() {
        if (!isShutdown) {
            EXECUTOR_SERVICE.shutdown();
            try {
                if (!EXECUTOR_SERVICE.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    EXECUTOR_SERVICE.shutdownNow();
                }
            } catch (InterruptedException e) {
                EXECUTOR_SERVICE.shutdownNow();
                Thread.currentThread().interrupt();
            }
            isShutdown = true;
        }
    }
}