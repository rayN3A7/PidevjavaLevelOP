package tn.esprit.Services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Produit;
import tn.esprit.utils.MyDatabase;

public class ProduitService implements IService<Produit> {
    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;

    public ProduitService() {
        connection = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Produit produit) {
        String query = "INSERT INTO produit (nom_produit, description, platform, region, type, activation_region, score) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            System.out.println("Attempting to add product with values:");
            System.out.println("nom_produit: " + produit.getNomProduit());
            System.out.println("description: " + produit.getDescription());
            System.out.println("platform: " + produit.getPlatform());
            System.out.println("region: " + produit.getRegion());
            System.out.println("type: " + produit.getType());
            System.out.println("activation_region: " + produit.getActivation_region());
            System.out.println("score: " + produit.getScore());

            preparedStatement.setString(1, produit.getNomProduit());
            preparedStatement.setString(2, produit.getDescription());
            preparedStatement.setString(3, produit.getPlatform());
            preparedStatement.setString(4, produit.getRegion());
            preparedStatement.setString(5, produit.getType());
            preparedStatement.setString(6, produit.getActivation_region());
            preparedStatement.setInt(7, produit.getScore());

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Rows affected by insert: " + rowsAffected);

            // Retrieve the generated ID
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                produit.setId(generatedId);
                System.out.println("Generated ID for new product: " + generatedId);
            } else {
                System.out.println("No ID was generated");
                throw new RuntimeException("Failed to get generated product ID");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error during product insertion:");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            throw new RuntimeException("Failed to add product to database", e);
        }
    }

    @Override
    public List<Produit> getAll() {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit";
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()) {
                Produit produit = new Produit(
                        resultSet.getInt("id"),
                        resultSet.getString("nom_produit"),
                        resultSet.getString("description"),
                        resultSet.getString("platform"),
                        resultSet.getString("region"),
                        resultSet.getString("type"),
                        resultSet.getString("activation_region"),
                        resultSet.getInt("score")
                );
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return produits;
    }

    @Override
    public void update(Produit produit) {
        String query = "UPDATE produit SET nom_produit = ?, description = ?, " +
                "platform = ?, region = ?, type = ?, activation_region = ?, score = ? " +
                "WHERE id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, produit.getNomProduit());
            preparedStatement.setString(2, produit.getDescription());
            preparedStatement.setString(3, produit.getPlatform());
            preparedStatement.setString(4, produit.getRegion());
            preparedStatement.setString(5, produit.getType());
            preparedStatement.setString(6, produit.getActivation_region());
            preparedStatement.setInt(7, produit.getScore());
            preparedStatement.setInt(8, produit.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Updated product rows affected: " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("SQL Error during product update:");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            throw new RuntimeException("Failed to update product", e);
        }
    }

    public Produit getOne(int id) {
        String query = "SELECT * FROM produit WHERE id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return new Produit(
                        resultSet.getInt("id"),
                        resultSet.getString("nom_produit"),
                        resultSet.getString("description"),
                        resultSet.getString("platform"),
                        resultSet.getString("region"),
                        resultSet.getString("type"),
                        resultSet.getString("activation_region"),
                        resultSet.getInt("score")
                );
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void delete(Produit t) {
        String query = "DELETE FROM produit WHERE id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, t.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}