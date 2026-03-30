-- ============================================================
-- Base de données : conference_reservation
-- ============================================================

CREATE DATABASE IF NOT EXISTS conference_reservation
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE conference_reservation;

-- ── Utilisateurs ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    nom                VARCHAR(100) NOT NULL,
    prenom             VARCHAR(100) NOT NULL,
    email              VARCHAR(200) NOT NULL UNIQUE,
    mot_de_passe       VARCHAR(255) NOT NULL,
    role               ENUM('ADMIN','RESPONSABLE','UTILISATEUR') NOT NULL DEFAULT 'UTILISATEUR',
    telephone          VARCHAR(20),
    actif              BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    derniere_connexion DATETIME
);

-- ── Salles ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS salles (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nom            VARCHAR(150) NOT NULL,
    capacite       INT NOT NULL DEFAULT 0,
    localisation   VARCHAR(200),
    description    TEXT,
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ── Équipements ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS equipements (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    nom              VARCHAR(150) NOT NULL,
    description      TEXT,
    quantite_totale  INT NOT NULL DEFAULT 1,
    statut           ENUM('DISPONIBLE','EN_MAINTENANCE','HORS_SERVICE') NOT NULL DEFAULT 'DISPONIBLE',
    date_creation    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ── Équipements par salle ─────────────────────────────────────
CREATE TABLE IF NOT EXISTS salle_equipements (
    salle_id      INT NOT NULL,
    equipement_id INT NOT NULL,
    quantite      INT NOT NULL DEFAULT 1,
    PRIMARY KEY (salle_id, equipement_id),
    FOREIGN KEY (salle_id)      REFERENCES salles(id)      ON DELETE CASCADE,
    FOREIGN KEY (equipement_id) REFERENCES equipements(id) ON DELETE CASCADE
);

-- ── Réservations ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reservations (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id      INT NOT NULL,
    salle_id            INT NOT NULL,
    titre               VARCHAR(200) NOT NULL,
    description         TEXT,
    date_debut          DATETIME NOT NULL,
    date_fin            DATETIME NOT NULL,
    nombre_participants INT NOT NULL DEFAULT 1,
    statut              ENUM('EN_ATTENTE','CONFIRMEE','ANNULEE','TERMINEE') NOT NULL DEFAULT 'EN_ATTENTE',
    disposition         VARCHAR(100),
    date_creation       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification   DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES users(id)  ON DELETE CASCADE,
    FOREIGN KEY (salle_id)       REFERENCES salles(id) ON DELETE CASCADE
);

-- ── Équipements par réservation ───────────────────────────────
CREATE TABLE IF NOT EXISTS reservation_equipements (
    reservation_id INT NOT NULL,
    equipement_id  INT NOT NULL,
    quantite       INT NOT NULL DEFAULT 1,
    PRIMARY KEY (reservation_id, equipement_id),
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (equipement_id)  REFERENCES equipements(id)  ON DELETE CASCADE
);

-- ── Notifications ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS notifications (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    reservation_id INT,
    titre          VARCHAR(200) NOT NULL,
    message        TEXT,
    type           VARCHAR(50),
    lue            BOOLEAN NOT NULL DEFAULT FALSE,
    date_creation  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES users(id)         ON DELETE CASCADE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id)  ON DELETE SET NULL
);

-- ── Signalements ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS signalements (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    equipement_id  INT NOT NULL,
    utilisateur_id INT NOT NULL,
    description    TEXT,
    date_creation  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (equipement_id)  REFERENCES equipements(id) ON DELETE CASCADE,
    FOREIGN KEY (utilisateur_id) REFERENCES users(id)       ON DELETE CASCADE
);

-- ============================================================
-- Données de démarrage
-- ============================================================

-- Admin (mot de passe : Admin123!)
INSERT IGNORE INTO users (nom, prenom, email, mot_de_passe, role, telephone, actif)
VALUES ('Admin', 'Super', 'admin@conference.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'ADMIN', '0600000000', TRUE);

-- Salles exemples
INSERT IGNORE INTO salles (nom, capacite, localisation, description, active) VALUES
('Salle Horizon', 20, 'Bâtiment A - 1er étage', 'Salle lumineuse avec vue panoramique', TRUE),
('Salle Zen', 10, 'Bâtiment B - RDC', 'Petite salle calme pour réunions intimes', TRUE),
('Grande Salle Conférence', 80, 'Bâtiment C - Sous-sol', 'Amphithéâtre équipé pour grandes réunions', TRUE);

-- Équipements exemples
INSERT IGNORE INTO equipements (nom, description, quantite_totale, statut) VALUES
('Projecteur HD', 'Vidéoprojecteur haute définition', 5, 'DISPONIBLE'),
('Tableau blanc', 'Tableau magnétique effaçable', 10, 'DISPONIBLE'),
('Système audio', 'Microphone + enceintes', 3, 'DISPONIBLE'),
('Webcam 4K', 'Caméra pour visioconférence', 4, 'DISPONIBLE');
