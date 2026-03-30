package com.conference.controller;

import com.conference.dao.UserDAO;
import com.conference.model.User;
import com.conference.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button btnCreerCompte;
    @FXML private Button btnAnnuler;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        btnCreerCompte.setOnAction(e -> handleCreerCompte());
        btnAnnuler.setOnAction(e -> handleAnnuler());
    }

    @FXML
    public void handleCreerCompte() {
        if (!validateForm()) return;

        String email = emailField.getText().trim();
        if (userDAO.emailExists(email)) {
            showError("Cet email est déjà utilisé.");
            return;
        }

        User user = new User();
        user.setNom(nomField.getText().trim());
        user.setPrenom(prenomField.getText().trim());
        user.setEmail(email);
        user.setTelephone(telephoneField.getText().trim());
        user.setMotDePasse(passwordField.getText());
        user.setRole("UTILISATEUR");
        user.setActif(true);

        if (userDAO.save(user)) {
            AlertUtil.showInfo("Succès", "Compte créé avec succès ! Vous pouvez maintenant vous connecter.");
            handleAnnuler();
        } else {
            showError("Erreur lors de la création du compte.");
        }
    }

    @FXML
    public void handleAnnuler() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    public boolean validateForm() {
        if (nomField.getText().trim().isEmpty()) {
            showError("Le nom est obligatoire."); return false;
        }
        if (prenomField.getText().trim().isEmpty()) {
            showError("Le prénom est obligatoire."); return false;
        }
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showError("Email invalide."); return false;
        }
        if (passwordField.getText().length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères."); return false;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas."); return false;
        }
        return true;
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
