package tn.esprit.Services;

import tn.esprit.Models.Report;
import tn.esprit.Models.ReportReason;
import tn.esprit.Models.ReportStatus;
import tn.esprit.utils.MyDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportService {
    private Connection connection;

    public ReportService() {
        connection = MyDatabase.getInstance().getCnx();


    }
    /**
     * Create the report table in the database
     */


    // Ajouter un signalement
    public void addReport(Report report) {
        String sql = "INSERT INTO reports (reporterId, reportedUserId, reason, evidence, status) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, report.getReporterId());
            statement.setInt(2, report.getReportedUserId());
            statement.setString(3, report.getReason().name());
            statement.setString(4, report.getEvidence());
            statement.setString(5, report.getStatus().name());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Signalement ajouté avec succès !");
            } else {
                System.out.println("⚠️ Aucun signalement ajouté.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du signalement : " + e.getMessage());
        }
    }

    // Récupérer tous les signalements
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports";

        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                Report report = new Report(
                        rs.getInt("reporterId"),
                        rs.getInt("reportedUserId"),
                        ReportReason.valueOf(rs.getString("reason")),
                        rs.getString("evidence"));
                report.setReportId(rs.getInt("reportId"));
                report.setStatus(ReportStatus.valueOf(rs.getString("status")));
                reports.add(report);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des signalements : " + e.getMessage());
        }

        return reports;
    }

    // Mettre à jour le statut d'un signalement
    public void updateReportStatus(int reportId, ReportStatus newStatus) {
        String sql = "UPDATE reports SET status = ? WHERE reportId = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus.name());
            statement.setInt(2, reportId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Statut mis à jour avec succès !");
            } else {
                System.out.println("⚠️ Aucun signalement trouvé avec cet ID.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du statut : " + e.getMessage());
        }
    }

    // Supprimer un signalement
    public void deleteReport(int reportId) {
        String sql = "DELETE FROM reports WHERE reportId = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reportId);

            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("✅ Signalement supprimé avec succès !");
            } else {
                System.out.println("⚠️ Aucun signalement trouvé avec cet ID.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du signalement : " + e.getMessage());
        }
    }

    public List<Report> getReportsByReportedUserId(int reportedUserId) {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE reportedUserId = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reportedUserId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Report report = new Report(
                        rs.getInt("reporterId"),
                        rs.getInt("reportedUserId"),
                        ReportReason.valueOf(rs.getString("reason")),
                        rs.getString("evidence"));
                report.setReportId(rs.getInt("reportId"));
                report.setStatus(ReportStatus.valueOf(rs.getString("status")));
                reports.add(report);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des signalements pour l'utilisateur " + reportedUserId
                    + " : " + e.getMessage());
        }

        return reports;
    }

    public int countReportsByReportedUserId(int reportedUserId) {
        String sql = "SELECT COUNT(*) FROM reports WHERE reportedUserId = ?";
        int count = 0;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reportedUserId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des signalements pour l'utilisateur " + reportedUserId + " : "
                    + e.getMessage());
        }

        return count;
    }





}
