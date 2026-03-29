package com.conference.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Reservation {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private int utilisateurId;
    private int salleId;
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private int nombreParticipants;
    private String statut;
    private String disposition;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private User utilisateur;
    private Salle salle;
    private List<Equipement> equipements = new ArrayList<>();

    public Reservation() {
    }

    public String getDateDebutFormatted() {
        return dateDebut != null
                ? dateDebut.format(FORMATTER) : "";
    }

    public String getDateFinFormatted() {
        return dateFin != null
                ? dateFin.format(FORMATTER) : "";
    }

    public long getDureeMinutes() {
        if (dateDebut != null && dateFin != null) {
            return java.time.Duration
                    .between(dateDebut, dateFin).toMinutes();
        }
        return 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public int getSalleId() { return salleId; }
    public void setSalleId(int salleId) {
        this.salleId = salleId;
    }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public int getNombreParticipants() {
        return nombreParticipants;
    }
    public void setNombreParticipants(int nombreParticipants) {
        this.nombreParticipants = nombreParticipants;
    }

    public String getStatut() { return statut; }
    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDisposition() { return disposition; }
    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }
    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public User getUtilisateur() { return utilisateur; }
    public void setUtilisateur(User utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }

    public List<Equipement> getEquipements() { return equipements; }
    public void setEquipements(List<Equipement> equipements) {
        this.equipements = equipements;
    }
}