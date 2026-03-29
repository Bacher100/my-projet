package com.conference.dao;

import com.conference.model.Reservation;
import com.conference.model.Salle;
import com.conference.model.User;
import com.conference.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Reservation> findAll() {
        return findByQuery("""
            SELECT r.*, u.nom, u.prenom, u.email, s.nom AS salle_nom,
                   s.capacite, s.localisation
            FROM reservations r
            JOIN users u ON r.utilisateur_id = u.id
            JOIN salles s ON r.salle_id = s.id
            ORDER BY r.date_debut DESC
            """, null);
    }

    public List<Reservation> findByUser(int userId) {
        return findByQuery("""
            SELECT r.*, u.nom, u.prenom, u.email, s.nom AS salle_nom,
                   s.capacite, s.localisation
            FROM reservations r
            JOIN users u ON r.utilisateur_id = u.id
            JOIN salles s ON r.salle_id = s.id
            WHERE r.utilisateur_id = ?
            ORDER BY r.date_debut DESC
            """, userId);
    }

    public List<Reservation> findBySalle(int salleId) {
        return findByQuery("""
            SELECT r.*, u.nom, u.prenom, u.email, s.nom AS salle_nom,
                   s.capacite, s.localisation
            FROM reservations r
            JOIN users u ON r.utilisateur_id = u.id
            JOIN salles s ON r.salle_id = s.id
            WHERE r.salle_id = ?
            ORDER BY r.date_debut DESC
            """, salleId);
    }

    public List<Reservation> findUpcoming() {
        String sql = """
            SELECT r.*, u.nom, u.prenom, u.email, s.nom AS salle_nom,
                   s.capacite, s.localisation
            FROM reservations r
            JOIN users u ON r.utilisateur_id = u.id
            JOIN salles s ON r.salle_id = s.id
            WHERE r.statut = 'CONFIRMEE' AND r.date_debut > NOW()
            ORDER BY r.date_debut ASC
            """;
        return findByQuery(sql, null);
    }

    public List<Reservation> findForReminder(int minutesBefore) {
        String sql = """
            SELECT r.*, u.nom, u.prenom, u.email, s.nom AS salle_nom,
                   s.capacite, s.localisation
            FROM reservations r
            JOIN users u ON r.utilisateur_id = u.id
            JOIN salles s ON r.salle_id = s.id
            WHERE r.statut = 'CONFIRMEE'
            AND r.date_debut BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL ? MINUTE)
            """;
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, minutesBefore);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapReservation(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Reservation findById(int id) {
        String sql = """
            SELECT r.*, u.nom, u.prenom, u.email, s.nom AS salle_nom,
                   s.capacite, s.localisation
            FROM reservations r
            JOIN users u ON r.utilisateur_id = u.id
            JOIN salles s ON r.salle_id = s.id
            WHERE r.id = ?
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapReservation(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean isConflict(int salleId, LocalDateTime debut,
                              LocalDateTime fin, Integer excludeId) {
        String sql = """
            SELECT COUNT(*) FROM reservations
            WHERE salle_id = ?
            AND statut NOT IN ('ANNULEE', 'TERMINEE')
            AND NOT (date_fin <= ? OR date_debut >= ?)
            """ + (excludeId != null ? "AND id != ?" : "");
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, salleId);
            ps.setTimestamp(2, Timestamp.valueOf(debut));
            ps.setTimestamp(3, Timestamp.valueOf(fin));
            if (excludeId != null) ps.setInt(4, excludeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean save(Reservation reservation) {
        String sql = """
            INSERT INTO reservations
            (utilisateur_id, salle_id, titre, description, date_debut, date_fin,
             nombre_participants, statut, disposition)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservation.getUtilisateurId());
            ps.setInt(2, reservation.getSalleId());
            ps.setString(3, reservation.getTitre());
            ps.setString(4, reservation.getDescription());
            ps.setTimestamp(5, Timestamp.valueOf(reservation.getDateDebut()));
            ps.setTimestamp(6, Timestamp.valueOf(reservation.getDateFin()));
            ps.setInt(7, reservation.getNombreParticipants());
            ps.setString(8, reservation.getStatut() != null
                    ? reservation.getStatut() : "EN_ATTENTE");
            ps.setString(9, reservation.getDisposition() != null
                    ? reservation.getDisposition() : "THEATRE");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) reservation.setId(keys.getInt(1));
                saveEquipements(reservation);
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Reservation reservation) {
        String sql = """
            UPDATE reservations SET salle_id=?, titre=?, description=?,
            date_debut=?, date_fin=?, nombre_participants=?, statut=?, disposition=?
            WHERE id=?
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, reservation.getSalleId());
            ps.setString(2, reservation.getTitre());
            ps.setString(3, reservation.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(reservation.getDateDebut()));
            ps.setTimestamp(5, Timestamp.valueOf(reservation.getDateFin()));
            ps.setInt(6, reservation.getNombreParticipants());
            ps.setString(7, reservation.getStatut());
            ps.setString(8, reservation.getDisposition());
            ps.setInt(9, reservation.getId());
            if (ps.executeUpdate() > 0) {
                deleteEquipements(reservation.getId());
                saveEquipements(reservation);
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateStatut(int id, String statut) {
        String sql = "UPDATE reservations SET statut = ? WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM reservations WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // --- Statistiques ---

    public int countTotal() {
        return countQuery("SELECT COUNT(*) FROM reservations");
    }

    public int countByStatut(String statut) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE statut = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<Object[]> getStatsByMonth(int year) {
        String sql = """
            SELECT MONTH(date_debut) AS mois, COUNT(*) AS total
            FROM reservations
            WHERE YEAR(date_debut) = ?
            GROUP BY MONTH(date_debut)
            ORDER BY mois
            """;
        List<Object[]> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new Object[]{ rs.getInt("mois"), rs.getInt("total") });
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> getTopSalles(int limit) {
        String sql = """
            SELECT s.nom, COUNT(r.id) AS total
            FROM reservations r JOIN salles s ON r.salle_id = s.id
            WHERE r.statut != 'ANNULEE'
            GROUP BY s.nom ORDER BY total DESC LIMIT ?
            """;
        List<Object[]> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new Object[]{ rs.getString("nom"), rs.getInt("total") });
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public double getAverageDuration() {
        String sql = """
            SELECT AVG(TIMESTAMPDIFF(MINUTE, date_debut, date_fin)) AS avg_min
            FROM reservations WHERE statut = 'TERMINEE'
            """;
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("avg_min");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // --- Helpers privés ---

    private void saveEquipements(Reservation reservation) throws SQLException {
        if (reservation.getEquipements() == null || reservation.getEquipements().isEmpty())
            return;
        String sql = """
            INSERT INTO reservation_equipements (reservation_id, equipement_id, quantite)
            VALUES (?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            for (var eq : reservation.getEquipements()) {
                ps.setInt(1, reservation.getId());
                ps.setInt(2, eq.getId());
                ps.setInt(3, Math.max(1, eq.getQuantiteReservee()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteEquipements(int reservationId) throws SQLException {
        String sql = "DELETE FROM reservation_equipements WHERE reservation_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.executeUpdate();
        }
    }

    private List<Reservation> findByQuery(String sql, Integer param) {
        List<Reservation> list = new ArrayList<>();
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            if (param != null) ps.setInt(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapReservation(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private int countQuery(String sql) {
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setUtilisateurId(rs.getInt("utilisateur_id"));
        r.setSalleId(rs.getInt("salle_id"));
        r.setTitre(rs.getString("titre"));
        r.setDescription(rs.getString("description"));
        r.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime());
        r.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime());
        r.setNombreParticipants(rs.getInt("nombre_participants"));
        r.setStatut(rs.getString("statut"));
        r.setDisposition(rs.getString("disposition"));
        Timestamp dc = rs.getTimestamp("date_creation");
        if (dc != null) r.setDateCreation(dc.toLocalDateTime());

        // Objets liés depuis le JOIN
        try {
            User u = new User();
            u.setId(rs.getInt("utilisateur_id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            u.setEmail(rs.getString("email"));
            r.setUtilisateur(u);

            Salle s = new Salle();
            s.setId(rs.getInt("salle_id"));
            s.setNom(rs.getString("salle_nom"));
            s.setCapacite(rs.getInt("capacite"));
            s.setLocalisation(rs.getString("localisation"));
            r.setSalle(s);
        } catch (SQLException ignored) {}
        return r;
    }
}