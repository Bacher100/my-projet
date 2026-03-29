package com.conference.controller;

import com.conference.dao.EquipementDAO;
import com.conference.dao.SalleDAO;
import com.conference.model.Equipement;
import com.conference.model.Reservation;
import com.conference.model.Salle;
import com.conference.service.ReservationService;
import com.conference.util.AlertUtil;
import com.conference.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ReservationFormController {

    @FXML private TextField            titreField;
    @FXML private TextArea             descriptionArea;
    @FXML private ComboBox<Salle>      salleCombo;
    @FXML private DatePicker           datePicker;
    @FXML private Spinner<Integer>     heureDebutSpinner;
    @FXML private Spinner<Integer>     minuteDebutSpinner;
    @FXML private Spinner<Integer>     heureFinSpinner;
    @FXML private Spinner<Integer>     minuteFinSpinner;
    @FXML private Spinner<Integer>     participantsSpinner;
    @FXML private ComboBox<String>     dispositionCombo;
    @FXML private ListView<Equipement> equipementsListView;
    @FXML private Button               btnSauvegarder;
    @FXML private Button               btnAnnuler;
    @FXML private Label                salleInfoLabel;

    private final ReservationService service =
            new ReservationService();
    private final SalleDAO     salleDAO = new SalleDAO();
    private final EquipementDAO equipDAO = new EquipementDAO();

    private Reservation reservation;
    private Runnable    onSaved;

    // ─────────────────────────────────────────
    // INITIALISATION
    // ─────────────────────────────────────────
    @FXML
    public void initialize() {

        // Charger les salles
        salleCombo.setItems(FXCollections.observableArrayList(
                salleDAO.findAll()));
        salleCombo.setOnAction(e -> updateSalleInfo());

        // Dispositions
        dispositionCombo.setItems(
                FXCollections.observableArrayList(
                        "THEATRE", "CLASSE", "U_SHAPE",
                        "BANQUET", "COCKTAIL"));
        dispositionCombo.setValue("THEATRE");

        // Spinners heures
        heureDebutSpinner.setValueFactory(
                new SpinnerValueFactory
                        .IntegerSpinnerValueFactory(0, 23, 9));
        minuteDebutSpinner.setValueFactory(
                new SpinnerValueFactory
                        .IntegerSpinnerValueFactory(0, 59, 0, 15));
        heureFinSpinner.setValueFactory(
                new SpinnerValueFactory
                        .IntegerSpinnerValueFactory(0, 23, 10));
        minuteFinSpinner.setValueFactory(
                new SpinnerValueFactory
                        .IntegerSpinnerValueFactory(0, 59, 0, 15));

        // Spinner participants
        participantsSpinner.setValueFactory(
                new SpinnerValueFactory
                        .IntegerSpinnerValueFactory(1, 500, 1));

        // Date par defaut : demain
        datePicker.setValue(
                LocalDate.now().plusDays(1));

        // Equipements disponibles
        equipementsListView.setItems(
                FXCollections.observableArrayList(
                        equipDAO.findDisponibles()));
        equipementsListView.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);

        // Boutons
        btnSauvegarder.setOnAction(e -> handleSave());
        btnAnnuler.setOnAction(e -> close());
    }

    // ─────────────────────────────────────────
    // SETTER : passer la réservation à modifier
    // (null = nouvelle réservation)
    // ─────────────────────────────────────────
    public void setReservation(Reservation r) {
        this.reservation = r;
        if (r == null) return;

        titreField.setText(r.getTitre());
        descriptionArea.setText(r.getDescription());
        datePicker.setValue(
                r.getDateDebut().toLocalDate());
        heureDebutSpinner.getValueFactory()
                .setValue(r.getDateDebut().getHour());
        minuteDebutSpinner.getValueFactory()
                .setValue(r.getDateDebut().getMinute());
        heureFinSpinner.getValueFactory()
                .setValue(r.getDateFin().getHour());
        minuteFinSpinner.getValueFactory()
                .setValue(r.getDateFin().getMinute());
        participantsSpinner.getValueFactory()
                .setValue(r.getNombreParticipants());

        if (r.getDisposition() != null) {
            dispositionCombo.setValue(r.getDisposition());
        }

        // Selectionner la salle correspondante
        salleCombo.getItems().stream()
                .filter(s -> s.getId() == r.getSalleId())
                .findFirst()
                .ifPresent(s -> salleCombo.setValue(s));
    }

    // ─────────────────────────────────────────
    // SETTER : callback après sauvegarde
    // ─────────────────────────────────────────
    public void setOnSaved(Runnable callback) {
        this.onSaved = callback;
    }

    // ─────────────────────────────────────────
    // SAUVEGARDER
    // ─────────────────────────────────────────
    private void handleSave() {
        if (!validateForm()) return;

        Salle salle = salleCombo.getValue();
        LocalDate date = datePicker.getValue();

        LocalDateTime debut = LocalDateTime.of(date,
                LocalTime.of(
                        heureDebutSpinner.getValue(),
                        minuteDebutSpinner.getValue()));
        LocalDateTime fin = LocalDateTime.of(date,
                LocalTime.of(
                        heureFinSpinner.getValue(),
                        minuteFinSpinner.getValue()));

        if (reservation == null) {
            reservation = new Reservation();
        }

        reservation.setTitre(
                titreField.getText().trim());
        reservation.setDescription(
                descriptionArea.getText().trim());
        reservation.setSalleId(salle.getId());
        reservation.setSalle(salle);
        reservation.setDateDebut(debut);
        reservation.setDateFin(fin);
        reservation.setNombreParticipants(
                participantsSpinner.getValue());
        reservation.setDisposition(
                dispositionCombo.getValue());
        reservation.setUtilisateurId(
                SessionManager.getInstance()
                        .getCurrentUser().getId());
        reservation.setUtilisateur(
                SessionManager.getInstance().getCurrentUser());

        List<Equipement> equips = new ArrayList<>(
                equipementsListView.getSelectionModel()
                        .getSelectedItems());
        reservation.setEquipements(equips);

        boolean isNew = (reservation.getId() == 0);

        ReservationService.ReservationResult result;
        if (isNew) {
            result = service.creerReservation(reservation);
        } else {
            result = service.modifierReservation(reservation);
        }

        handleResult(result, isNew);
    }

    private void handleResult(
            ReservationService.ReservationResult result,
            boolean isNew) {

        if (result == ReservationService
                .ReservationResult.SUCCESS) {
            AlertUtil.showInfo("Succes",
                    "Reservation "
                            + (isNew ? "creee" : "modifiee")
                            + " avec succes !");
            if (onSaved != null) onSaved.run();
            close();

        } else if (result == ReservationService
                .ReservationResult.CONFLICT) {
            AlertUtil.showError("Conflit",
                    "La salle est deja reservee "
                            + "pour cette periode.");

        } else if (result == ReservationService
                .ReservationResult.INVALID_DATES) {
            AlertUtil.showError("Dates invalides",
                    "Verifiez les dates et heures.");

        } else {
            AlertUtil.showError("Erreur",
                    "Une erreur est survenue.");
        }
    }

    // ─────────────────────────────────────────
    // VALIDATION
    // ─────────────────────────────────────────
    private boolean validateForm() {
        if (titreField.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validation",
                    "Le titre est obligatoire.");
            return false;
        }
        if (salleCombo.getValue() == null) {
            AlertUtil.showWarning("Validation",
                    "Veuillez selectionner une salle.");
            return false;
        }
        if (datePicker.getValue() == null) {
            AlertUtil.showWarning("Validation",
                    "Veuillez selectionner une date.");
            return false;
        }

        LocalDate date = datePicker.getValue();
        LocalDateTime debut = LocalDateTime.of(date,
                LocalTime.of(
                        heureDebutSpinner.getValue(),
                        minuteDebutSpinner.getValue()));
        LocalDateTime fin = LocalDateTime.of(date,
                LocalTime.of(
                        heureFinSpinner.getValue(),
                        minuteFinSpinner.getValue()));

        if (!debut.isBefore(fin)) {
            AlertUtil.showWarning("Validation",
                    "L'heure de fin doit etre apres "
                            + "l'heure de debut.");
            return false;
        }
        if (debut.isBefore(LocalDateTime.now())) {
            AlertUtil.showWarning("Validation",
                    "La date de debut ne peut pas "
                            + "etre dans le passe.");
            return false;
        }
        return true;
    }

    // ─────────────────────────────────────────
    // INFO SALLE
    // ─────────────────────────────────────────
    private void updateSalleInfo() {
        Salle s = salleCombo.getValue();
        if (s != null && salleInfoLabel != null) {
            salleInfoLabel.setText(
                    s.getLocalisation()
                            + "  |  Capacite: " + s.getCapacite()
                            + "  |  Equipements: "
                            + s.getEquipements().size());
        }
    }

    // ─────────────────────────────────────────
    // FERMER LA FENETRE
    // ─────────────────────────────────────────
    private void close() {
        Stage stage = (Stage) btnAnnuler
                .getScene().getWindow();
        stage.close();
    }
}
