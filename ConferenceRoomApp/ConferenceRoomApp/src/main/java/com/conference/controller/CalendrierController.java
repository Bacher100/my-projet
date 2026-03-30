package com.conference.controller;

import com.conference.dao.SalleDAO;
import com.conference.model.Reservation;
import com.conference.model.Salle;
import com.conference.service.ReservationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class CalendrierController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Salle> salleFilter;
    @FXML private TableView<Reservation> tableCalendrier;
    @FXML private TableColumn<Reservation, String> colTitre;
    @FXML private TableColumn<Reservation, String> colSalle;
    @FXML private TableColumn<Reservation, String> colDebut;
    @FXML private TableColumn<Reservation, String> colFin;
    @FXML private TableColumn<Reservation, String> colStatut;

    private final ReservationService reservationService = new ReservationService();
    private final SalleDAO salleDAO = new SalleDAO();
    private final ObservableList<Reservation> reservationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colTitre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitre()));
        colSalle.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSalle() != null ? d.getValue().getSalle().getNom() : ""));
        colDebut.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateDebutFormatted()));
        colFin.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateFinFormatted()));
        colStatut.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatut()));

        tableCalendrier.setItems(reservationList);

        salleFilter.getItems().add(null);
        salleFilter.getItems().addAll(salleDAO.findAll());
        salleFilter.setPromptText("Toutes les salles");

        datePicker.setValue(LocalDate.now());
        loadCalendrier();

        datePicker.valueProperty().addListener((obs, o, n) -> loadCalendrier());
        salleFilter.valueProperty().addListener((obs, o, n) -> loadCalendrier());
    }

    public void loadCalendrier() {
        applyFilters();
    }

    @FXML
    public void applyFilters() {
        List<Reservation> all = reservationService.getToutesReservations();
        LocalDate date = datePicker.getValue();
        Salle salle = salleFilter.getValue();

        List<Reservation> filtered = all.stream()
                .filter(r -> date == null || (r.getDateDebut() != null &&
                        r.getDateDebut().toLocalDate().equals(date)))
                .filter(r -> salle == null || r.getSalleId() == salle.getId())
                .collect(Collectors.toList());

        reservationList.setAll(filtered);
    }
}
