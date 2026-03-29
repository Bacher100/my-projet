package com.conference.dao;

import com.conference.model.Equipement;
import com.conference.model.Salle;
import com.conference.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalleDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Salle> findAll() {
        List<Salle> list = new ArrayList<>();
        String sql = "SELECT * FROM salles WHERE active = TRUE ORDER BY nom";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Salle s = mapSalle(rs);
                s.setEquipements(findEquipementsBySalle(s.getId()));
                list.add(s);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Salle> findAllIncludingInactive() {
        List<Salle> list = new ArrayList<>();
        String sql = "SELECT * FROM salles ORDER BY nom";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapSalle(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Salle findById(int id) {
        String sql = "SELECT * FROM salles WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Salle s = mapSalle(rs);
                s.setEquipements(findEquipementsBySalle(id));
                return s;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Salle> findDisponibles(LocalDateTime debut, LocalDateTime fin) {
        String sql = """
            SELECT s.* FROM salles s
            WHERE s.active = TRUE
            AND s.id NOT IN (
                SELECT r.salle_id FROM reservations r
                WHERE r.statut NOT IN ('ANNULEE', 'TERMINEE')
                AND NOT (r.date_fin <= ? OR r.date_debut >= ?)
            )
            ORDER BY s.nom
            """;
        List<Salle> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(debut));
            ps.setTimestamp(2, Timestamp.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Salle s = mapSalle(rs);
                s.setEquipements(findEquipementsBySalle(s.getId()));
                list.add(s);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Equipement> findEquipementsBySalle(int salleId) {
        String sql = """
            SELECT e.*, se.quantite AS quantite_reservee
            FROM equipements e
            JOIN salle_equipements se ON e.id = se.equipement_id
            WHERE se.salle_id = ?
            """;
        List<Equipement> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, salleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Equipement eq = mapEquipement(rs);
                eq.setQuantiteReservee(rs.getInt("quantite_reservee"));
                list.add(eq);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean save(Salle salle) {
        String sql = """
            INSERT INTO salles (nom, capacite, localisation, description)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, salle.getNom());
            ps.setInt(2, salle.getCapacite());
            ps.setString(3, salle.getLocalisation());
            ps.setString(4, salle.getDescription());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) salle.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Salle salle) {
        String sql = """
            UPDATE salles SET nom=?, capacite=?, localisation=?,
            description=?, active=? WHERE id=?
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, salle.getNom());
            ps.setInt(2, salle.getCapacite());
            ps.setString(3, salle.getLocalisation());
            ps.setString(4, salle.getDescription());
            ps.setBoolean(5, salle.isActive());
            ps.setInt(6, salle.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        String sql = "UPDATE salles SET active = FALSE WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Salle mapSalle(ResultSet rs) throws SQLException {
        Salle s = new Salle();
        s.setId(rs.getInt("id"));
        s.setNom(rs.getString("nom"));
        s.setCapacite(rs.getInt("capacite"));
        s.setLocalisation(rs.getString("localisation"));
        s.setDescription(rs.getString("description"));
        s.setActive(rs.getBoolean("active"));
        Timestamp dc = rs.getTimestamp("date_creation");
        if (dc != null) s.setDateCreation(dc.toLocalDateTime());
        return s;
    }

    private Equipement mapEquipement(ResultSet rs) throws SQLException {
        Equipement e = new Equipement();
        e.setId(rs.getInt("id"));
        e.setNom(rs.getString("nom"));
        e.setDescription(rs.getString("description"));
        e.setQuantiteTotale(rs.getInt("quantite_totale"));
        e.setStatut(rs.getString("statut"));
        return e;
    }
}