package tn.esprit.Services;

import tn.esprit.Models.Review;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Models.Produit;
import tn.esprit.Models.Commande;
import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewService {
    private Connection connection;
    private UtilisateurService utilisateurService;
    private ProduitService produitService;
    private CommandeService commandeService;

    public ReviewService() {
        connection = MyDatabase.getInstance().getCnx();
        utilisateurService = new UtilisateurService();
        produitService = new ProduitService();
        commandeService = new CommandeService();
    }

    public boolean add(Review review) {
        String sql = "INSERT INTO review (utilisateur_id, produit_id, comment, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, review.getUtilisateur().getId());
            statement.setInt(2, review.getProduit().getId());
            statement.setString(3, review.getComment());
            statement.setTimestamp(4, Timestamp.valueOf(review.getCreatedAt()));

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    review.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error adding review: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Review review) {
        String sql = "UPDATE review SET comment = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, review.getComment());
            statement.setInt(2, review.getId());

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.out.println("Error updating review: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(Review review) {
        return delete(review.getId());
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM review WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting review: " + e.getMessage());
        }
        return false;
    }

    public Review getById(int id) {
        String sql = "SELECT * FROM review WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return mapResultSetToReview(result);
            }
        } catch (SQLException e) {
            System.out.println("Error getting review: " + e.getMessage());
        }
        return null;
    }

    public List<Review> getAll() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM review ORDER BY created_at DESC";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                Review review = mapResultSetToReview(result);
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.out.println("Error getting all reviews: " + e.getMessage());
        }
        return reviews;
    }

    public List<Review> getByProduitId(int produitId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM review WHERE produit_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, produitId);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                Review review = mapResultSetToReview(result);
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.out.println("Error getting reviews by product ID: " + e.getMessage());
        }
        return reviews;
    }

    public List<Review> getByUtilisateurId(int utilisateurId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM review WHERE utilisateur_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateurId);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                Review review = mapResultSetToReview(result);
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.out.println("Error getting reviews by user ID: " + e.getMessage());
        }
        return reviews;
    }

    public boolean hasUserPurchasedProduct(int utilisateurId, int produitId) {
        String sql = "SELECT COUNT(*) FROM commande WHERE utilisateur_id = ? AND produit_id = ? AND status = 'terminé'";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateurId);
            statement.setInt(2, produitId);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking if user purchased product: " + e.getMessage());
        }
        return false;
    }

    public boolean hasUserReviewedProduct(int utilisateurId, int produitId) {
        String sql = "SELECT COUNT(*) FROM review WHERE utilisateur_id = ? AND produit_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateurId);
            statement.setInt(2, produitId);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking if user reviewed product: " + e.getMessage());
        }
        return false;
    }

    private Review mapResultSetToReview(ResultSet result) throws SQLException {
        int id = result.getInt("id");
        int utilisateurId = result.getInt("utilisateur_id");
        int produitId = result.getInt("produit_id");
        String comment = result.getString("comment");
        LocalDateTime createdAt = result.getTimestamp("created_at").toLocalDateTime();

        Utilisateur utilisateur = utilisateurService.getOne(utilisateurId);
        Produit produit = produitService.getOne(produitId);

        Review review = new Review(id, utilisateur, produit, comment, createdAt);
        return review;
    }
} 