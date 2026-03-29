package com.conference.controller;

import com.conference.dao.NotificationDAO;
import com.conference.model.User;
import com.conference.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;

public class MainController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label notifCountLabel;
    @FXML private BorderPane mainPane;
    @FXML private Button btnDashboard;
    @FXML private Button btnReservations;
    @FXML private Button btnSalles;
    @FXML private Button btnEquipements;
    @FXML private Button btnCalendrier;
    @FXML private Button btnRapports;
    @FXML private Button btnUtilisateurs;
    @FXML private Button btnNotifications;
    @FXML private Button btnLogout;
    @FXML private VBox adminMenu;

    private final NotificationDAO notifDAO = new NotificationDAO();

    @FXML
    public void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Bonjour, " + user.getNomComplet());
        roleLabel.setText(user.getRole());

        // Masquer menus admin si non-admin
        if (!SessionManager.getInstance().isResponsable()) {
            adminMenu.setVisible(false);
            adminMenu.setManaged(false);
        }

        updateNotifCount();
        loadView("/fxml/dashboard.fxml");

        // Boutons sidebar
        btnDashboard.setOnAction(e -> loadView("/fxml/dashboard.fxml"));
        btnReservations.setOnAction(e -> loadView("/fxml/reservations.fxml"));
        btnSalles.setOnAction(e -> loadView("/fxml/salles.fxml"));
        btnEquipements.setOnAction(e -> loadView("/fxml/equipements.fxml"));
        btnCalendrier.setOnAction(e -> loadView("/fxml/calendrier.fxml"));
        btnRapports.setOnAction(e -> loadView("/fxml/rapports.fxml"));
        btnUtilisateurs.setOnAction(e -> loadView("/fxml/utilisateurs.fxml"));
        btnNotifications.setOnAction(e -> {
            loadView("/fxml/notifications.fxml");
            updateNotifCount();
        });
        btnLogout.setOnAction(e -> handleLogout());
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            mainPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateNotifCount() {
        int count = notifDAO.countUnread(
                SessionManager.getInstance().getCurrentUser().getId());
        if (count > 0) {
            notifCountLabel.setText(String.valueOf(count));
            notifCountLabel.setVisible(true);
        } else {
            notifCountLabel.setVisible(false);
        }
    }

    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnLogout.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 800, 500));
            stage.setMaximized(false);
            stage.setTitle("Connexion");
        } catch (IOException e) { e.printStackTrace(); }
    }
}
