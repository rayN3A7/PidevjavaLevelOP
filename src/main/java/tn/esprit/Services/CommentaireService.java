package tn.esprit.Services;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Commentaire;
import tn.esprit.Models.Question;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.utils.MyDatabase;
import tn.esprit.utils.PrivilegeEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentaireService implements IService<Commentaire> {
    private static Connection connexion;
    private Node eventTarget;
    private final UtilisateurService us = new UtilisateurService();
    public CommentaireService() {
        connexion = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Commentaire commentaire) {
        Utilisateur utilisateur = commentaire.getUtilisateur();
        Question question = commentaire.getQuestion();
        Commentaire parentCommentaire = commentaire.getParent_commentaire_id();

        if (utilisateur == null) throw new IllegalArgumentException("Utilisateur cannot be null.");
        if (question == null && parentCommentaire == null) throw new IllegalArgumentException("Question or Parent Comment must be provided.");

        String query = "INSERT INTO Commentaire (contenu, Votes, creation_at, utilisateur_id, question_id, parent_commentaire_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, commentaire.getContenu());
            ps.setInt(2, commentaire.getVotes());
            ps.setTimestamp(3, commentaire.getCreation_at());
            ps.setInt(4, utilisateur.getId());
            ps.setObject(5, question != null ? question.getQuestion_id() : null);
            ps.setObject(6, parentCommentaire != null ? parentCommentaire.getCommentaire_id() : null);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) commentaire.setCommentaire_id(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add comment: " + e.getMessage(), e);
        }
        UtilisateurService us = new UtilisateurService();
        us.updateUserPrivilege(commentaire.getUtilisateur().getId());
    }
    public void setEventTarget(Node eventTarget) {
        this.eventTarget = eventTarget;
        us.setEventTarget(eventTarget); // Propagate to UtilisateurService
    }

    public void upvoteComment(int commentaireId, int userId) {
        String checkVoteQuery = "SELECT vote_type FROM commentaire_votes WHERE commentaire_id = ? AND user_id = ?";
        String updateVoteQuery = "INSERT INTO commentaire_votes (commentaire_id, user_id, vote_type) VALUES (?, ?, 'UP') " +
                "ON DUPLICATE KEY UPDATE vote_type = CASE " +
                "WHEN vote_type = 'DOWN' THEN 'UP' " +
                "WHEN vote_type = 'NONE' THEN 'UP' " +
                "ELSE 'NONE' END";
        String updateVotesQuery = "UPDATE Commentaire SET Votes = Votes + 1 WHERE Commentaire_id = ?";

        try {
            Commentaire commentaire = getOne(commentaireId);
            if (commentaire == null) {
                throw new RuntimeException("Comment not found for ID: " + commentaireId);
            }

            // Check current vote
            String currentVote = getUserVote(commentaireId, userId);
            if ("UP".equals(currentVote)) {
                // User already upvoted, no action needed
                return;
            } else if ("DOWN".equals(currentVote)) {
                // User previously downvoted, switch to upvote and adjust votes
                updateVoteInDb(commentaireId, userId, "UP");
                updateVotesQuery = "UPDATE Commentaire SET Votes = Votes + 1 WHERE Commentaire_id = ?"; // +2 to undo downvote (-1) and add upvote (+1)
            } else {
                // No vote or NONE, just upvote
                updateVoteInDb(commentaireId, userId, "UP");
            }

            try (PreparedStatement ps = connexion.prepareStatement(updateVotesQuery)) {
                ps.setInt(1, commentaireId);
                ps.executeUpdate();
            }

            // Trigger privilege update for the comment owner after voting
            UtilisateurService.PrivilegeChange ownerChange = us.updateUserPrivilege(commentaire.getUtilisateur().getId());
            if (ownerChange.isChanged() && eventTarget != null) {
                Platform.runLater(() -> {
                    PrivilegeEvent event = new PrivilegeEvent(commentaire.getUtilisateur().getId(), ownerChange.getNewPrivilege());
                    Event.fireEvent(eventTarget, event);
                });
            }

            // Trigger privilege update for the voter (current user)
            UtilisateurService.PrivilegeChange voterChange = us.updateUserPrivilege(userId);
            if (voterChange.isChanged() && eventTarget != null) {
                Platform.runLater(() -> {
                    PrivilegeEvent event = new PrivilegeEvent(userId, voterChange.getNewPrivilege());
                    Event.fireEvent(eventTarget, event);
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to upvote comment: " + e.getMessage(), e);
        }
    }

    public void downvoteComment(int commentaireId, int userId) {
        String checkVoteQuery = "SELECT vote_type FROM commentaire_votes WHERE commentaire_id = ? AND user_id = ?";
        String updateVoteQuery = "INSERT INTO commentaire_votes (commentaire_id, user_id, vote_type) VALUES (?, ?, 'DOWN') " +
                "ON DUPLICATE KEY UPDATE vote_type = CASE " +
                "WHEN vote_type = 'UP' THEN 'DOWN' " +
                "WHEN vote_type = 'NONE' THEN 'DOWN' " +
                "ELSE 'NONE' END";
        String updateVotesQuery = "UPDATE Commentaire SET Votes = Votes - 1 WHERE Commentaire_id = ? AND Votes > 0";

        try {
            Commentaire commentaire = getOne(commentaireId);
            if (commentaire == null) {
                throw new RuntimeException("Comment not found for ID: " + commentaireId);
            }

            String currentVote = getUserVote(commentaireId, userId);
            if ("DOWN".equals(currentVote)) {
                return;
            } else if ("UP".equals(currentVote)) {
                updateVoteInDb(commentaireId, userId, "DOWN");
                updateVotesQuery = "UPDATE Commentaire SET Votes = Votes - 1 WHERE Commentaire_id = ?"; // -2 to undo upvote (+1) and add downvote (-1)
            } else {
                updateVoteInDb(commentaireId, userId, "DOWN");
            }

            try (PreparedStatement ps = connexion.prepareStatement(updateVotesQuery)) {
                ps.setInt(1, commentaireId);
                ps.executeUpdate();
            }

            UtilisateurService.PrivilegeChange ownerChange = us.updateUserPrivilege(commentaire.getUtilisateur().getId());
            if (ownerChange.isChanged() && eventTarget != null) {
                Platform.runLater(() -> {
                    PrivilegeEvent event = new PrivilegeEvent(commentaire.getUtilisateur().getId(), ownerChange.getNewPrivilege());
                    Event.fireEvent(eventTarget, event);
                });
            }

            // Trigger privilege update for the voter (current user)
            UtilisateurService.PrivilegeChange voterChange = us.updateUserPrivilege(userId);
            if (voterChange.isChanged() && eventTarget != null) {
                Platform.runLater(() -> {
                    PrivilegeEvent event = new PrivilegeEvent(userId, voterChange.getNewPrivilege());
                    Event.fireEvent(eventTarget, event);
                });
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to downvote comment: " + e.getMessage(), e);
        }
    }

    private void updateVoteInDb(int commentaireId, int userId, String voteType) throws SQLException {
        String query = "INSERT INTO commentaire_votes (commentaire_id, user_id, vote_type) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE vote_type = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, commentaireId);
            ps.setInt(2, userId);
            ps.setString(3, voteType);
            ps.setString(4, voteType);
            ps.executeUpdate();
        }
    }

    public String getUserVote(int commentaireId, int userId) {
        String query = "SELECT vote_type FROM commentaire_votes WHERE commentaire_id = ? AND user_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, commentaireId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("vote_type");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get user vote for comment: " + e.getMessage(), e);
        }
        return "NONE";
    }

    public int getVotes(int Commentaire_id) {
        String query = "SELECT Votes FROM Commentaire WHERE Commentaire_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, Commentaire_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("Votes");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get votes: " + e.getMessage(), e);
        }
        return 0;
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

                Commentaire commentaire = new Commentaire(parentCommentaire, question, utilisateur, creationAt, votes, contenu, commentaireId);
                commentaire.setReactions(getReactions(commentaireId)); // Load reactions
                commentaire.setUserReaction(getUserReaction(commentaireId, utilisateurId)); // Load user-specific reaction
                return commentaire;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch comment: " + e.getMessage(), e);
        }
        return null;
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
                commentaire.setReactions(getReactions(commentaireId)); // Load reactions
                commentaire.setUserReaction(getUserReaction(commentaireId, utilisateurId)); // Load user-specific reaction
                commentaireList.add(commentaire);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch comments: " + e.getMessage(), e);
        }
        return commentaireList;
    }

    @Override
    public void update(Commentaire commentaire) {

    }

    @Override
    public void delete(Commentaire commentaire) {

    }

    public void update(Commentaire commentaire, int userId) {
        if (!hasPermissionForComment(commentaire.getCommentaire_id(), userId, "UPDATE")) {
            throw new SecurityException("Vous n'avez pas la permission de modifier ce commentaire.");
        }

        String query = "UPDATE Commentaire SET contenu = ?, Votes = ?, creation_at = ?, parent_commentaire_id = ? WHERE Commentaire_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, commentaire.getContenu());
            ps.setInt(2, commentaire.getVotes());
            ps.setTimestamp(3, commentaire.getCreation_at());
            ps.setObject(4, commentaire.getParent_commentaire_id() != null ? commentaire.getParent_commentaire_id().getCommentaire_id() : null);
            ps.setInt(5, commentaire.getCommentaire_id());
            ps.executeUpdate();

            // Trigger privilege update for the user who updated the comment
            UtilisateurService us = new UtilisateurService();
            us.updateUserPrivilege(userId);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update comment: " + e.getMessage(), e);
        }
    }

    public void delete(int commentaireId, int userId) {
        if (!hasPermissionForComment(commentaireId, userId, "DELETE")) {
            throw new SecurityException("Vous n'avez pas la permission de supprimer ce commentaire.");
        }
        try {
            deleteReplies(commentaireId); // Delete all replies recursively
            String deleteQuery = "DELETE FROM Commentaire WHERE Commentaire_id = ?";
            try (PreparedStatement ps = connexion.prepareStatement(deleteQuery)) {
                ps.setInt(1, commentaireId);
                ps.executeUpdate();
            }

            // Trigger privilege update for the user who deleted the comment
            Commentaire commentaire = getOne(commentaireId);
            if (commentaire != null) {
                UtilisateurService us = new UtilisateurService();
                us.updateUserPrivilege(commentaire.getUtilisateur().getId());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete comment: " + e.getMessage(), e);
        }
    }
    private Commentaire extractCommentaireFromResultSet(ResultSet rs) throws SQLException {
        int commentaireId = rs.getInt("Commentaire_id");
        String contenu = rs.getString("contenu");
        int votes = rs.getInt("Votes");
        Timestamp creationAt = rs.getTimestamp("creation_at");
        int utilisateurId = rs.getInt("utilisateur_id");
        Integer questionId = (Integer) rs.getObject("question_id");
        Integer parentCommentaireId = (Integer) rs.getObject("parent_commentaire_id");

        Utilisateur utilisateur = new UtilisateurService().getOne(utilisateurId);
        Question question = questionId != null ? new QuestionService().getOne(questionId) : null;
        Commentaire parentCommentaire = parentCommentaireId != null ? getOne(parentCommentaireId) : null;

        Commentaire commentaire = new Commentaire(parentCommentaire, question, utilisateur, creationAt, votes, contenu, commentaireId);
        commentaire.setReactions(getReactions(commentaireId));
        commentaire.setUserReaction(getUserReaction(commentaireId, utilisateurId));
        return commentaire;
    }
    private void deleteReplies(int parentId) throws SQLException {
        String selectRepliesQuery = "SELECT Commentaire_id FROM Commentaire WHERE parent_commentaire_id = ?";
        List<Integer> replyIds = new ArrayList<>();
        try (PreparedStatement ps = connexion.prepareStatement(selectRepliesQuery)) {
            ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                replyIds.add(rs.getInt("Commentaire_id"));
            }
        }
        for (int replyId : replyIds) {
            deleteReplies(replyId); // Recursive deletion
            String deleteReplyQuery = "DELETE FROM Commentaire WHERE Commentaire_id = ?";
            try (PreparedStatement ps = connexion.prepareStatement(deleteReplyQuery)) {
                ps.setInt(1, replyId);
                ps.executeUpdate();
            }
        }
    }

    public List<Commentaire> getReplies(int parentCommentaireId) {
        List<Commentaire> replies = new ArrayList<>();
        String query = "SELECT * FROM Commentaire WHERE parent_commentaire_id = ? ORDER BY creation_at ASC";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, parentCommentaireId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                replies.add(extractCommentaireFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch replies: " + e.getMessage(), e);
        }
        return replies;
    }

    // Reaction methods for comments
    public void addReaction(int commentaireId, int userId, String emoji) {
        String existingReaction = getUserReaction(commentaireId, userId);
        if (existingReaction != null) {
            removeReaction(commentaireId, userId);
            updateReactionCount(commentaireId, existingReaction, -1);
        }

        String query = "INSERT INTO commentaire_reactions (commentaire_id, user_id, emoji) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE emoji = VALUES(emoji)";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, commentaireId);
            ps.setInt(2, userId);
            ps.setString(3, emoji);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add reaction to comment: " + e.getMessage(), e);
        }

        updateReactionCount(commentaireId, emoji, 1);
    }

    private void updateReactionCount(int commentaireId, String emoji, int delta) {
        Map<String, Integer> reactions = getReactions(commentaireId);
        reactions.put(emoji, reactions.getOrDefault(emoji, 0) + delta);
    }

    public void removeReaction(int commentaireId, int userId) {
        String query = "DELETE FROM commentaire_reactions WHERE commentaire_id = ? AND user_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, commentaireId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove user reaction from comment: " + e.getMessage(), e);
        }
    }

    public Map<String, Integer> getReactions(int commentaireId) {
        Map<String, Integer> reactionCounts = new HashMap<>();
        String query = "SELECT emoji, COUNT(*) as count FROM commentaire_reactions WHERE commentaire_id = ? GROUP BY emoji";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, commentaireId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String emoji = rs.getString("emoji");
                int count = rs.getInt("count");
                reactionCounts.put(emoji, count);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch reactions for comment: " + e.getMessage(), e);
        }
        return reactionCounts;
    }

    public String getUserReaction(int commentaireId, int userId) {
        String query = "SELECT emoji FROM commentaire_reactions WHERE commentaire_id = ? AND user_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, commentaireId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("emoji");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user reaction for comment: " + e.getMessage(), e);
        }
        return null;
    }

    private boolean hasPermissionForComment(int commentaireId, int userId, String action) {
        UtilisateurService us = new UtilisateurService();
        Utilisateur currentUser = us.getOne(userId);
        if (currentUser == null) return false;

        if (currentUser.getRole() == Role.ADMIN) {
            return true; // Admin can perform any action on comments
        }

        // For non-admin (CLIENT or COACH), check ownership
        Commentaire commentaire = getOne(commentaireId);
        if (commentaire == null) return false;

        return commentaire.getUtilisateur().getId() == userId; // Client/Coach can only modify their own comments
    }
}