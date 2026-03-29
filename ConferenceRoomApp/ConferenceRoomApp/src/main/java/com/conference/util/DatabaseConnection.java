package com.conference.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton pour la gestion de la connexion MySQL.
 * Une seule instance existe dans toute l'application.
 */
public class DatabaseConnection {

    // Logger remplace printStackTrace() (bonne pratique)
    private static final Logger LOGGER =
            Logger.getLogger(DatabaseConnection.class.getName());

    // Instance unique du Singleton
    private static DatabaseConnection instance;

    // La connexion MySQL réutilisable
    private Connection connection;

    // Propriétés lues depuis db.properties
    private final Properties props;

    // ───────��─────────────────────────────────
    // CONSTRUCTEUR PRIVÉ
    // ─────────────────────────────────────────
    private DatabaseConnection() {
        // Initialiser props ICI pour éviter NullPointerException
        props = new Properties();
        loadProperties();
    }

    // ─────────────────────────────────────────
    // OBTENIR L'INSTANCE UNIQUE (Thread-safe)
    // ─────────────────────────────────────────
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // ─────────────────────────────────────────
    // CHARGER db.properties
    // Correction du NullPointerException ligne 34
    // ─────────────────────────────────────────
    private void loadProperties() {
        // Chercher le fichier dans src/main/resources/
        InputStream is = getClass().getResourceAsStream("/db.properties");

        // VÉRIFICATION OBLIGATOIRE avant d'appeler .load()
        // C'est ici que le NullPointerException peut survenir !
        if (is == null) {
            throw new RuntimeException(
                    "ERREUR : db.properties introuvable !\n"
                            + "Verifiez que le fichier existe dans : "
                            + "src/main/resources/db.properties"
            );
        }

        // Maintenant on peut appeler .load() en toute securite
        try {
            props.load(is);
            is.close();
            LOGGER.info("db.properties charge avec succes.");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,
                    "Impossible de lire db.properties", e);
            throw new RuntimeException(
                    "Erreur lecture db.properties : " + e.getMessage()
            );
        }
    }

    // ─────────────────────────────────────────
    // OBTENIR LA CONNEXION MySQL
    // ─────────────────────────────────────────
    public Connection getConnection() throws SQLException {

        // Créer une nouvelle connexion si inexistante ou fermée
        if (connection == null || connection.isClosed()) {

            String url = String.format(
                    "jdbc:mysql://%s:%s/%s"
                            + "?useSSL=false"
                            + "&serverTimezone=%s"
                            + "&allowPublicKeyRetrieval=true"
                            + "&characterEncoding=UTF-8",
                    props.getProperty("db.host"),
                    props.getProperty("db.port"),
                    props.getProperty("db.name"),
                    props.getProperty("db.timezone")
            );

            connection = DriverManager.getConnection(
                    url,
                    props.getProperty("db.user"),
                    props.getProperty("db.password")
            );

            LOGGER.info("Connexion MySQL etablie : "
                    + props.getProperty("db.name"));
        }

        return connection;
    }

    // ─────────────────────────────────────────
    // FERMER LA CONNEXION
    // ─────────────────────────────────────────
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Connexion MySQL fermee.");
            } catch (SQLException e) {
                // Logger remplace e.printStackTrace()
                LOGGER.log(Level.WARNING,
                        "Erreur fermeture connexion", e);
            }
        }
    }

    // ─────────────────────────────────────────
    // OBTENIR LES PROPRIETES
    // Utilisé par EmailService pour mail.*
    // ─────────────────────────────────────────
    public Properties getProps() {
        return props;
    }
}