package tn.esprit.Services;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Games;
import tn.esprit.Models.Question;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.utils.MyDatabase;
import tn.esprit.utils.PrivilegeEvent;
import tn.esprit.utils.SessionManager;
import tn.esprit.Services.UtilisateurService;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuestionService implements IService<Question> {
    private static Connection connexion;
    private UtilisateurService us = new UtilisateurService();
    private int userId = SessionManager.getInstance().getUserId();
    private Node eventTarget; // To fire events, set via setter
    public QuestionService() {
        connexion = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Question question) {
        Utilisateur user = question.getUser();
        Games game = question.getGame();

        if (user == null || user.getId() <= 0) throw new IllegalArgumentException("Invalid user.");
        if (game == null || game.getGame_id() <= 0) throw new IllegalArgumentException("Invalid game.");
        if (question.getTitle() == null || question.getTitle().trim().isEmpty()) throw new IllegalArgumentException("Title required.");
        if (question.getContent() == null || question.getContent().trim().isEmpty()) throw new IllegalArgumentException("Content required.");

        try {
            connexion.setAutoCommit(false);
            String query = "INSERT INTO Questions (title, content, game_id, Utilisateur_id, Votes, media_path, media_type) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement st = connexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, question.getTitle());
                st.setString(2, question.getContent());
                st.setInt(3, game.getGame_id());
                st.setInt(4, user.getId());
                st.setInt(5, question.getVotes());
                st.setString(6, question.getMediaPath());
                st.setString(7, question.getMediaType());
                int affectedRows = st.executeUpdate();
                if (affectedRows == 0) throw new RuntimeException("Failed to insert question.");
                try (ResultSet generatedKeys = st.getGeneratedKeys()) {
                    if (generatedKeys.next()) question.setQuestion_id(generatedKeys.getInt(1));
                }
            }
            connexion.commit();
            us.updateUserPrivilege(user.getId()); // Triggers event via EventBus
        } catch (SQLException e) {
            try {
                if (connexion != null) connexion.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback error: " + ex.getMessage());
            }
            throw new RuntimeException("Failed to add question: " + e.getMessage(), e);
        } finally {
            try {
                if (connexion != null) connexion.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("Auto-commit restore error: " + ex.getMessage());
            }
        }
    }

    public void upvoteQuestion(int questionId) {
        int userId = SessionManager.getInstance().getUserId();
        Question question = getOne(questionId);
        if (question == null) throw new RuntimeException("Question not found: " + questionId);

        String currentVote = getUserVote(questionId, userId);
        if ("UP".equals(currentVote)) return;

        String updateVoteQuery = "INSERT INTO question_votes (question_id, user_id, vote_type) VALUES (?, ?, 'UP') " +
                "ON DUPLICATE KEY UPDATE vote_type = CASE WHEN vote_type = 'DOWN' THEN 'UP' WHEN vote_type = 'NONE' THEN 'UP' ELSE 'NONE' END";
        String updateVotesQuery = "DOWN".equals(currentVote) ?
                "UPDATE Questions SET Votes = Votes + 1 WHERE question_id = ?" :
                "UPDATE Questions SET Votes = Votes + 1 WHERE question_id = ?";

        try {
            try (PreparedStatement ps = connexion.prepareStatement(updateVoteQuery)) {
                ps.setInt(1, questionId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connexion.prepareStatement(updateVotesQuery)) {
                ps.setInt(1, questionId);
                ps.executeUpdate();
            }
            us.updateUserPrivilege(question.getUser().getId()); // Owner
            us.updateUserPrivilege(userId); // Voter
        } catch (SQLException e) {
            throw new RuntimeException("Failed to upvote question: " + e.getMessage(), e);
        }
    }

    public void downvoteQuestion(int questionId) {
        int userId = SessionManager.getInstance().getUserId();
        Question question = getOne(questionId);
        if (question == null || question.getVotes() <= 0) return;

        String currentVote = getUserVote(questionId, userId);
        if ("DOWN".equals(currentVote)) return;

        String updateVoteQuery = "INSERT INTO question_votes (question_id, user_id, vote_type) VALUES (?, ?, 'DOWN') " +
                "ON DUPLICATE KEY UPDATE vote_type = CASE WHEN vote_type = 'UP' THEN 'DOWN' WHEN vote_type = 'NONE' THEN 'DOWN' ELSE 'NONE' END";
        String updateVotesQuery = "UP".equals(currentVote) ?
                "UPDATE Questions SET Votes = Votes - 1 WHERE question_id = ?" :
                "UPDATE Questions SET Votes = Votes - 1 WHERE question_id = ? AND Votes > 0";

        try {
            try (PreparedStatement ps = connexion.prepareStatement(updateVoteQuery)) {
                ps.setInt(1, questionId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connexion.prepareStatement(updateVotesQuery)) {
                ps.setInt(1, questionId);
                ps.executeUpdate();
            }
            us.updateUserPrivilege(question.getUser().getId()); // Owner
            us.updateUserPrivilege(userId); // Voter
        } catch (SQLException e) {
            throw new RuntimeException("Failed to downvote question: " + e.getMessage(), e);
        }
    }

    private void updateVoteInDb(int questionId, int userId, String voteType) throws SQLException {
        String query = "INSERT INTO question_votes (question_id, user_id, vote_type) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE vote_type = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, questionId);
            ps.setInt(2, userId);
            ps.setString(3, voteType);
            ps.setString(4, voteType);
            ps.executeUpdate();
        }
    }

    public String getUserVote(int questionId, int userId) {
        String query = "SELECT vote_type FROM question_votes WHERE question_id = ? AND user_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, questionId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("vote_type");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get user vote: " + e.getMessage(), e);
        }
        return "NONE"; // Default to no vote
    }

    public int getVotes(int questionId) {
        String query = "SELECT Votes FROM Questions WHERE question_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, questionId);
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
    public Question getOne(int id) {
        String query = "SELECT * FROM Questions WHERE question_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int questionId = rs.getInt("question_id");
                String title = rs.getString("title");
                String content = rs.getString("content");
                int votes = rs.getInt("Votes");
                int gameId = rs.getInt("game_id");
                int userId = rs.getInt("utilisateur_id");
                String mediaPath = rs.getString("media_path");
                String mediaType = rs.getString("media_type");

                Games game = new GamesService().getOne(gameId);
                Utilisateur user = new UtilisateurService().getOne(userId);
                Question question = new Question(questionId, title, content, votes, game, user);
                question.setMediaPath(mediaPath);
                question.setMediaType(mediaType);
                question.setReactions(getReactions(questionId));
                question.setUserReaction(getUserReaction(questionId, userId));
                return question;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch question: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Question> getAll() {
        List<Question> questionList = new ArrayList<>();
        String query = "SELECT * FROM Questions ORDER BY question_id DESC";
        try (Statement st = connexion.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("question_id");
                String title = rs.getString("title");
                String content = rs.getString("content");
                int votes = rs.getInt("Votes");
                int gameId = rs.getInt("game_id");
                int userId = rs.getInt("Utilisateur_id");
                String mediaPath = rs.getString("media_path");
                String mediaType = rs.getString("media_type");

                Games game = new GamesService().getOne(gameId);
                Utilisateur user = new UtilisateurService().getOne(userId);
                Question question = new Question(id, title, content, votes, game, user);
                question.setMediaPath(mediaPath);
                question.setMediaType(mediaType);
                question.setReactions(getReactions(id));
                question.setUserReaction(getUserReaction(id, userId));
                questionList.add(question);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch questions: " + e.getMessage(), e);
        }
        return questionList;
    }

    @Override
    public void update(Question question) {

    }

    @Override
    public void delete(Question question) {

    }

    public void update(Question question, int userId) {
        if (!hasPermissionForQuestion(question.getQuestion_id(), userId, "UPDATE")) {
            throw new SecurityException("Vous n'avez pas la permission de modifier cette question.");
        }

        String query = "UPDATE Questions SET title = ?, content = ?, game_id = ?, Votes = ?, media_path = ?, media_type = ? WHERE question_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, question.getTitle());
            ps.setString(2, question.getContent());
            ps.setInt(3, question.getGame().getGame_id()); // Update the game_id
            ps.setInt(4, question.getVotes());
            ps.setString(5, question.getMediaPath());
            ps.setString(6, question.getMediaType());
            ps.setInt(7, question.getQuestion_id());
            ps.executeUpdate();

            // Trigger privilege update for the user who updated the question
            us.updateUserPrivilege(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update question: " + e.getMessage(), e);
        }
    }

    public void delete(int questionId, int userId) {
        if (!hasPermissionForQuestion(questionId, userId, "DELETE")) {
            throw new SecurityException("Vous n'avez pas la permission de supprimer cette question.");
        }

        try {
            deleteRepliesForQuestion(new Question(questionId, "", "", 0, null, null)); // Dummy question for deletion
            deleteCommentsForQuestion(new Question(questionId, "", "", 0, null, null)); // Dummy question for deletion

            String deleteQuestionQuery = "DELETE FROM Questions WHERE question_id = ?";
            try (PreparedStatement ps = connexion.prepareStatement(deleteQuestionQuery)) {
                ps.setInt(1, questionId);
                ps.executeUpdate();
            }

            // Trigger privilege update for the user who deleted the question
            Question question = getOne(questionId);
            if (question != null) {
                us.updateUserPrivilege(question.getUser().getId());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete question: " + e.getMessage(), e);
        }
    }

    private void deleteRepliesForQuestion(Question question) {
        try {
            String selectRepliesQuery = "SELECT Commentaire_id FROM Commentaire WHERE question_id = ? AND parent_commentaire_id IS NOT NULL";
            try (PreparedStatement ps = connexion.prepareStatement(selectRepliesQuery)) {
                ps.setInt(1, question.getQuestion_id());
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int replyId = rs.getInt("Commentaire_id");
                    deleteReplies(replyId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete replies for question: " + e.getMessage(), e);
        }
    }

    private void deleteReplies(int parentId) {
        try {
            String selectRepliesQuery = "SELECT Commentaire_id FROM Commentaire WHERE parent_commentaire_id = ?";
            try (PreparedStatement ps = connexion.prepareStatement(selectRepliesQuery)) {
                ps.setInt(1, parentId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int replyId = rs.getInt("Commentaire_id");
                    deleteReplies(replyId);
                }
            }

            String deleteReplyQuery = "DELETE FROM Commentaire WHERE Commentaire_id = ?";
            try (PreparedStatement ps = connexion.prepareStatement(deleteReplyQuery)) {
                ps.setInt(1, parentId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete replies: " + e.getMessage(), e);
        }
    }

    private void deleteCommentsForQuestion(Question question) {
        try {
            String deleteCommentsQuery = "DELETE FROM Commentaire WHERE question_id = ?";
            try (PreparedStatement ps = connexion.prepareStatement(deleteCommentsQuery)) {
                ps.setInt(1, question.getQuestion_id());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete comments for question: " + e.getMessage(), e);
        }
    }

    public List<Question> getQuestionsByGameName(String gameName) {
        if (gameName.isEmpty()) {
            return getAll();
        }
        System.out.println("Fetching questions with game name: " + gameName);
        List<Question> allQuestions = getAll();
        List<Question> filteredQuestions = allQuestions.stream()
                .filter(question -> question.getGame().getGame_name().toLowerCase().contains(gameName.toLowerCase()))
                .collect(Collectors.toList());
        System.out.println("Filtered questions: " + filteredQuestions.size());
        return filteredQuestions;
    }

    public void addReaction(int questionId, int userId, String emoji) {
        String existingReaction = getUserReaction(questionId, userId);
        if (existingReaction != null) {
            removeReaction(questionId, userId);
            updateReactionCount(questionId, existingReaction, -1);
        }

        String query = "INSERT INTO question_reactions (question_id, user_id, emoji) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE emoji = VALUES(emoji)";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, questionId);
            ps.setInt(2, userId);
            ps.setString(3, emoji);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add reaction: " + e.getMessage(), e);
        }

        updateReactionCount(questionId, emoji, 1);
    }

    private void updateReactionCount(int questionId, String emoji, int delta) {
        Map<String, Integer> reactions = getReactions(questionId);
        reactions.put(emoji, reactions.getOrDefault(emoji, 0) + delta);
    }

    public void removeReaction(int questionId, int userId) {
        String query = "DELETE FROM question_reactions WHERE question_id = ? AND user_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, questionId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove user reaction: " + e.getMessage(), e);
        }
    }

    public Map<String, Integer> getReactions(int questionId) {
        Map<String, Integer> reactionCounts = new HashMap<>();
        String query = "SELECT emoji, COUNT(*) as count FROM question_reactions WHERE question_id = ? GROUP BY emoji";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, questionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String emoji = rs.getString("emoji");
                int count = rs.getInt("count");
                reactionCounts.put(emoji, count);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch reactions: " + e.getMessage(), e);
        }
        return reactionCounts;
    }

    public String getUserReaction(int questionId, int userId) {
        String query = "SELECT emoji FROM question_reactions WHERE question_id = ? AND user_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, questionId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("emoji");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user reaction: " + e.getMessage(), e);
        }
        return null;
    }

    private boolean hasPermissionForQuestion(int questionId, int userId, String action) {
        Utilisateur currentUser = us.getOne(userId);
        if (currentUser == null) return false;

        if (currentUser.getRole() == Role.ADMIN) {
            return true; // Admin can perform any action on questions
        }

        // For non-admin (CLIENT or COACH), check ownership
        Question question = getOne(questionId);
        if (question == null) return false;

        return question.getUser().getId() == userId; // Client/Coach can only modify their own questions
    }
}