package com.conference;

// ✅ Import correct du package util
import com.conference.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Main extends Application {

    private static final Logger LOGGER =
            Logger.getLogger(Main.class.getName());

    // ─────────────────────────────────────────
    // DÉMARRAGE DE L'APPLICATION
    // ─────────────────────────────────────────
    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger la page de connexion
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 850, 550);

            // ✅ CSS avec vérification null
            var cssUrl = getClass()
                    .getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(
                        cssUrl.toExternalForm());
            } else {
                LOGGER.warning(
                        "style.css introuvable dans resources/css/");
            }

            // Configurer la fenetre
            primaryStage.setTitle(
                    "Gestion des Salles de Conference");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,
                    "Erreur chargement login.fxml", e);

            // Afficher une alerte si FXML introuvable
            javafx.scene.control.Alert alert =
                    new javafx.scene.control.Alert(
                            javafx.scene.control.Alert
                                    .AlertType.ERROR);
            alert.setTitle("Erreur de demarrage");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Impossible de charger l'interface.\n"
                            + "Verifiez que login.fxml existe dans "
                            + "src/main/resources/fxml/\n\n"
                            + e.getMessage());
            alert.showAndWait();
        }
    }

    // ─────────────────────────────────────────
    // ARRÊT PROPRE DE L'APPLICATION
    // ─────────────────────────────────────────
    @Override
    public void stop() {
        try {
            // ✅ Fermer proprement la connexion MySQL
            DatabaseConnection.getInstance()
                    .closeConnection();
            LOGGER.info("Application fermee proprement.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,
                    "Erreur fermeture connexion", e);
        }
    }

    // ─────────────────────────────────────────
    // POINT D'ENTRÉE
    // ─────────────────────────────────────────
    public static void main(String[] args) {
        launch(args);
    }
}