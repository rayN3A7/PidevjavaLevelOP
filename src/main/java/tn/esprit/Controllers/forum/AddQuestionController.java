package tn.esprit.Controllers.forum;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import tn.esprit.Models.Games;
import tn.esprit.Models.Question;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.GamesService;
import tn.esprit.Services.QuestionService;
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
import tn.esprit.Services.UtilisateurService;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddQuestionController implements Initializable {
    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentField;
    @FXML
    private Button submitButton;
    @FXML
    private VBox questionCardContainer;
    @FXML
    private Button uploadMediaButton;
    @FXML
    private ImageView uploadedImageView;
    @FXML
    private ComboBox<String> gameComboBox;

    private UtilisateurService us = new UtilisateurService();
    private int userId = SessionManager.getInstance().getUserId();
    private GamesService gamesService = new GamesService();
    private QuestionService questionService = new QuestionService();
    private String lastMediaPath;
    private String lastMediaType;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadGames();
    }

    private void loadGames() {
        executorService.submit(() -> {
            List<Games> gamesList = gamesService.getAll();
            Platform.runLater(() -> gameComboBox.getItems().setAll(
                    gamesList.stream().map(Games::getGame_name).toList()
            ));
        });
    }

    @FXML
    private void handleUploadMedia(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Question Media");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Media Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.mp4")
        );

        Stage stage = (Stage) uploadMediaButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null && isValidMediaFile(selectedFile)) {
            // Proceed with upload
        } else {
            showAlert("Erreur", "Type de fichier non supporté ou invalide.");
        }
        if (selectedFile != null) {
            executorService.submit(() -> {
                try {
                    String destinationDir = "C:\\xampp\\htdocs\\img";
                    Path destinationPath = Paths.get(destinationDir);
                    if (!Files.exists(destinationPath)) Files.createDirectories(destinationPath);

                    String fileName = "question_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                    Path targetPath = destinationPath.resolve(fileName);
                    Files.copy(selectedFile.toPath(), targetPath);

                    lastMediaPath = fileName;
                    String fileExtension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf(".") + 1).toLowerCase();

                    Platform.runLater(() -> {
                        if ("mp4".equals(fileExtension)) {
                            lastMediaType = "video";
                            uploadedImageView.setImage(null);
                            showSuccessAlert("Succès", "Video uploaded successfully: " + fileName);
                        } else {
                            lastMediaType = "image";
                            Image image = new Image(selectedFile.toURI().toString(), 200, 150, true, true);
                            if (!image.isError()) {
                                uploadedImageView.setImage(image);
                                showSuccessAlert("Succès", "Image uploaded successfully: " + fileName);
                            } else {
                                showAlert("Erreur", "Failed to load image preview: " + image.getException().getMessage());
                            }
                        }
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> showAlert("Erreur", "Failed to upload media: " + e.getMessage()));
                    e.printStackTrace();
                }
            });
        }
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        String title = titleField.getText().trim();
        String content = contentField.getText().trim();
        String selectedGame = gameComboBox.getValue();

        if (title.isEmpty() || content.isEmpty() || selectedGame == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }

        executorService.submit(() -> {
            try {
                if (ProfanityChecker.containsProfanity(title) || ProfanityChecker.containsProfanity(content)) {
                    final boolean[] proceed = {false}; // Use an array to update from within lambda
                    Platform.runLater(() -> {
                        proceed[0] = showProfanityWarningAlert("Avertissement",
                                "Votre texte contient des mots inappropriés. Vous risquez d'être banni ou signalé. Voulez-vous ajouter quand même?");
                    });
                    // Wait for the UI thread to complete (simple polling, could be improved with a semaphore or callback)
                    while (!Thread.currentThread().isInterrupted() && !proceed[0] && !Platform.isFxApplicationThread()) {
                        Thread.yield(); // Avoid busy-waiting, but this is a simple solution
                    }
                    if (!proceed[0]) return;
                }

                Games selectedGameObj = gamesService.getByName(selectedGame);
                if (selectedGameObj == null) {
                    Platform.runLater(() -> showAlert("Erreur", "Le jeu sélectionné n'existe pas."));
                    return;
                }

                Utilisateur utilisateur = us.getOne(userId);
                if (utilisateur == null) {
                    Platform.runLater(() -> showAlert("Erreur", "Utilisateur non trouvé pour ID: " + userId));
                    return;
                }

                String mediaPath = lastMediaPath;
                String mediaType = lastMediaType != null ? lastMediaType : "image";
                Question question = new Question(title, content, selectedGameObj, utilisateur, 0,
                        new Timestamp(System.currentTimeMillis()), mediaPath, mediaType);

                questionService.add(question);
                Platform.runLater(() -> {
                    showSuccessAlert("Succès", "Question ajoutée avec succès !");
                    clearForm();
                    navigateToForumPage(question);
                });
            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Erreur Réseau", "Impossible de vérifier le contenu pour le moment. Veuillez réessayer plus tard. Détails: " + e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Erreur", "Failed to add question: " + e.getMessage()));
                e.printStackTrace();
            }
        });
    }
    private boolean isValidMediaFile(File file) {
        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
        if ("mp4".equals(extension)) {
            try {
                Media media = new Media(file.toURI().toString());
                MediaPlayer testPlayer = new MediaPlayer(media);
                testPlayer.setOnError(() -> { throw new RuntimeException("Invalid media"); });
                Thread.sleep(100); // Wait briefly to check for errors
                testPlayer.dispose();
                return true;
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Erreur", "Fichier vidéo non valide ou corrompu."));
                return false;
            }
        }
        return List.of("png", "jpg", "jpeg", "gif").contains(extension);
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

    private void navigateToForumPage(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/Forum.fxml"));
            Parent root = loader.load();
            ForumController forumController = loader.getController();
            forumController.refreshQuestions();

            Stage stage = (Stage) submitButton.getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.show();

            Platform.runLater(() -> forumController.loadQuestionsLazy());
        } catch (IOException e) {
            e.printStackTrace();
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

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(new Image(getClass().getResource("/forumUI/icons/sucessalert.png").toExternalForm()));
        icon.setFitHeight(60);
        icon.setFitWidth(80);
        alert.setGraphic(icon);

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/forumUI/alert.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("gaming-alert");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/forumUI/icons/sucessalert.png").toString()));

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);

        alert.showAndWait();
    }
}