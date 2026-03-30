package com.conference.controller;

import com.conference.dao.UserDAO;
import com.conference.model.User;
import com.conference.util.AlertUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class UtilisateursController {

    @FXML private TableView<User> tableUtilisateurs;
    @FXML private TableColumn<User, String>  colNom;
    @FXML private TableColumn<User, String>  colPrenom;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colRole;
    @FXML private TableColumn<User, Boolean> colActif;
    @FXML private Button btnNouvelUtilisateur;
    @FXML private Button btnModifier;
    @FXML private Button btnDesactiver;
    @FXML private TextField searchField;

    private final UserDAO userDAO = new UserDAO();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        colPrenom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrenom()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole()));
        colActif.setCellValueFactory(d -> new SimpleBooleanProperty(d.getValue().isActif()).asObject());

        tableUtilisateurs.setItems(userList);
        loadUtilisateurs();

        searchField.textProperty().addListener((obs, o, n) -> applySearch(n));
        btnNouvelUtilisateur.setOnAction(e -> handleNouvelUtilisateur());
        btnModifier.setOnAction(e -> handleModifier());
        btnDesactiver.setOnAction(e -> handleDesactiver());
    }

    public void loadUtilisateurs() {
        userList.setAll(userDAO.findAll());
    }

    private void applySearch(String text) {
        if (text == null || text.isEmpty()) {
            userList.setAll(userDAO.findAll());
            return;
        }
        String lower = text.toLowerCase();
        userList.setAll(userDAO.findAll().stream()
                .filter(u -> u.getNom().toLowerCase().contains(lower)
                        || u.getPrenom().toLowerCase().contains(lower)
                        || u.getEmail().toLowerCase().contains(lower))
                .toList());
    }

    private void handleNouvelUtilisateur() {
        showUserDialog(null);
    }

    private void handleModifier() {
        User selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Sélection", "Veuillez sélectionner un utilisateur.");
            return;
        }
        showUserDialog(selected);
    }

    private void handleDesactiver() {
        User selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Sélection", "Veuillez sélectionner un utilisateur.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Désactiver l'utilisateur \"" + selected.getNomComplet() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            userDAO.delete(selected.getId());
            loadUtilisateurs();
        }
    }

    private void showUserDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Nouvel utilisateur" : "Modifier utilisateur");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nomField = new TextField(user != null ? user.getNom() : "");
        TextField prenomField = new TextField(user != null ? user.getPrenom() : "");
        TextField emailField = new TextField(user != null ? user.getEmail() : "");
        TextField telField = new TextField(user != null ? user.getTelephone() : "");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("UTILISATEUR", "RESPONSABLE", "ADMIN");
        roleCombo.setValue(user != null ? user.getRole() : "UTILISATEUR");
        PasswordField pwField = new PasswordField();

        grid.add(new Label("Nom:"), 0, 0); grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1); grid.add(prenomField, 1, 1);
        grid.add(new Label("Email:"), 0, 2); grid.add(emailField, 1, 2);
        grid.add(new Label("Téléphone:"), 0, 3); grid.add(telField, 1, 3);
        grid.add(new Label("Rôle:"), 0, 4); grid.add(roleCombo, 1, 4);
        if (user == null) {
            grid.add(new Label("Mot de passe:"), 0, 5); grid.add(pwField, 1, 5);
        }
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                User u = user != null ? user : new User();
                u.setNom(nomField.getText().trim());
                u.setPrenom(prenomField.getText().trim());
                u.setEmail(emailField.getText().trim());
                u.setTelephone(telField.getText().trim());
                u.setRole(roleCombo.getValue());
                u.setActif(true);
                if (user == null) u.setMotDePasse(pwField.getText());
                return u;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(u -> {
            if (u.getId() == 0) userDAO.save(u);
            else userDAO.update(u);
            loadUtilisateurs();
        });
    }
}
