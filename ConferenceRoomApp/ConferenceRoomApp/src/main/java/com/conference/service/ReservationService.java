package com.conference.service;

import com.conference.dao.NotificationDAO;
import com.conference.dao.ReservationDAO;
import com.conference.model.Notification;
import com.conference.model.Reservation;
import com.conference.util.EmailService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service métier pour la gestion des réservations.
 */
public class ReservationService {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final EmailService emailService = EmailService.getInstance();

    public enum ReservationResult {
        SUCCESS, CONFLICT, INVALID_DATES, CAPACITY_EXCEEDED, ERROR
    }

    public ReservationResult creerReservation(Reservation reservation) {
        // Validation des dates
        if (reservation.getDateDebut() == null || reservation.getDateFin() == null)
            return ReservationResult.INVALID_DATES;
        if (reservation.getDateDebut().isAfter(reservation.getDateFin()) ||
                reservation.getDateDebut().isEqual(reservation.getDateFin()))
            return ReservationResult.INVALID_DATES;
        if (reservation.getDateDebut().isBefore(LocalDateTime.now()))
            return ReservationResult.INVALID_DATES;

        // Vérification des conflits
        if (reservationDAO.isConflict(reservation.getSalleId(),
                reservation.getDateDebut(), reservation.getDateFin(), null))
            return ReservationResult.CONFLICT;

        // Sauvegarde
        reservation.setStatut("CONFIRMEE");
        if (!reservationDAO.save(reservation))
            return ReservationResult.ERROR;

        // Notification en base
        Notification notif = new Notification(
                reservation.getUtilisateurId(),
                reservation.getId(),
                "Réservation confirmée",
                "Votre réservation « " + reservation.getTitre() + " » a été confirmée.",
                "CONFIRMATION"
        );
        notificationDAO.save(notif);

        // Email async
        new Thread(() -> {
            if (reservation.getUtilisateur() != null) {
                String html = emailService.buildConfirmationEmail(
                        reservation.getUtilisateur().getPrenom(),
                        reservation.getSalle() != null ? reservation.getSalle().getNom() : "",
                        reservation.getDateDebutFormatted(),
                        reservation.getDateFinFormatted()
                );
                emailService.sendEmail(
                        reservation.getUtilisateur().getEmail(),
                        "✅ Confirmation de réservation",
                        html
                );
            }
        }).start();

        return ReservationResult.SUCCESS;
    }

    public ReservationResult modifierReservation(Reservation reservation) {
        if (reservation.getDateDebut() == null || reservation.getDateFin() == null)
            return ReservationResult.INVALID_DATES;
        if (reservation.getDateDebut().isAfter(reservation.getDateFin()))
            return ReservationResult.INVALID_DATES;

        if (reservationDAO.isConflict(reservation.getSalleId(),
                reservation.getDateDebut(), reservation.getDateFin(), reservation.getId()))
            return ReservationResult.CONFLICT;

        return reservationDAO.update(reservation)
                ? ReservationResult.SUCCESS
                : ReservationResult.ERROR;
    }

    public boolean annulerReservation(int reservationId, int userId) {
        Reservation r = reservationDAO.findById(reservationId);
        if (r == null) return false;

        boolean ok = reservationDAO.updateStatut(reservationId, "ANNULEE");
        if (ok) {
            Notification notif = new Notification(
                    userId, reservationId,
                    "Réservation annulée",
                    "La réservation « " + r.getTitre() + " » a été annulée.",
                    "ANNULATION"
            );
            notificationDAO.save(notif);
        }
        return ok;
    }

    public List<Reservation> getToutesReservations()  { return reservationDAO.findAll(); }
    public List<Reservation> getMesReservations(int uid) { return reservationDAO.findByUser(uid); }
    public List<Reservation> getProchainesReservations() { return reservationDAO.findUpcoming(); }
    public Reservation findById(int id)              { return reservationDAO.findById(id); }
    public int countTotal()                          { return reservationDAO.countTotal(); }
    public int countByStatut(String s)               { return reservationDAO.countByStatut(s); }
    public List<Object[]> getStatsByMonth(int year)  { return reservationDAO.getStatsByMonth(year); }
    public List<Object[]> getTopSalles(int limit)    { return reservationDAO.getTopSalles(limit); }
    public double getAverageDuration()               { return reservationDAO.getAverageDuration(); }
}
