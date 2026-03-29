package com.conference.dao;

import com.conference.model.Equipement;
import com.conference.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipementDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Equipement> findAll() {
        List<Equipement> list = new ArrayList<>();
        String sql = "SELECT * FROM equipements ORDER BY nom";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapEquipement(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Equipement> findDisponibles() {
        List<Equipement> list = new ArrayList<>();
        String sql = "SELECT * FROM equipements WHERE statut = 'DISPONIBLE' ORDER BY nom";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapEquipement(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Equipement findById(int id) {
        String sql = "SELECT * FROM equipements WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapEquipement(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean save(Equipement eq) {
        String sql = """
            INSERT INTO equipements (nom, description, quantite_totale, statut)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, eq.getNom());
            ps.setString(2, eq.getDescription());
            ps.setInt(3, eq.getQuantiteTotale());
            ps.setString(4, eq.getStatut() != null ? eq.getStatut() : "DISPONIBLE");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) eq.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Equipement eq) {
        String sql = """
            UPDATE equipements SET nom=?, description=?,
            quantite_totale=?, statut=? WHERE id=?
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, eq.getNom());
            ps.setString(2, eq.getDescription());
            ps.setInt(3, eq.getQuantiteTotale());
            ps.setString(4, eq.getStatut());
            ps.setInt(5, eq.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean signalerDefectueux(int equipementId, int userId, String description) {
        String sql = """
            INSERT INTO signalements (equipement_id, utilisateur_id, description)
            VALUES (?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, equipementId);
            ps.setInt(2, userId);
            ps.setString(3, description);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Equipement mapEquipement(ResultSet rs) throws SQLException {
        Equipement e = new Equipement();
        e.setId(rs.getInt("id"));
        e.setNom(rs.getString("nom"));
        e.setDescription(rs.getString("description"));
        e.setQuantiteTotale(rs.getInt("quantite_totale"));
        e.setStatut(rs.getString("statut"));
        Timestamp dc = rs.getTimestamp("date_creation");
        if (dc != null) e.setDateCreation(dc.toLocalDateTime());
        return e;
    }
}