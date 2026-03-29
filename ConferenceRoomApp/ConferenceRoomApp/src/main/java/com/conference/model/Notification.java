package com.conference.model;

import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class Notification {

    private int id;
    private int utilisateurId;
    private Integer reservationId;
    private String titre;
    private String message;
    private String type;
    private boolean lue;
    private LocalDateTime dateCreation;

    public Notification() {
    }

    public Notification(int utilisateurId,
                        Integer reservationId,
                        String titre,
                        String message,
                        String type) {
        this.utilisateurId = utilisateurId;
        this.reservationId = reservationId;
        this.titre         = titre;
        this.message       = message;
        this.type          = type;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getMessage() { return message; }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isLue() { return lue; }
    public void setLue(boolean lue) { this.lue = lue; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}