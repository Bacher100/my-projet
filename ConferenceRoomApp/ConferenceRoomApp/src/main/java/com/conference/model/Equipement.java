package com.conference.model;

import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class Equipement {

    private int id;
    private String nom;
    private String description;
    private int quantiteTotale;
    private String statut;
    private LocalDateTime dateCreation;
    private int quantiteReservee;

    public Equipement() {
    }

    public Equipement(int id, String nom, String description,
                      int quantiteTotale, String statut) {
        this.id             = id;
        this.nom            = nom;
        this.description    = description;
        this.quantiteTotale = quantiteTotale;
        this.statut         = statut;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantiteTotale() { return quantiteTotale; }
    public void setQuantiteTotale(int quantiteTotale) {
        this.quantiteTotale = quantiteTotale;
    }

    public String getStatut() { return statut; }
    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public int getQuantiteReservee() { return quantiteReservee; }
    public void setQuantiteReservee(int quantiteReservee) {
        this.quantiteReservee = quantiteReservee;
    }

    @Override
    public String toString() {
        return nom;
    }
}