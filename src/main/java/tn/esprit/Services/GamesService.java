package tn.esprit.Services;



import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Games;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GamesService implements IService<Games> {
    private static Connection connexion;

    public GamesService() {
        connexion = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Games games) {
        String query = "INSERT INTO Games (game_name) VALUES (?)";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, games.getGame_name());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Games getOne(int id) {
        String query = "SELECT * FROM Games WHERE game_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Games(rs.getInt("game_id"), rs.getString("game_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Games> getAll() {
        List<Games> gamesList = new ArrayList<>();
        String query = "SELECT * FROM Games";
        try (Statement st = connexion.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                gamesList.add(new Games(rs.getInt("game_id"), rs.getString("game_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gamesList;
    }

    @Override
    public void update(Games games) {
        String query = "UPDATE Games SET game_name = ? WHERE game_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, games.getGame_name());
            ps.setInt(2, games.getGame_id());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Games games) {
        String query = "DELETE FROM Games WHERE game_id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, games.getGame_id());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Games getByName(String name) {
        String query = "SELECT * FROM Games WHERE game_name = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Games(rs.getInt("game_id"), rs.getString("game_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
