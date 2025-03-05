package tn.esprit.Controllers.Utilisateur;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class ImageCropDialogController implements Initializable {
    @FXML
    private ImageView imageView;
    @FXML
    private StackPane imageContainer;
    @FXML
    private Pane overlayPane;
    @FXML
    private Circle cropCircle;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    private double circleRadius = 100; // Larger circle radius
    private double dragStartX, dragStartY;
    private double initialCircleX, initialCircleY;
    private Image originalImage;
    private Image croppedImage;
    private double imageScale;
    private double imageViewX;
    private double imageViewY;
    private boolean isDragging = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            setupCropCircle();
            setupDragHandling();
            setupButtons();
        });
    }

    private void setupCropCircle() {
        cropCircle.setRadius(circleRadius);
        cropCircle.setStroke(Color.WHITE);
        cropCircle.setStrokeWidth(3);
        cropCircle.setFill(Color.TRANSPARENT);
        cropCircle.setMouseTransparent(false);

        // Make sure overlay covers the entire container
        overlayPane.prefWidthProperty().bind(imageContainer.widthProperty());
        overlayPane.prefHeightProperty().bind(imageContainer.heightProperty());

        // Ensure proper stacking
        overlayPane.toFront();
        cropCircle.toFront();
    }

    private void setupDragHandling() {
        // Handle mouse events for the circle only
        cropCircle.setOnMousePressed(this::handleMousePressed);
        cropCircle.setOnMouseDragged(this::handleMouseDragged);
        cropCircle.setOnMouseReleased(this::handleMouseReleased);

        // Set cursor and make sure circle is draggable
        cropCircle.setCursor(javafx.scene.Cursor.MOVE);
        cropCircle.setMouseTransparent(false);

        // Make overlay transparent to mouse events
        overlayPane.setMouseTransparent(true);
    }

    private void handleMousePressed(MouseEvent e) {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
        initialCircleX = cropCircle.getLayoutX();
        initialCircleY = cropCircle.getLayoutY();
        isDragging = true;
        cropCircle.toFront();
        e.consume();
    }

    private void handleMouseDragged(MouseEvent e) {
        if (!isDragging)
            return;

        double deltaX = e.getSceneX() - dragStartX;
        double deltaY = e.getSceneY() - dragStartY;

        double newX = initialCircleX + deltaX;
        double newY = initialCircleY + deltaY;

        // Get the bounds of the actual image
        Bounds imageBounds = imageView.getBoundsInParent();

        // Constrain movement within the image bounds
        newX = Math.max(imageBounds.getMinX() + circleRadius,
                Math.min(imageBounds.getMaxX() - circleRadius, newX));
        newY = Math.max(imageBounds.getMinY() + circleRadius,
                Math.min(imageBounds.getMaxY() - circleRadius, newY));

        cropCircle.setLayoutX(newX);
        cropCircle.setLayoutY(newY);
        e.consume();
    }

    private void handleMouseReleased(MouseEvent e) {
        isDragging = false;
        e.consume();
    }

    private void setupButtons() {
        confirmButton.getStyleClass().add("button-primary");
        cancelButton.getStyleClass().add("button-secondary");

        confirmButton.setOnAction(e -> {
            createCroppedImage();
            ((Stage) confirmButton.getScene().getWindow()).close();
        });

        cancelButton.setOnAction(e -> {
            croppedImage = null;
            ((Stage) cancelButton.getScene().getWindow()).close();
        });
    }

    public void setImage(Image image) {
        this.originalImage = image;
        imageView.setImage(image);

        // Calculate scale to fit the image in the container
        double containerWidth = imageContainer.getPrefWidth();
        double containerHeight = imageContainer.getPrefHeight();

        double scaleX = containerWidth / image.getWidth();
        double scaleY = containerHeight / image.getHeight();
        imageScale = Math.min(scaleX, scaleY);

        double scaledWidth = image.getWidth() * imageScale;
        double scaledHeight = image.getHeight() * imageScale;

        // Center the image
        imageViewX = (containerWidth - scaledWidth) / 2;
        imageViewY = (containerHeight - scaledHeight) / 2;

        imageView.setFitWidth(scaledWidth);
        imageView.setFitHeight(scaledHeight);
        imageView.setLayoutX(imageViewX);
        imageView.setLayoutY(imageViewY);

        // Position crop circle in center
        Platform.runLater(() -> {
            cropCircle.setLayoutX(imageViewX + scaledWidth / 2);
            cropCircle.setLayoutY(imageViewY + scaledHeight / 2);
            cropCircle.setVisible(true);
            cropCircle.toFront();
        });
    }

    private void createCroppedImage() {
        try {
            // Calculate the relative position of the crop circle within the scaled image
            double relativeX = (cropCircle.getLayoutX() - imageViewX - circleRadius) / imageScale;
            double relativeY = (cropCircle.getLayoutY() - imageViewY - circleRadius) / imageScale;

            // Create a circular clip
            Circle clip = new Circle(circleRadius);
            clip.setCenterX(circleRadius);
            clip.setCenterY(circleRadius);

            // Create an ImageView for the cropped region
            ImageView croppedView = new ImageView(originalImage);
            croppedView.setClip(clip);

            // Set the viewport to crop the image
            double size = circleRadius * 2;
            croppedView.setViewport(new Rectangle2D(
                    relativeX,
                    relativeY,
                    size / imageScale,
                    size / imageScale));

            croppedView.setFitWidth(size);
            croppedView.setFitHeight(size);

            // Create the final snapshot
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            croppedImage = croppedView.snapshot(params, null);
        } catch (Exception e) {
            System.out.println("Error creating cropped image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Image getCroppedImage() {
        return croppedImage;
    }
}