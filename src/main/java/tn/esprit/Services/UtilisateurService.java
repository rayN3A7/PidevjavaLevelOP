package tn.esprit.Services;

import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Role;
import tn.esprit.Models.Utilisateur;
import tn.esprit.utils.MyDatabase;
import tn.esprit.utils.SessionManager;

import java.sql.*;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilisateurService implements IService<Utilisateur> {

    private Connection cnx;

    public UtilisateurService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Utilisateur utilisateur) {
        String query = "INSERT INTO utilisateur (email, mot_passe, nickname, nom, numero, prenom, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {

            PreparedStatement stmt = cnx.prepareStatement(query);
            stmt.setString(1, utilisateur.getEmail());
            stmt.setString(2, utilisateur.getMotPasse());
            stmt.setString(3, utilisateur.getNickname());
            stmt.setString(4, utilisateur.getNom());
            stmt.setInt(5, utilisateur.getNumero());
            stmt.setString(6, utilisateur.getPrenom());
            stmt.setString(7, utilisateur.getRole().name());
            stmt.executeUpdate();

            if (utilisateur.getRole().equals(Role.CLIENT)) {
                String clientQuery = "INSERT INTO client (id) VALUES (?)";
                PreparedStatement clientStmt = cnx.prepareStatement(clientQuery);
                clientStmt.setInt(1, getLastInsertedId());
                clientStmt.executeUpdate();
            } else if (utilisateur.getRole().equals(Role.COACH)) {
                String coachQuery = "INSERT INTO coach (id) VALUES (?)";
                PreparedStatement coachStmt = cnx.prepareStatement(coachQuery);
                coachStmt.setInt(1, getLastInsertedId());
                coachStmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    private int getLastInsertedId() throws SQLException {
        String query = "SELECT LAST_INSERT_ID()";
        PreparedStatement stmt = cnx.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        throw new SQLException("Failed to retrieve last inserted ID.");
    }

    @Override
    public List<Utilisateur> getAll() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT id, email, nickname, nom, numero, prenom, role, privilege, ban, banTime FROM utilisateur";

        try (PreparedStatement stmt = cnx.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Timestamp banTimestamp = rs.getTimestamp("banTime");
                // System.out.println("Fetched banTime for user ID " + rs.getInt("id") + ": " +
                // banTimestamp);

                LocalDateTime banTime = (banTimestamp != null && banTimestamp.getTime() != 0)
                        ? banTimestamp.toLocalDateTime()
                        : null;

                Utilisateur utilisateur = new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("nickname"),
                        rs.getString("nom"),
                        rs.getInt("numero"),
                        rs.getString("prenom"),
                        Role.valueOf(rs.getString("role")),
                        rs.getString("privilege"),
                        rs.getBoolean("ban"),
                        banTime);

                utilisateurs.add(utilisateur);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching users: " + e.getMessage());
        }

        return utilisateurs;
    }

    @Override
    public void update(Utilisateur utilisateur) {
        String query = "UPDATE utilisateur SET nickname = ?, nom = ?, numero = ?, prenom = ?, role = ?, mot_passe = ?, photo = ? WHERE id = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(query);
            stmt.setString(1, utilisateur.getNickname());
            stmt.setString(2, utilisateur.getNom());
            stmt.setInt(3, utilisateur.getNumero());
            stmt.setString(4, utilisateur.getPrenom());
            stmt.setString(5, utilisateur.getRole().name());
            stmt.setString(6, utilisateur.getMotPasse());

            // Handle photo (can be null)
            if (utilisateur.getPhoto() != null) {
                stmt.setString(7, utilisateur.getPhoto());
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }

            stmt.setInt(8, utilisateur.getId());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("User updated successfully: " + utilisateur.getId());
            } else {
                System.out.println("No user found with ID: " + utilisateur.getId());
            }
        } catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Utilisateur utilisateur) {
        String query = "DELETE FROM utilisateur WHERE email = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(query);
            stmt.setString(1, utilisateur.getEmail());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }

    }

    public List<Utilisateur> getByRole(String role) {

        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT email, nickname, nom, numero, prenom, role FROM utilisateur WHERE role = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(query);
            stmt.setString(1, role.toUpperCase());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur(
                        rs.getString("email"),
                        rs.getString("nickname"),
                        rs.getString("nom"),
                        rs.getInt("numero"),
                        rs.getString("prenom"),
                        Role.valueOf(rs.getString("role")));
                utilisateurs.add(utilisateur);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération par rôle : " + e.getMessage());
        }

        return utilisateurs;
    }

    public Utilisateur getBynickname(String nickname) {
        Utilisateur utilisateur = null;
        String query = "SELECT * FROM utilisateur WHERE nickname = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(query);
            stmt.setString(1, nickname);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                utilisateur = new Utilisateur(

                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("mot_passe"),
                        rs.getString("nickname"),
                        rs.getString("nom"),
                        rs.getInt("numero"),
                        rs.getString("prenom"),
                        Role.valueOf(rs.getString("role")));

            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération par nickname : " + e.getMessage());
        }

        return utilisateur;
    }

    public Utilisateur getByEmail(String email) {
        Utilisateur utilisateur = null;
        String query = "SELECT * FROM utilisateur WHERE email = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                utilisateur = new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("mot_passe"),
                        rs.getString("nickname"),
                        rs.getString("nom"),
                        rs.getInt("numero"),
                        rs.getString("prenom"),
                        Role.valueOf(rs.getString("role"))
                );
                // Set additional fields
                utilisateur.setPrivilege(rs.getString("privilege"));
                utilisateur.setBan(rs.getBoolean("ban"));
                utilisateur.setBanTime(rs.getTimestamp("banTime") != null ? rs.getTimestamp("banTime").toLocalDateTime() : null);
                utilisateur.setCountRep(rs.getInt("countRep"));
                utilisateur.setPhoto(rs.getString("photo"));
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération par email : " + e.getMessage());
        }

        return utilisateur;
    }

    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification de l'email : " + e.getMessage());
        }

        return false;
    }

    private String nickname;

    public boolean nicknameExists(String nickname) {
        String query = "SELECT COUNT(*) FROM utilisateur WHERE nickname = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(query);
            stmt.setString(1, nickname);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification du nickname : " + e.getMessage());
        }

        return false;
    }

    @Override
    public Utilisateur getOne(int id) {
        String query = "SELECT * FROM Utilisateur WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Utilisateur user = new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("mot_passe"),
                        rs.getString("nickname"),
                        rs.getString("nom"),
                        rs.getInt("numero"),
                        rs.getString("prenom"),
                        Role.valueOf(rs.getString("role")));
                user.setPrivilege(rs.getString("privilege") != null ? rs.getString("privilege") : "regular");
                user.setPhoto(rs.getString("photo"));
                return user;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean loginUser(String email, String password, boolean rememberMe) {
        Utilisateur user = getByEmail(email);

        if (user != null && BCrypt.checkpw(password, user.getMotPasse())) {
            SessionManager.getInstance().login(user.getId(), user.getRole(), user.getEmail(), rememberMe);
            return true;
        }
        return false;
    }

    public void updatePassword(String email, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt()); // Hash the password
        String query = "UPDATE utilisateur SET mot_passe = ? WHERE email = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, hashedPassword); // Set the hashed password
            stmt.setString(2, email);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Mot de passe mis à jour avec succès !");
            } else {
                System.out.println("Aucun utilisateur trouvé avec cet email.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour du mot de passe : " + e.getMessage());
        }
    }

    public int getUserActivityCount(int userId) {
        int count = 0;
        String questionQuery = "SELECT COUNT(*) FROM Questions WHERE Utilisateur_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(questionQuery)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count += rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count questions: " + e.getMessage(), e);
        }

        String commentQuery = "SELECT COUNT(*) FROM Commentaire WHERE utilisateur_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(commentQuery)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count += rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count comments: " + e.getMessage(), e);
        }

        return count;
    }

    public int getUserVoteCount(int userId) {
        int totalVotes = 0;

        String questionVoteQuery = "SELECT SUM(Votes) FROM Questions WHERE Utilisateur_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(questionVoteQuery)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                totalVotes += rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count question votes: " + e.getMessage(), e);
        }

        String commentVoteQuery = "SELECT SUM(Votes) FROM Commentaire WHERE utilisateur_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(commentVoteQuery)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                totalVotes += rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count comment votes: " + e.getMessage(), e);
        }

        return totalVotes;
    }

    public void updateUserRole(int userId) {

        String query = "UPDATE Utilisateur SET role = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, "COACH");
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update Role: " + e.getMessage(), e);
        }
    }

    public void deleteClient(int userId) {
        String query = "DELETE FROM client WHERE id = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(query);
            stmt.setInt(1, userId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }

    }

    public void addCoach(int useId) {
        String query = "INSERT INTO coach (id) VALUES (?)";
        try {

            PreparedStatement stmt = cnx.prepareStatement(query);

            stmt.setInt(1, useId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public String getEmail(int id) {
        String query = "SELECT email FROM Utilisateur WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {

                return rs.getString("email");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user: " + e.getMessage(), e);
        }
        return null;
    }

    public class PrivilegeChange {
        private final String oldPrivilege;
        private final String newPrivilege;

        public PrivilegeChange(String oldPrivilege, String newPrivilege) {
            this.oldPrivilege = oldPrivilege;
            this.newPrivilege = newPrivilege;
        }

        public String getOldPrivilege() {
            return oldPrivilege;
        }

        public String getNewPrivilege() {
            return newPrivilege;
        }

        public boolean isChanged() {
            return !oldPrivilege.equals(newPrivilege);
        }
    }

    public PrivilegeChange updateUserPrivilege(int userId) {
        Utilisateur user = getOne(userId);
        String oldPrivilege = user.getPrivilege() != null ? user.getPrivilege() : "regular";

        int activityCount = getUserActivityCount(userId);
        int voteCount = getUserVoteCount(userId);

        String newPrivilege;
        if (activityCount >= 5 && voteCount > 10) {
            newPrivilege = "top_fan";
        } else if (activityCount >= 5) {
            newPrivilege = "top_contributor";
        } else {
            newPrivilege = "regular";
        }

        if (!newPrivilege.equals(oldPrivilege)) {
            String query = "UPDATE Utilisateur SET privilege = ? WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(query)) {
                ps.setString(1, newPrivilege);
                ps.setInt(2, userId);
                ps.executeUpdate();
                System.out.println(
                        "Updated privilege for user " + userId + " from " + oldPrivilege + " to " + newPrivilege);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update privilege: " + e.getMessage(), e);
            }
            user.setPrivilege(newPrivilege);
        }

        return new PrivilegeChange(oldPrivilege, newPrivilege);
    }

    public void updateBanStatus(int userId, boolean isBanned, LocalDateTime banTime) {
        String query = "UPDATE utilisateur SET ban = ?, banTime = ? WHERE id = ?";

        try {
            PreparedStatement stmt = cnx.prepareStatement(query);
            stmt.setBoolean(1, isBanned);

            // Handle banTime (can be null for permanent ban)
            if (banTime != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(banTime));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }

            stmt.setInt(3, userId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Ban status updated successfully for user: " + userId);
            } else {
                System.out.println("No user found with ID: " + userId);
            }
        } catch (SQLException e) {
            System.out.println("Error updating ban status: " + e.getMessage());
            e.printStackTrace();
        }
    }

}