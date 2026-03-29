package com.conference.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Salle {

    private int id;
    private String nom;
    private int capacite;
    private String localisation;
    private String description;
    private boolean active;
    private LocalDateTime dateCreation;
    private List<Equipement> equipements = new ArrayList<>();

    public Salle() {
    }

    public Salle(int id, String nom, int capacite,
                 String localisation, String description,
                 boolean active) {
        this.id           = id;
        this.nom          = nom;
        this.capacite     = capacite;
        this.localisation = localisation;
        this.description  = description;
        this.active       = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public List<Equipement> getEquipements() { return equipements; }
    public void setEquipements(List<Equipement> equipements) {
        this.equipements = equipements;
    }

    @Override
    public String toString() {
        return nom + " (Cap: " + capacite + ")";
    }
}