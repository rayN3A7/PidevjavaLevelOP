package tn.esprit.Services;

import tn.esprit.Models.Reservation;
import tn.esprit.Models.Session_game;
import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Reservation;
import tn.esprit.Models.Session_game;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservation implements IService<Reservation> {
    private Connection cnx;

    public ServiceReservation() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Reservation reservation) {
        String qry = "INSERT INTO `reservation`(`date_reservation`, `session_id_id`, `client_id`) VALUES (?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setDate(1, new Date(reservation.getdate_reservation().getTime()));
            pstm.setInt(2, reservation.getSession().getId());
            pstm.setInt(3, reservation.getClient_id()); // Ajout du client_id

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Reservation> getAll() {
        List<Reservation> reservations = new ArrayList<>();
        String qry = "SELECT * FROM `reservation`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);

            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setdate_reservation(new Date(rs.getTimestamp("date_reservation").getTime())); // Correction ici
                r.setClient_id(rs.getInt("client_id"));

                // Fetch the associated session
                ServiceSession serviceSession = new ServiceSession();
                Session_game sessiongame = serviceSession.getAll().stream()
                        .filter(s -> {
                            try {
                                return s.getId() == rs.getInt("session_id_id");
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .findFirst()
                        .orElse(null);
                r.setSession(sessiongame);

                reservations.add(r);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reservations;
    }

    @Override
    public void update(Reservation reservation) {
        String qry = "UPDATE `reservation` SET `date_reservation`=?, `session_id_id`=?, `client_id`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setDate(1, new Date(reservation.getdate_reservation().getTime()));
            pstm.setInt(2, reservation.getSession().getId());
            pstm.setInt(3, reservation.getClient_id());
            pstm.setInt(4, reservation.getId());
            pstm.executeUpdate();
            System.out.println("Reservation mise à jour avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Reservation reservation) {
        String qry = "DELETE FROM `reservation` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, reservation.getId());
            pstm.executeUpdate();
            System.out.println("Reservation supprimée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public Reservation getReservationByClientAndSession(int clientId, int sessionId) {
        String qry = "SELECT * FROM reservation WHERE client_id = ? AND session_id_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, clientId);
            pstm.setInt(2, sessionId);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setdate_reservation(rs.getDate("date_reservation"));
                reservation.setClient_id(rs.getInt("client_id"));

                ServiceSession serviceSession = new ServiceSession();
                Session_game sessiongame = serviceSession.getAll().stream()
                        .filter(s -> {
                            try {
                                return s.getId() == rs.getInt("session_id_id");
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .findFirst()
                        .orElse(null);
                reservation.setSession(sessiongame);
                return reservation;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    // Méthode qui vérifie si une session est déjà réservée
    public boolean isSessionReserved(int sessionId) {
        String qry = "SELECT 1 FROM reservation WHERE session_id_id = ? LIMIT 1"; // Query améliorée pour vérifier rapidement
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, sessionId);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next(); // Si une ligne est retournée, cela signifie que la session est réservée
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification de la réservation : " + e.getMessage());
        }
        return false; // Retourne false en cas d'erreur ou si la session n'est pas réservée
    }
    public boolean isSessionAlreadyReserved(int sessionId) {
        String qry = "SELECT COUNT(*) FROM `reservation` WHERE `session_id_id` = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, sessionId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Retourne true si une réservation existe déjà
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification de la réservation : " + e.getMessage());
        }
        return false;
    }


    @Override
    public Reservation getOne(int id) {
        return null;
    }
}
