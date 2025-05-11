package tn.esprit.Services;

import tn.esprit.Models.Notification;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final Connection connexion;

    public NotificationService() {
        connexion = MyDatabase.getInstance().getCnx();
    }

    public void add(Notification notification) {
        String query = "INSERT INTO notification (user_id, message, link, is_read, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getMessage());
            ps.setString(3, notification.getLink());
            ps.setBoolean(4, notification.isRead());
            ps.setTimestamp(5, Timestamp.valueOf(notification.getCreatedAt()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    notification.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add notification: " + e.getMessage(), e);
        }
    }

    public List<Notification> getNotificationsByUser(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notification WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Notification notification = new Notification();
                notification.setId(rs.getInt("id"));
                notification.setUserId(rs.getInt("user_id"));
                notification.setMessage(rs.getString("message"));
                notification.setLink(rs.getString("link"));
                notification.setRead(rs.getBoolean("is_read"));
                notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                notifications.add(notification);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch notifications: " + e.getMessage(), e);
        }
        return notifications;
    }

    public void markAsRead(int notificationId) {
        String query = "UPDATE notification SET is_read = 1 WHERE id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark notification as read: " + e.getMessage(), e);
        }
    }
    public void delete(int notificationId) {
        String query = "DELETE FROM notification WHERE id = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete notification: " + e.getMessage(), e);
        }
    }
    public int getUnreadCount(int userId) {
        String query = "SELECT COUNT(*) FROM notification WHERE user_id = ? AND is_read = 0";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count unread notifications: " + e.getMessage(), e);
        }
        return 0;
    }
}