package com.conference.controller;

import com.conference.dao.EquipementDAO;
import com.conference.model.Equipement;
import com.conference.util.AlertUtil;
import com.conference.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class EquipementsController {

    @FXML private TableView<Equipement> tableEquipements;
    @FXML private TableColumn<Equipement, String>  colNom;
    @FXML private TableColumn<Equipement, String>  colDescription;
    @FXML private TableColumn<Equipement, Integer> colQuantite;
    @FXML private TableColumn<Equipement, String>  colStatut;
    @FXML private Button btnNouvelEquipement;
    @FXML private Button btnModifier;
    @FXML private Button btnSignalerDefaut;
    @FXML private TextField searchField;

    private final EquipementDAO equipementDAO = new EquipementDAO();
    private final ObservableList<Equipement> equipementList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        colDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colQuantite.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getQuantiteTotale()).asObject());
        colStatut.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatut()));

        tableEquipements.setItems(equipementList);
        loadEquipements();

        searchField.textProperty().addListener((obs, o, n) -> applySearch(n));
        btnNouvelEquipement.setOnAction(e -> handleNouvelEquipement());
        btnModifier.setOnAction(e -> handleModifier());
        btnSignalerDefaut.setOnAction(e -> handleSignalerDefaut());
    }

    public void loadEquipements() {
        equipementList.setAll(equipementDAO.findAll());
    }

    private void applySearch(String text) {
        if (text == null || text.isEmpty()) {
            equipementList.setAll(equipementDAO.findAll());
            return;
        }
        String lower = text.toLowerCase();
        equipementList.setAll(equipementDAO.findAll().stream()
                .filter(e -> e.getNom().toLowerCase().contains(lower))
                .toList());
    }

    private void handleNouvelEquipement() {
        showEquipementDialog(null);
    }

    private void handleModifier() {
        Equipement selected = tableEquipements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Sélection", "Veuillez sélectionner un équipement.");
            return;
        }
        showEquipementDialog(selected);
    }

    private void handleSignalerDefaut() {
        Equipement selected = tableEquipements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Sélection", "Veuillez sélectionner un équipement.");
            return;
        }
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Signaler un défaut");
        dlg.setHeaderText("Équipement : " + selected.getNom());
        dlg.setContentText("Description du défaut:");
        dlg.showAndWait().ifPresent(desc -> {
            int userId = SessionManager.getInstance().getCurrentUser().getId();
            boolean ok = equipementDAO.signalerDefectueux(selected.getId(), userId, desc);
            if (ok) AlertUtil.showInfo("Signalement", "Défaut signalé avec succès.");
            else AlertUtil.showError("Erreur", "Impossible de signaler le défaut.");
        });
    }

    private void showEquipementDialog(Equipement eq) {
        Dialog<Equipement> dialog = new Dialog<>();
        dialog.setTitle(eq == null ? "Nouvel équipement" : "Modifier équipement");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nomField = new TextField(eq != null ? eq.getNom() : "");
        TextField descriptionField = new TextField(eq != null ? eq.getDescription() : "");
        TextField quantiteField = new TextField(eq != null ? String.valueOf(eq.getQuantiteTotale()) : "1");
        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("DISPONIBLE", "EN_MAINTENANCE", "HORS_SERVICE");
        statutCombo.setValue(eq != null ? eq.getStatut() : "DISPONIBLE");

        grid.add(new Label("Nom:"), 0, 0); grid.add(nomField, 1, 0);
        grid.add(new Label("Description:"), 0, 1); grid.add(descriptionField, 1, 1);
        grid.add(new Label("Quantité:"), 0, 2); grid.add(quantiteField, 1, 2);
        grid.add(new Label("Statut:"), 0, 3); grid.add(statutCombo, 1, 3);
        dialog.getDialogPane().setContent(grid);

        // Disable OK button when nom is empty or quantite is not a valid integer
        javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(nomField.getText().trim().isEmpty());
        nomField.textProperty().addListener((obs, o, n) -> {
            boolean invalid = n.trim().isEmpty();
            try { Integer.parseInt(quantiteField.getText().trim()); }
            catch (NumberFormatException ex) { invalid = true; }
            okButton.setDisable(invalid);
        });
        quantiteField.textProperty().addListener((obs, o, n) -> {
            boolean invalid = nomField.getText().trim().isEmpty();
            try { Integer.parseInt(n.trim()); }
            catch (NumberFormatException ex) { invalid = true; }
            okButton.setDisable(invalid);
        });

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Equipement e2 = eq != null ? eq : new Equipement();
                e2.setNom(nomField.getText().trim());
                e2.setDescription(descriptionField.getText().trim());
                e2.setQuantiteTotale(Integer.parseInt(quantiteField.getText().trim()));
                e2.setStatut(statutCombo.getValue());
                return e2;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(e2 -> {
            if (e2.getId() == 0) equipementDAO.save(e2);
            else equipementDAO.update(e2);
            loadEquipements();
        });
    }
}
