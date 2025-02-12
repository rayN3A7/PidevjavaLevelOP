package tn.esprit.Services;

import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Produit;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements IService<Produit> {
    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;
    
    public ProduitService() {
        connection = MyDatabase.getInstance().getCnx();
    }
    
    @Override
    public void add(Produit produit) {
        String query = "INSERT INTO produit (nom_produit, description, score) VALUES (?, ?, ?)";
        try {
            preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, produit.getNomProduit());
            preparedStatement.setString(2, produit.getDescription());
            preparedStatement.setInt(3, produit.getScore());
            preparedStatement.executeUpdate();
            
            // Récupérer l'ID généré
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
                produit.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
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
        String query = "UPDATE produit SET nom_produit = ?, description = ?, score = ? WHERE id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, produit.getNomProduit());
            preparedStatement.setString(2, produit.getDescription());
            preparedStatement.setInt(3, produit.getScore());
            preparedStatement.setInt(4, produit.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
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