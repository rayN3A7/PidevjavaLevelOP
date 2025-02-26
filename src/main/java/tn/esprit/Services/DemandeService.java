package tn.esprit.Services;

import tn.esprit.Models.Demande;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DemandeService {

    private Connection cnx;

    public DemandeService() {
        cnx = MyDatabase.getInstance().getCnx(); // Initialize the database connection
    }

    // Add a new demande with file content
    public void add(Demande demande) {
        String query = "INSERT INTO demande (userId, game, description, file, date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, demande.getUserId());
            stmt.setString(2, demande.getGame());
            stmt.setString(3, demande.getDescription());
            stmt.setBytes(4, demande.getFile()); // Set file content as bytes
            stmt.setTimestamp(5, demande.getDate());
            stmt.executeUpdate();
            System.out.println("Demande added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding demande: " + e.getMessage());
        }
    }

    // Retrieve all demandes
    public List<Demande> getAll() {
        List<Demande> demandes = new ArrayList<>();
        String query = "SELECT * FROM demande";
        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Demande demande = new Demande(
                        rs.getInt("id"),
                        rs.getInt("userId"),
                        rs.getString("game"),
                        rs.getString("description"),
                        rs.getBytes("file"), // Retrieve file content as bytes
                        rs.getTimestamp("date")
                );
                demandes.add(demande);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving demandes: " + e.getMessage());
        }
        return demandes;
    }

    // Update a demande
    public void update(Demande demande) {
        String query = "UPDATE demande SET userId = ?, game = ?, description = ?, file = ?, date = ? WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, demande.getUserId());
            stmt.setString(2, demande.getGame());
            stmt.setString(3, demande.getDescription());
            stmt.setBytes(4, demande.getFile()); // Update file content
            stmt.setTimestamp(5, demande.getDate());
            stmt.setInt(6, demande.getId());
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Demande updated successfully!");
            } else {
                System.out.println("No demande found with id: " + demande.getId());
            }
        } catch (SQLException e) {
            System.out.println("Error updating demande: " + e.getMessage());
        }
    }

    // Delete a demande
    public void delete(Demande demande) {
        String query = "DELETE FROM demande WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, demande.getId());
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Demande deleted successfully!");
            } else {
                System.out.println("No demande found with id: " + demande.getId());
            }
        } catch (SQLException e) {
            System.out.println("Error deleting demande: " + e.getMessage());
        }
    }

    // Get a demande by id
    public Demande getOne(int id) {
        String query = "SELECT * FROM demande WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Demande(
                        rs.getInt("id"),
                        rs.getInt("userId"),
                        rs.getString("game"),
                        rs.getString("description"),
                        rs.getBytes("file"), // Retrieve file content
                        rs.getTimestamp("date")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving demande: " + e.getMessage());
        }
        return null;
    }
}