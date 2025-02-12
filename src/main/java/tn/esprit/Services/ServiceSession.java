package tn.esprit.Services;

import tn.esprit.Models.Session_game;
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
        String qry = "INSERT INTO `session_game`(`prix`, `date_creation`, `duree_session`, `game`, `coach_id`) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setDouble(1, sessiongame.getprix());
            pstm.setDate(2, new Date(sessiongame.getdate_creation().getTime()));
            pstm.setString(3,sessiongame.getduree_session());
            pstm.setString(4, sessiongame.getGame());
            pstm.setInt(5, sessiongame.getCoach_id()); // Ajout du coach_id

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
                s.setDateCreation(new Date(rs.getTimestamp("date_creation").getTime())); // Correction ici
                s.setduree_session(rs.getString("duree_session")); // Conversion de Timestamp en LocalDateTime
                s.setGame(rs.getString("game"));
                s.setCoach_id(rs.getInt("coach_id"));

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

    @Override
    public Session_game getOne(int id) {
        return null;
    }
}