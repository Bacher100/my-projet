package com.conference.dao;

import com.conference.model.Notification;
import com.conference.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Notification> findByUser(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = """
            SELECT * FROM notifications WHERE utilisateur_id = ?
            ORDER BY date_creation DESC
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapNotification(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE utilisateur_id=? AND lue=FALSE";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public boolean save(Notification n) {
        String sql = """
            INSERT INTO notifications (utilisateur_id, reservation_id, titre, message, type)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getUtilisateurId());
            if (n.getReservationId() != null) ps.setInt(2, n.getReservationId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, n.getTitre());
            ps.setString(4, n.getMessage());
            ps.setString(5, n.getType());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) n.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean markAsRead(int id) {
        String sql = "UPDATE notifications SET lue = TRUE WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET lue = TRUE WHERE utilisateur_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Notification mapNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUtilisateurId(rs.getInt("utilisateur_id"));
        int rid = rs.getInt("reservation_id");
        if (!rs.wasNull()) n.setReservationId(rid);
        n.setTitre(rs.getString("titre"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setLue(rs.getBoolean("lue"));
        Timestamp dc = rs.getTimestamp("date_creation");
        if (dc != null) n.setDateCreation(dc.toLocalDateTime());
        return n;
    }
}