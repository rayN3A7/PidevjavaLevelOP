package tn.esprit.Services;

import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Session_game;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceSession implements IService<Session_game> {
    private Connection cnx;

    public ServiceSession() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Session_game sessiongame) {
        String qry = "INSERT INTO `session_game`(`prix`, `date_creation`, `duree_session`, `game`, `coach_id`, `image_name`) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setDouble(1, sessiongame.getprix());
            pstm.setDate(2, new Date(sessiongame.getdate_creation().getTime()));
            pstm.setString(3, sessiongame.getduree_session());
            pstm.setString(4, sessiongame.getGame());
            pstm.setInt(5, sessiongame.getCoach_id());
            pstm.setString(6, sessiongame.getImageName());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Session_game> getAll() {
        List<Session_game> sessiongames = new ArrayList<>();
        String qry = "SELECT * FROM `session_game`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Session_game s = new Session_game();
                s.setId(rs.getInt("id"));
                s.setprix(rs.getDouble("prix"));
                s.setDateCreation(new Date(rs.getTimestamp("date_creation").getTime()));
                s.setduree_session(rs.getString("duree_session"));
                s.setGame(rs.getString("game"));
                s.setCoach_id(rs.getInt("coach_id"));
                s.setImageName(rs.getString("image_name"));
                sessiongames.add(s);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return sessiongames;
    }

    @Override
    public void update(Session_game sessiongame) {
        String qry = "UPDATE `session_game` SET `prix`=?, `date_creation`=?, `duree_session`=?, `game`=?, `coach_id`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setDouble(1, sessiongame.getprix()); // Mettre à jour le prix
            pstm.setDate(2, new Date(sessiongame.getdate_creation().getTime())); // Mettre à jour la date de création
            pstm.setString(3, sessiongame.getduree_session()); // Mettre à jour la durée de session
            pstm.setString(4, sessiongame.getGame()); // Mettre à jour le jeu
            pstm.setInt(5, sessiongame.getCoach_id()); // Mettre à jour l'ID du coach
            pstm.setInt(6, sessiongame.getId()); // Identifier la session à mettre à jour

            pstm.executeUpdate();
            System.out.println("Session mise à jour avec succès");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour de la session : " + e.getMessage());
        }
    }

    @Override
    public void delete(Session_game sessiongame) {
        String qry = "DELETE FROM `session_game` WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, sessiongame.getId());
            pstm.executeUpdate();
            System.out.println("Session supprimée avec succès");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Session_game getSessionById(int id) {
        String qry = "SELECT * FROM `session_game` WHERE `id` = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return new Session_game(
                        rs.getInt("id"),
                        rs.getDouble("prix"),
                        rs.getDate("date_creation"),
                        rs.getString("duree_session"),
                        rs.getString("game"),
                        rs.getInt("coach_id"),
                        rs.getString("image_name")
                );
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de la session : " + e.getMessage());
        }
        return null;
    }

    @Override
    public Session_game getOne(int id) {
        return getSessionById(id);
    }

    // Ajout de la méthode getSessionsByCoachId()
    public List<Session_game> getSessionsByCoachId(int coachId) {
        List<Session_game> sessions = new ArrayList<>();
        String qry = "SELECT * FROM `session_game` WHERE `coach_id` = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, coachId);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                sessions.add(new Session_game(
                        rs.getInt("id"),
                        rs.getDouble("prix"),
                        rs.getDate("date_creation"),
                        rs.getString("duree_session"),
                        rs.getString("game"),
                        rs.getInt("coach_id"),
                        rs.getString("image_name")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des sessions du coach : " + e.getMessage());
        }
        return sessions;
    }
    public List<Session_game> getSessionsInPromo() {
        List<Session_game> promoSessions = new ArrayList<>();
        String qry = "SELECT * FROM `session_game` WHERE `prix` < 60";
        try (Statement stm = cnx.createStatement()) {
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                promoSessions.add(new Session_game(
                        rs.getInt("id"),
                        rs.getDouble("prix"),
                        rs.getDate("date_creation"),
                        rs.getString("duree_session"),
                        rs.getString("game"),
                        rs.getInt("coach_id"),
                        rs.getString("image_name")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des sessions en promotion : " + e.getMessage());
        }
        return promoSessions;
    }
}