package tn.esprit.Controllers.Utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;

import org.mindrot.jbcrypt.BCrypt;

import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ProfilController implements Initializable {
    @FXML
    private ImageView profileImage;
    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private TextField txtNickname;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtOldPassword;
    @FXML
    private Button btnChangePhoto;
    @FXML
    private Button btnUpdateInfo;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userNicknameLabel;
    @FXML
    private StackPane profileImageContainer;
    @FXML
    private Label lblMessage;
    @FXML
    private Label lblPasswordRequirements;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private Utilisateur currentUser;
    private String currentPhotoPath;
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "C:\\xampp\\htdocs\\img\\";
    private static final String DEFAULT_AVATAR = "default-avatar.jpg";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int userId = SessionManager.getInstance().getUserId();
        currentUser = utilisateurService.getOne(userId);

        if (currentUser != null) {
            setupImageStyle();
            loadUserData();
            setupEventHandlers();
            setupPasswordValidation();
        } else {
            showMessage("Error loading user data", false);
        }
    }

    private void setupImageStyle() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        profileImageContainer.setEffect(dropShadow);
        profileImage.setPreserveRatio(true);
        profileImage.setSmooth(true);
    }

    private void loadUserData() {
        txtNom.setText(currentUser.getNom());
        txtPrenom.setText(currentUser.getPrenom());
        txtNickname.setText(currentUser.getNickname());

        // Update the display labels
        userNameLabel.setText(currentUser.getNom() + " " + currentUser.getPrenom());
        userNicknameLabel.setText(currentUser.getNickname());

        // Check if user has a photo and load it
        String userPhoto = currentUser.getPhoto();
        if (userPhoto != null && !userPhoto.isEmpty()) {
            currentPhotoPath = userPhoto;
            loadProfileImage();
        } else {
            loadDefaultAvatar();
        }
    }

    private void setupEventHandlers() {
        btnChangePhoto.setOnAction(e -> handleChangePhoto());
        btnUpdateInfo.setOnAction(e -> handleSave());
    }

    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(btnChangePhoto.getScene().getWindow());
        if (selectedFile != null) {
            try {
                selectedImageFile = selectedFile;
                Image originalImage = new Image(selectedFile.toURI().toString());

                // Load the crop dialog FXML
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/gestion Utilisateur/Profil/ImageCropDialog.fxml"));
                Parent cropDialog = loader.load();
                ImageCropDialogController cropController = loader.getController();

                // Show the dialog
                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.APPLICATION_MODAL);
                dialogStage.initStyle(StageStyle.UNDECORATED);
                dialogStage.setScene(new Scene(cropDialog));

                // Set the image to crop
                cropController.setImage(originalImage);

                // Show dialog and wait for result
                dialogStage.showAndWait();

                // Get the cropped image
                Image croppedImage = cropController.getCroppedImage();
                if (croppedImage != null) {
                    currentPhotoPath = saveImageAsPNG(croppedImage);
                    processAndDisplayImage(croppedImage);
                    currentUser.setPhoto(currentPhotoPath);
                    utilisateurService.update(currentUser);
                    showMessage("Photo updated successfully", true);
                }
            } catch (Exception ex) {
                showMessage("Error processing image: " + ex.getMessage(), false);
            }
        }
    }

    private void setupPasswordValidation() {
        txtPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                lblPasswordRequirements.setText(
                        "Password must contain at least 8 characters, including uppercase, lowercase, number and special character");
                lblPasswordRequirements.getStyleClass().removeAll("error", "success");
            } else {
                if (isValidPassword(newValue)) {
                    lblPasswordRequirements.setText("Password meets requirements");
                    lblPasswordRequirements.getStyleClass().remove("error");
                    lblPasswordRequirements.getStyleClass().add("success");
                } else {
                    lblPasswordRequirements.setText(
                            "Password must contain at least 8 characters, including uppercase, lowercase, number and special character");
                    lblPasswordRequirements.getStyleClass().remove("success");
                    lblPasswordRequirements.getStyleClass().add("error");
                }
            }
        });
    }

    private void handleSave() {
        if (!validateInputs())
            return;

        currentUser.setNom(txtNom.getText());
        currentUser.setPrenom(txtPrenom.getText());
        currentUser.setNickname(txtNickname.getText());

        // Handle password change if provided
        if (!txtPassword.getText().isEmpty()) {
            // Verify old password first
            if (txtOldPassword.getText().isEmpty()) {
                showMessage("Please enter your current password", false);
                return;
            }

            if (!BCrypt.checkpw(txtOldPassword.getText(), currentUser.getMotPasse())) {
                showMessage("Current password is incorrect", false);
                return;
            }

            if (!isValidPassword(txtPassword.getText())) {
                showMessage("New password does not meet requirements", false);
                return;
            }

            currentUser.setMotPasse(BCrypt.hashpw(txtPassword.getText(), BCrypt.gensalt()));
        }

        try {
            utilisateurService.update(currentUser);
            loadUserData();
            // Clear password fields after successful update
            txtOldPassword.clear();
            txtPassword.clear();
            showMessage("Profile updated successfully", true);
            navigateToHome();
        } catch (Exception e) {
            showMessage("Error updating profile: " + e.getMessage(), false);
        }
    }
    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) btnUpdateInfo.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            btnUpdateInfo.setText("Impossible de charger la page d'accueil.");
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() || txtNickname.getText().isEmpty()) {
            showMessage("Please fill in all required fields", false);
            return false;
        }
        return true;
    }

    private void showMessage(String message, boolean isSuccess) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: " + (isSuccess ? "green" : "red") + ";");
        lblMessage.setVisible(true);
    }

    private boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$";
        return Pattern.matches(passwordRegex, password);
    }

    private void processAndDisplayImage(Image originalImage) {
        double size = 100;
        profileImage.setImage(originalImage);
        profileImage.setFitWidth(size);
        profileImage.setFitHeight(size);

        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(size / 2);
        clip.setCenterX(size / 2);
        clip.setCenterY(size / 2);
        profileImage.setClip(clip);
    }

    private void loadDefaultAvatar() {
        try {
            File defaultAvatarFile = new File(UPLOAD_DIR + DEFAULT_AVATAR);
            if (defaultAvatarFile.exists()) {
                Image defaultImage = new Image(defaultAvatarFile.toURI().toString());
                if (!defaultImage.isError()) {
                    processAndDisplayImage(defaultImage);
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading default avatar: " + e.getMessage());
        }
    }

    private String saveImageAsPNG(Image image) throws IOException {
        String fileName = System.currentTimeMillis() + ".png";
        Path targetPath = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(Paths.get(UPLOAD_DIR));

        WritableImage writableImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        ImageView tempImageView = new ImageView(image);
        tempImageView.snapshot(null, writableImage);

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
        ImageIO.write(bufferedImage, "PNG", targetPath.toFile());

        return fileName;
    }

    private void loadProfileImage() {
        try {
            // Create upload directory if it doesn't exist
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String photoPath = currentUser.getPhoto();
            if (photoPath != null && !photoPath.isEmpty()) {
                currentPhotoPath = photoPath;
                File photoFile = new File(UPLOAD_DIR + photoPath);
                if (photoFile.exists()) {
                    try {
                        Image originalImage = new Image(photoFile.toURI().toString());
                        if (!originalImage.isError()) {
                            processAndDisplayImage(originalImage);
                            return;
                        }
                    } catch (Exception e) {
                        System.out.println("Error loading profile image: " + e.getMessage());
                    }
                }
            }
            loadDefaultAvatar();
        } catch (Exception e) {
            System.out.println("Error in loadProfileImage: " + e.getMessage());
            loadDefaultAvatar();
        }
    }
}