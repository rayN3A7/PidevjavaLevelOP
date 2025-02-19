package tn.esprit.Services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Stock;
import tn.esprit.utils.MyDatabase;

public class StockService implements IService<Stock> {
    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;

    public StockService() {
        connection = MyDatabase.getInstance().getCnx();
    }

    public Stock getByProduitId(int produitId) {
        String query = "SELECT * FROM stock WHERE produit_id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, produitId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return new Stock(
                        resultSet.getInt("id"),
                        resultSet.getInt("produit_id"),
                        resultSet.getInt("games_id"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("prix_produit"),
                        resultSet.getString("image")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting stock for product " + produitId + ": " + e.getMessage());
        }
        return null;
    }

    @Override
    public void add(Stock stock) {
        String query = "INSERT INTO stock (produit_id, games_id, quantity, prix_produit, image) VALUES (?, ?, ?, ?, ?)";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, stock.getProduitId());
            preparedStatement.setInt(2, stock.getGamesId());
            preparedStatement.setInt(3, stock.getQuantity());
            preparedStatement.setInt(4, stock.getPrixProduit());
            preparedStatement.setString(5, stock.getImage());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    @Override
    public List<Stock> getAll() {
        List<Stock> stocks = new ArrayList<>();
        String query = "SELECT * FROM stock";
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()) {
                Stock stock = new Stock(
                        resultSet.getInt("id"),
                        resultSet.getInt("produit_id"),
                        resultSet.getInt("games_id"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("prix_produit"),
                        resultSet.getString("image")
                );
                stocks.add(stock);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return stocks;
    }

    @Override
    public void update(Stock stock) {
        String query = "UPDATE stock SET produit_id = ?, games_id = ?, quantity = ?, prix_produit = ?, image = ? WHERE id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, stock.getProduitId());
            preparedStatement.setInt(2, stock.getGamesId());
            preparedStatement.setInt(3, stock.getQuantity());
            preparedStatement.setInt(4, stock.getPrixProduit());
            preparedStatement.setString(5, stock.getImage());
            preparedStatement.setInt(6, stock.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public Stock getOne(int id) {
        String query = "SELECT * FROM stock WHERE id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return new Stock(
                        resultSet.getInt("id"),
                        resultSet.getInt("produit_id"),
                        resultSet.getInt("games_id"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("prix_produit"),
                        resultSet.getString("image")
                );
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void delete(Stock t) {
        String query = "DELETE FROM stock WHERE id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, t.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
