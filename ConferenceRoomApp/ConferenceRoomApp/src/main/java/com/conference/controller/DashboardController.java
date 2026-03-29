package com.conference.controller;

import com.conference.service.ReservationService;
import com.conference.dao.SalleDAO;
import com.conference.dao.UserDAO;
import com.conference.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

public class DashboardController {

    @FXML private Label lblTotalReservations;
    @FXML private Label lblReservationsAujourdhui;
    @FXML private Label lblSallesActives;
    @FXML private Label lblUtilisateurs;
    @FXML private Label lblReservationsConfirmees;
    @FXML private Label lblReservationsAnnulees;
    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;

    private final ReservationService reservationService = new ReservationService();
    private final SalleDAO salleDAO = new SalleDAO();
    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        loadStats();
        loadBarChart();
        loadPieChart();
    }

    private void loadStats() {
        lblTotalReservations.setText(
                String.valueOf(reservationService.countTotal()));
        lblReservationsConfirmees.setText(
                String.valueOf(reservationService.countByStatut("CONFIRMEE")));
        lblReservationsAnnulees.setText(
                String.valueOf(reservationService.countByStatut("ANNULEE")));
        lblSallesActives.setText(
                String.valueOf(salleDAO.findAll().size()));

        if (SessionManager.getInstance().isAdmin()) {
            lblUtilisateurs.setText(
                    String.valueOf(userDAO.findAll().size()));
        }

        // Réservations aujourd'hui
        long today = reservationService.getToutesReservations().stream()
                .filter(r -> r.getDateDebut() != null &&
                        r.getDateDebut().toLocalDate().equals(
                                LocalDateTime.now().toLocalDate()))
                .count();
        lblReservationsAujourdhui.setText(String.valueOf(today));
    }

    private void loadBarChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réservations " + LocalDateTime.now().getYear());

        List<Object[]> stats = reservationService.getStatsByMonth(
                LocalDateTime.now().getYear());
        String[] months = {"Jan","Fév","Mar","Avr","Mai","Jun",
                "Jul","Aoû","Sep","Oct","Nov","Déc"};
        int[] counts = new int[12];
        for (Object[] row : stats)
            counts[(int) row[0] - 1] = (int) row[1];
        for (int i = 0; i < 12; i++)
            series.getData().add(new XYChart.Data<>(months[i], counts[i]));

        barChart.getData().clear();
        barChart.getData().add(series);
        barChart.setTitle("Réservations par mois");
    }

    private void loadPieChart() {
        List<Object[]> topSalles = reservationService.getTopSalles(5);
        pieChart.getData().clear();
        for (Object[] row : topSalles) {
            pieChart.getData().add(
                    new PieChart.Data((String) row[0], (int) row[1]));
        }
        pieChart.setTitle("Top 5 Salles");
    }
}
