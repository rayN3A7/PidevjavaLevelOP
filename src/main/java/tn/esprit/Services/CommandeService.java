package tn.esprit.Services;

import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Commande;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import tn.esprit.Models.Produit;
public class CommandeService implements IService<Commande> {
    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;

    public CommandeService() {
        connection = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Commande commande) {
        String query = "INSERT INTO commande (utilisateur_id, produit_id, status) VALUES (?, ?, ?)";
        try {
            preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, commande.getUtilisateurId());
            preparedStatement.setInt(2, commande.getProduitId());
            preparedStatement.setString(3, commande.getStatus());
            preparedStatement.executeUpdate();

            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
                commande.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Commande> getAll() {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commande";
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()) {
                Commande commande = new Commande(
                        resultSet.getInt("id"),
                        resultSet.getInt("utilisateur_id"),
                        resultSet.getInt("produit_id"),
                        resultSet.getString("status")
                );
                commandes.add(commande);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return commandes;
    }
    @Override
    public void update(Commande commande) {
        String query = "UPDATE commande SET utilisateur_id = ?, produit_id = ?, status = ? WHERE id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, commande.getUtilisateurId());
            preparedStatement.setInt(2, commande.getProduitId());
            preparedStatement.setString(3, commande.getStatus());
            preparedStatement.setInt(4, commande.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public Commande getOne(int id) {
        String query = "SELECT * FROM commande WHERE id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return new Commande(
                        resultSet.getInt("id"),
                        resultSet.getInt("utilisateur_id"),
                        resultSet.getInt("produit_id"),
                        resultSet.getString("status")
                );
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void delete(Commande t) {
        String query = "DELETE FROM commande WHERE id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, t.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
