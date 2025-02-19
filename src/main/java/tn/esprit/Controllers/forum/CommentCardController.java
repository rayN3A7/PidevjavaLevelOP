package tn.esprit.Controllers.forum;

import tn.esprit.Models.Commentaire;
import tn.esprit.Services.CommentaireService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class CommentCardController {

    @FXML
    private Label commentAuthor;
    @FXML
    private Label commentContent;
    @FXML
    private Button upvoteButton;
    @FXML private Label votesLabel;
    @FXML
    private TextField editCommentField;
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

    private Commentaire commentaire;
    private QuestionDetailsController questionDetailsController;
    private CommentaireService commentaireService = new CommentaireService();

    public void init(Commentaire commentaire, QuestionDetailsController questionDetailsController) {
        this.questionDetailsController = questionDetailsController;
        this.commentaire = commentaire;
        commentAuthor.setText(commentaire.getUtilisateur().getNickname());
        commentContent.setText(commentaire.getContenu());

        // Handle upvote and downvote actions
        upvoteButton.setOnAction(e -> questionDetailsController.handleUpvoteC(commentaire, votesLabel, downvoteButton));
        downvoteButton.setOnAction(e -> questionDetailsController.handleDownvoteC(commentaire, votesLabel, downvoteButton));

        // Handle delete and update actions
        deleteButton.setOnAction(e -> questionDetailsController.deleteComment(commentaire));

        // Initially hide the edit field and save/cancel buttons
        editCommentField.setVisible(false);
        editButtonsBox.setVisible(false);

        // Set up button actions
        updateButton.setOnAction(event -> enableEditMode());
        saveButton.setOnAction(event -> saveUpdatedComment());
    }

    private void enableEditMode() {
        // Show the text field with current comment text
        editCommentField.setText(commentaire.getContenu());
        editCommentField.setVisible(true);
        editButtonsBox.setVisible(true);

        // Hide the label and update button
        commentContent.setVisible(false);
        updateButton.setVisible(false);
    }

    private void saveUpdatedComment() {
        String updatedContent = editCommentField.getText().trim();
        if (updatedContent.isEmpty()) {
            System.out.println("Comment cannot be empty!");
            return;
        }

        // Update comment object
        commentaire.setContenu(updatedContent);

        // Update in database
        commentaireService.update(commentaire);

        // Refresh UI
        commentContent.setText(updatedContent);
        commentContent.setVisible(true);
        updateButton.setVisible(true);
        editCommentField.setVisible(false);
        editButtonsBox.setVisible(false);
    }
}
