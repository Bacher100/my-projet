package com.conference.controller;

import com.conference.model.User;
import com.conference.dao.UserDAO;
import com.conference.service.ReminderService;
import com.conference.util.AlertUtil;
import com.conference.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink registerLink;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        loginButton.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());
        registerLink.setOnAction(e -> openRegister());
    }

    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        User user = userDAO.authenticate(email, password);
        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            ReminderService.getInstance().start();
            openMainWindow();
        } else {
            showError("Email ou mot de passe incorrect.");
            passwordField.clear();
        }
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
            stage.setTitle("Gestion des Salles de Conférence");
            stage.setMaximized(true);
        } catch (IOException e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir la fenêtre principale.");
            e.printStackTrace();
        }
    }

    private void openRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Créer un compte");
            stage.setScene(new Scene(root, 500, 600));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}