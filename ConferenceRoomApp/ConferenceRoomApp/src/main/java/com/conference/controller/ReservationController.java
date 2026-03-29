package com.conference.controller;

import com.conference.model.Reservation;
import com.conference.service.ReservationService;
import com.conference.util.AlertUtil;
import com.conference.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ReservationController {

    // ─────────────────────────────────────────
    // COMPOSANTS FXML
    // ─────────────────────────────────────────
    @FXML private TableView<Reservation>             tableReservations;
    @FXML private TableColumn<Reservation, String>   colTitre;
    @FXML private TableColumn<Reservation, String>   colSalle;
    @FXML private TableColumn<Reservation, String>   colDebut;
    @FXML private TableColumn<Reservation, String>   colFin;
    @FXML private TableColumn<Reservation, String>   colStatut;
    @FXML private TableColumn<Reservation, Integer>  colParticipants;
    @FXML private Button                             btnNouvelleReservation;
    @FXML private Button                             btnModifier;
    @FXML private Button                             btnAnnuler;
    @FXML private ComboBox<String>                   filtreStatut;
    @FXML private TextField                          searchField;
    @FXML private Label                              totalLabel;

    // ─────────────────────────────────────────
    // DONNÉES
    // ─────────────────────────────────────────
    private final ReservationService service =
            new ReservationService();
    private final ObservableList<Reservation> reservationList =
            FXCollections.observableArrayList();

    // ─────────────────────────────────────────
    // INITIALISATION
    // ─────────────────────────────────────────
    @FXML
    public void initialize() {
        setupColumns();
        setupStatutCell();
        setupFilters();
        setupButtons();
        tableReservations.setItems(reservationList);
        loadReservations();
    }

    // ─────────────────────────────────────────
    // COLONNES DU TABLEAU
    // ─────────────────────────────────────────
    private void setupColumns() {

        colTitre.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getTitre()));

        colSalle.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getSalle() != null
                                ? data.getValue().getSalle().getNom()
                                : ""));

        colDebut.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDateDebutFormatted()));

        colFin.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDateFinFormatted()));

        colStatut.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getStatut()));

        colParticipants.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue().getNombreParticipants())
                        .asObject());
    }

    // ─────────────────────────────────────────
    // COLORATION DES STATUTS
    // ✅ if/else classique (compatible Java 11+)
    //    Pas de switch expression (Java 14+)
    // ─────────────────────────────────────────
    private void setupStatutCell() {
        colStatut.setCellFactory(col ->
                new TableCell<Reservation, String>() {
                    @Override
                    protected void updateItem(String item,
                                              boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                            return;
                        }
                        setText(item);

                        // ✅ if/else au lieu de switch expression
                        String color;
                        if ("CONFIRMEE".equals(item)) {
                            color = "#27ae60"; // vert
                        } else if ("EN_ATTENTE".equals(item)) {
                            color = "#f39c12"; // orange
                        } else if ("ANNULEE".equals(item)) {
                            color = "#e74c3c"; // rouge
                        } else if ("TERMINEE".equals(item)) {
                            color = "#95a5a6"; // gris
                        } else {
                            color = "#2c3e50"; // defaut
                        }

                        setStyle("-fx-text-fill:" + color
                                + "; -fx-font-weight:bold;");
                    }
                });
    }

    // ─────────────────────────────────────────
    // FILTRES
    // ─────────────────────────────────────────
    private void setupFilters() {
        filtreStatut.setItems(
                FXCollections.observableArrayList(
                        "TOUS", "EN_ATTENTE", "CONFIRMEE",
                        "ANNULEE", "TERMINEE"));
        filtreStatut.setValue("TOUS");
        filtreStatut.setOnAction(e -> applyFilters());

        searchField.textProperty().addListener(
                (obs, oldVal, newVal) -> applyFilters());
    }

    // ─────────────────────────────────────────
    // BOUTONS
    // ─────────────────────────────────────────
    private void setupButtons() {
        btnNouvelleReservation.setOnAction(
                e -> openForm(null));

        btnModifier.setOnAction(e -> {
            Reservation sel = getSelected();
            if (sel != null) {
                openForm(sel);
            } else {
                AlertUtil.showWarning("Selection",
                        "Veuillez selectionner "
                                + "une reservation.");
            }
        });

        btnAnnuler.setOnAction(
                e -> handleAnnuler());
    }

    // ─────────────────────────────────────────
    // CHARGER LES RÉSERVATIONS
    // ─────────────────────────────────────────
    private void loadReservations() {
        List<Reservation> list;
        if (SessionManager.getInstance().isAdmin()) {
            list = service.getToutesReservations();
        } else {
            list = service.getMesReservations(
                    SessionManager.getInstance()
                            .getCurrentUser().getId());
        }
        reservationList.setAll(list);
        updateTotal(list.size());
    }

    // ─────────────────────────────────────────
    // APPLIQUER LES FILTRES
    // ─────────────────────────────────────────
    private void applyFilters() {
        String statut  = filtreStatut.getValue();
        String search  = searchField.getText()
                .toLowerCase().trim();

        List<Reservation> source;
        if (SessionManager.getInstance().isAdmin()) {
            source = service.getToutesReservations();
        } else {
            source = service.getMesReservations(
                    SessionManager.getInstance()
                            .getCurrentUser().getId());
        }

        List<Reservation> filtered = source.stream()
                .filter(r -> "TOUS".equals(statut)
                        || statut.equals(r.getStatut()))
                .filter(r -> {
                    if (search.isEmpty()) return true;
                    boolean t = r.getTitre() != null
                            && r.getTitre().toLowerCase()
                            .contains(search);
                    boolean s = r.getSalle() != null
                            && r.getSalle().getNom()
                            .toLowerCase().contains(search);
                    return t || s;
                })
                .collect(Collectors.toList());

        reservationList.setAll(filtered);
        updateTotal(filtered.size());
    }

    // ─────────────────────────────────────────
    // OUVRIR LE FORMULAIRE
    // ─────────────────────────────────────────
    private void openForm(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/reservation_form.fxml"));
            Parent root = loader.load();

            ReservationFormController ctrl =
                    loader.getController();
            ctrl.setReservation(reservation);
            ctrl.setOnSaved(this::loadReservations);

            Stage stage = new Stage();
            stage.initModality(
                    Modality.APPLICATION_MODAL);
            stage.setTitle(reservation == null
                    ? "Nouvelle Reservation"
                    : "Modifier Reservation");
            stage.setScene(new Scene(root, 750, 650));
            stage.showAndWait();

        } catch (IOException e) {
            AlertUtil.showError("Erreur",
                    "Impossible d'ouvrir le formulaire : "
                            + e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    // ANNULER UNE RÉSERVATION
    // ─────────────────────────────────────────
    private void handleAnnuler() {
        Reservation sel = getSelected();

        if (sel == null) {
            AlertUtil.showWarning("Selection",
                    "Veuillez selectionner "
                            + "une reservation.");
            return;
        }

        if ("ANNULEE".equals(sel.getStatut())
                || "TERMINEE".equals(sel.getStatut())) {
            AlertUtil.showWarning("Action impossible",
                    "Cette reservation ne peut pas "
                            + "etre annulee.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
                "Annulation",
                "Annuler la reservation : "
                        + sel.getTitre() + " ?");

        if (!confirmed) return;

        boolean ok = service.annulerReservation(
                sel.getId(),
                SessionManager.getInstance()
                        .getCurrentUser().getId());

        if (ok) {
            AlertUtil.showInfo("Succes",
                    "Reservation annulee.");
            loadReservations();
        } else {
            AlertUtil.showError("Erreur",
                    "Impossible d'annuler.");
        }
    }

    // ─────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────
    private Reservation getSelected() {
        return tableReservations
                .getSelectionModel().getSelectedItem();
    }

    private void updateTotal(int count) {
        if (totalLabel != null) {
            totalLabel.setText(
                    "Total : " + count
                            + " reservation(s)");
        }
    }
}