package tn.esprit.Services;



import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Commentaire;
import tn.esprit.Models.Question;
import tn.esprit.Models.Utilisateur;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService implements IService<Commentaire> {
    private static Connection connexion;

    // Default constructor; the connection is now managed by MaConnexion
    public CommentaireService() {
        connexion = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Commentaire commentaire) {
        Utilisateur utilisateur = commentaire.getUtilisateur();
        Question question = commentaire.getQuestion();
        Commentaire parentCommentaire = commentaire.getParent_commentaire_id();

        // Check if utilisateur is null before proceeding
        if (utilisateur == null) {
            throw new IllegalArgumentException("Utilisateur cannot be null when adding a comment.");
        }

        // Check if question is null before proceeding
        if (question == null) {
            throw new IllegalArgumentException("Question cannot be null when adding a comment.");
        }

        // Ajouter le commentaire
        String insertCommentaireQuery = "INSERT INTO Commentaire (contenu, Votes, creation_at, utilisateur_id, question_id, parent_commentaire_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(insertCommentaireQuery)) {
            ps.setString(1, commentaire.getContenu());
            ps.setInt(2, commentaire.getVotes());
            ps.setTimestamp(3, commentaire.getCreation_at());
            ps.setInt(4, utilisateur.getId()); // Safe to use utilisateur.getId() now
            ps.setInt(5, question.getQuestion_id());
            ps.setObject(6, parentCommentaire != null ? parentCommentaire.getCommentaire_id() : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add comment: " + e.getMessage(), e);
        }
    }

    @Override
    public Commentaire getOne(int id) {
        String query = "SELECT * FROM Commentaire WHERE Commentaire_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int commentaireId = rs.getInt("Commentaire_id");
                String contenu = rs.getString("contenu");
                int votes = rs.getInt("Votes");
                Timestamp creationAt = rs.getTimestamp("creation_at");
                int utilisateurId = rs.getInt("utilisateur_id");
                int questionId = rs.getInt("question_id");
                Integer parentCommentaireId = (Integer) rs.getObject("parent_commentaire_id");

                Utilisateur utilisateur = new UtilisateurService().getOne(utilisateurId);
                Question question = new QuestionService().getOne(questionId);
                Commentaire parentCommentaire = parentCommentaireId != null ? new CommentaireService().getOne(parentCommentaireId) : null;

                return new Commentaire(parentCommentaire, question, utilisateur, creationAt, votes, contenu, commentaireId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch comment: " + e.getMessage(), e);
        }
        return null; // No comment found with the given ID
    }

    @Override
    public List<Commentaire> getAll() {
        List<Commentaire> commentaireList = new ArrayList<>();
        String query = "SELECT * FROM Commentaire ORDER BY Commentaire_id DESC";
        try (Statement st = connexion.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                int commentaireId = rs.getInt("Commentaire_id");
                String contenu = rs.getString("contenu");
                int votes = rs.getInt("Votes");
                Timestamp creationAt = rs.getTimestamp("creation_at");
                int utilisateurId = rs.getInt("utilisateur_id");
                int questionId = rs.getInt("question_id");
                Integer parentCommentaireId = (Integer) rs.getObject("parent_commentaire_id");

                Utilisateur utilisateur = new UtilisateurService().getOne(utilisateurId);
                Question question = new QuestionService().getOne(questionId);
                Commentaire parentCommentaire = parentCommentaireId != null ? new CommentaireService().getOne(parentCommentaireId) : null;

                Commentaire commentaire = new Commentaire(parentCommentaire, question, utilisateur, creationAt, votes, contenu, commentaireId);
                commentaireList.add(commentaire);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch comments: " + e.getMessage(), e);
        }
        return commentaireList;
    }

    @Override
    public void update(Commentaire commentaire) {
        String query = "UPDATE Commentaire SET contenu = ?, Votes = ?, creation_at = ?, parent_commentaire_id = ? WHERE Commentaire_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, commentaire.getContenu());
            ps.setInt(2, commentaire.getVotes());
            ps.setTimestamp(3, commentaire.getCreation_at());
            ps.setObject(4, commentaire.getParent_commentaire_id() != null ? commentaire.getParent_commentaire_id().getCommentaire_id() : null);
            ps.setInt(5, commentaire.getCommentaire_id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update comment: " + e.getMessage(), e);
        }
    }

    // Method to delete a comment and all its replies (recursive)
    public void delete(Commentaire commentaire) {
        try {
            // First, delete all replies related to this comment (if it has any)
            deleteReplies(commentaire.getCommentaire_id());

            // Now, delete the comment itself
            String deleteCommentQuery = "DELETE FROM Commentaire WHERE Commentaire_id = ?";
            try (PreparedStatement ps = connexion.prepareStatement(deleteCommentQuery)) {
                ps.setInt(1, commentaire.getCommentaire_id());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete comment: " + e.getMessage(), e);
        }
    }

    // Recursive method to delete replies to the given comment
    private void deleteReplies(int parentId) {
        try {
            // Select all replies to this comment (comments with parent_commentaire_id pointing to the parent)
            String selectRepliesQuery = "SELECT Commentaire_id FROM Commentaire WHERE parent_commentaire_id = ?";
            try (PreparedStatement ps = connexion.prepareStatement(selectRepliesQuery)) {
                ps.setInt(1, parentId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int replyId = rs.getInt("Commentaire_id");
                    // Recursively delete all replies to this comment
                    deleteReplies(replyId);
                }
            }

            // Delete the comment itself (this is a reply to some other comment)
            String deleteReplyQuery = "DELETE FROM Commentaire WHERE Commentaire_id = ?";
            try (PreparedStatement ps = connexion.prepareStatement(deleteReplyQuery)) {
                ps.setInt(1, parentId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete replies: " + e.getMessage(), e);
        }
    }

  /*  @Override
    public Commentaire getByName(String name) {
        // This method is not implemented
        throw new UnsupportedOperationException("getByName is not supported for Commentaire.");
    }*/
}