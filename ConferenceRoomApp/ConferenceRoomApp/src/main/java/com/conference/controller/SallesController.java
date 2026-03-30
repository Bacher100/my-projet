package com.conference.controller;

import com.conference.dao.SalleDAO;
import com.conference.model.Salle;
import com.conference.util.AlertUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

public class SallesController {

    @FXML private TableView<Salle> tableSalles;
    @FXML private TableColumn<Salle, String>  colNom;
    @FXML private TableColumn<Salle, Integer> colCapacite;
    @FXML private TableColumn<Salle, String>  colLocalisation;
    @FXML private TableColumn<Salle, String>  colDescription;
    @FXML private TableColumn<Salle, Boolean> colActive;
    @FXML private Button btnNouvelleSalle;
    @FXML private Button btnModifier;
    @FXML private Button btnDesactiver;
    @FXML private TextField searchField;

    private final SalleDAO salleDAO = new SalleDAO();
    private final ObservableList<Salle> salleList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        colCapacite.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCapacite()).asObject());
        colLocalisation.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocalisation()));
        colDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colActive.setCellValueFactory(d -> new SimpleBooleanProperty(d.getValue().isActive()).asObject());

        tableSalles.setItems(salleList);
        loadSalles();

        searchField.textProperty().addListener((obs, o, n) -> applySearch(n));
        btnNouvelleSalle.setOnAction(e -> handleNouvelleSalle());
        btnModifier.setOnAction(e -> handleModifier());
        btnDesactiver.setOnAction(e -> handleDesactiver());
    }

    public void loadSalles() {
        List<Salle> all = salleDAO.findAllIncludingInactive();
        salleList.setAll(all);
    }

    private void applySearch(String text) {
        if (text == null || text.isEmpty()) {
            salleList.setAll(salleDAO.findAllIncludingInactive());
            return;
        }
        String lower = text.toLowerCase();
        salleList.setAll(salleDAO.findAllIncludingInactive().stream()
                .filter(s -> s.getNom().toLowerCase().contains(lower)
                        || (s.getLocalisation() != null && s.getLocalisation().toLowerCase().contains(lower)))
                .toList());
    }

    private void handleNouvelleSalle() {
        showSalleDialog(null);
    }

    private void handleModifier() {
        Salle selected = tableSalles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Sélection", "Veuillez sélectionner une salle.");
            return;
        }
        showSalleDialog(selected);
    }

    private void handleDesactiver() {
        Salle selected = tableSalles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Sélection", "Veuillez sélectionner une salle.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Désactiver la salle \"" + selected.getNom() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            salleDAO.delete(selected.getId());
            loadSalles();
        }
    }

    private void showSalleDialog(Salle salle) {
        Dialog<Salle> dialog = new Dialog<>();
        dialog.setTitle(salle == null ? "Nouvelle salle" : "Modifier salle");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nomField = new TextField(salle != null ? salle.getNom() : "");
        TextField capaciteField = new TextField(salle != null ? String.valueOf(salle.getCapacite()) : "");
        TextField localisationField = new TextField(salle != null ? salle.getLocalisation() : "");
        TextField descriptionField = new TextField(salle != null ? salle.getDescription() : "");

        grid.add(new Label("Nom:"), 0, 0); grid.add(nomField, 1, 0);
        grid.add(new Label("Capacité:"), 0, 1); grid.add(capaciteField, 1, 1);
        grid.add(new Label("Localisation:"), 0, 2); grid.add(localisationField, 1, 2);
        grid.add(new Label("Description:"), 0, 3); grid.add(descriptionField, 1, 3);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Salle s = salle != null ? salle : new Salle();
                s.setNom(nomField.getText().trim());
                try { s.setCapacite(Integer.parseInt(capaciteField.getText().trim())); }
                catch (NumberFormatException ex) {
                    AlertUtil.showError("Saisie invalide", "La capacité doit être un nombre entier valide.");
                    return null;
                }
                s.setLocalisation(localisationField.getText().trim());
                s.setDescription(descriptionField.getText().trim());
                s.setActive(true);
                return s;
            }
            return null;
        });

        Optional<Salle> result = dialog.showAndWait();
        result.ifPresent(s -> {
            if (s.getId() == 0) salleDAO.save(s);
            else salleDAO.update(s);
            loadSalles();
        });
    }
}
