package tn.esprit.Services;


import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Games;
import tn.esprit.Models.Question;
import tn.esprit.Models.Utilisateur;
import tn.esprit.utils.MyDatabase;
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
    private UtilisateurService us =new UtilisateurService();
    private int userId = SessionManager.getInstance().getUserId();
    public QuestionService() {
        connexion = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Question question) {
        Utilisateur user = question.getUser();
        Games game = question.getGame();

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null when adding a question.");
        }
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null when adding a question.");
        }
        try {
            connexion.setAutoCommit(false);

            String query = "INSERT INTO Questions (title, content, game_id, Utilisateur_id, Votes) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement st = connexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, question.getTitle());
                st.setString(2, question.getContent());
                st.setInt(3, game.getGame_id());
                st.setInt(4, user.getId());
                st.setInt(5, question.getVotes());

                int affectedRows = st.executeUpdate();
                if (affectedRows == 0) {
                    throw new RuntimeException("Failed to insert question, no rows affected.");
                }

                try (ResultSet generatedKeys = st.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int questionId = generatedKeys.getInt(1);
                        question.setQuestion_id(questionId);
                        System.out.println("Question ajoutée avec ID: " + questionId);
                    } else {
                        throw new RuntimeException("Failed to insert question, no ID generated.");
                    }
                }
            }

            connexion.commit();
        } catch (SQLException e) {
            if (connexion != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    connexion.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error during rollback: " + ex.getMessage());
                }
            }
            throw new RuntimeException("Failed to add question: " + e.getMessage(), e);
        } finally {
            if (connexion != null) {
                try {
                    connexion.setAutoCommit(true);
                } catch (SQLException ex) {
                    System.err.println("Error restoring auto-commit mode: " + ex.getMessage());
                }
            }
        }
    }

    public void upvoteQuestion(int questionId) {
        String query = "UPDATE Questions SET Votes = Votes + 1 WHERE question_id = ? ";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, questionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to upvote question: " + e.getMessage(), e);
        }
    }

    public void downvoteQuestion(int questionId) {
        String query = "UPDATE Questions SET Votes = Votes - 1 WHERE question_id = ? AND Votes > 0";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, questionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to downvote question: " + e.getMessage(), e);
        }
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



    // Update getOne to include the user's reaction
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

                Games game = new GamesService().getOne(gameId);
                Utilisateur user = new UtilisateurService().getOne(userId);
                Question question = new Question(questionId, title, content, votes, game, user);
                question.setReactions(getReactions(questionId)); // Set all reactions
                Utilisateur utilisateur = us.getOne(userId);
                question.setUserReaction(getUserReaction(questionId, userId));
                return question;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch question: " + e.getMessage(), e);
        }
        return null;
    }

    // Update getAll to include the user's reaction
    @Override
    public List<Question> getAll() {
        List<Question> questionList = new ArrayList<>();
        String query = "SELECT * FROM Questions ORDER BY question_id DESC";
        try (Statement st = connexion.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("question_id");
                String title = rs.getString("title");
                String content = rs.getString("content");
                int votes = rs.getInt("Votes");
                int gameId = rs.getInt("game_id");
                int userId = rs.getInt("Utilisateur_id");

                Games game = new GamesService().getOne(gameId);
                Utilisateur user = new UtilisateurService().getOne(userId);
                Question question = new Question(id, title, content, votes, game, user);
                question.setReactions(getReactions(id)); // Set all reactions
                Utilisateur utilisateur = us.getOne(userId);
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
        String query = "UPDATE Questions SET title = ?, content = ?, Votes = ? WHERE question_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, question.getTitle());
            ps.setString(2, question.getContent());
            ps.setInt(3, question.getVotes());
            ps.setInt(4, question.getQuestion_id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update question: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Question question) {
        try {
            deleteRepliesForQuestion(question);
            deleteCommentsForQuestion(question);

            String deleteQuestionQuery = "DELETE FROM Questions WHERE question_id = ?";
            try (PreparedStatement ps = connexion.prepareStatement(deleteQuestionQuery)) {
                ps.setInt(1, question.getQuestion_id());
                ps.executeUpdate();
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
        // Check if the user already has a reaction
        String existingReaction = getUserReaction(questionId, userId);
        if (existingReaction != null) {
            removeReaction(questionId, userId); // Remove the existing reaction
            // Update the reaction count (decrement for the old reaction)
            updateReactionCount(questionId, existingReaction, -1);
        }

        // Add the new reaction
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

}