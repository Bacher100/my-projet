package com.conference.service;

import com.conference.dao.NotificationDAO;
import com.conference.dao.ReservationDAO;
import com.conference.model.Notification;
import com.conference.model.Reservation;
import com.conference.util.DatabaseConnection;
import com.conference.util.EmailService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service de rappels automatiques planifiés (toutes les 5 minutes).
 */
public class ReminderService {

    private static ReminderService instance;
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final EmailService emailService = EmailService.getInstance();

    private ReminderService() {}

    public static synchronized ReminderService getInstance() {
        if (instance == null) instance = new ReminderService();
        return instance;
    }

    public void start() {
        int reminderMin = Integer.parseInt(
                DatabaseConnection.getInstance().getProps()
                        .getProperty("app.notification.reminder.minutes", "30")
        );

        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Reservation> upcoming =
                        reservationDAO.findForReminder(reminderMin);
                for (Reservation r : upcoming) {
                    if (r.getUtilisateur() == null) continue;

                    // Notif en base
                    Notification notif = new Notification(
                            r.getUtilisateurId(), r.getId(),
                            "Rappel : réservation dans " + reminderMin + " min",
                            "Votre réservation « " + r.getTitre() + " » commence bientôt.",
                            "RAPPEL"
                    );
                    notificationDAO.save(notif);

                    // Email
                    String html = emailService.buildReminderEmail(
                            r.getUtilisateur().getPrenom(),
                            r.getSalle() != null ? r.getSalle().getNom() : "",
                            r.getDateDebutFormatted(),
                            reminderMin
                    );
                    emailService.sendEmail(
                            r.getUtilisateur().getEmail(),
                            "⏰ Rappel de réservation",
                            html
                    );
                }

                // Marquer comme TERMINEE les réservations passées
                reservationDAO.findUpcoming().stream()
                        .filter(r -> r.getDateFin().isBefore(
                                java.time.LocalDateTime.now()))
                        .forEach(r -> reservationDAO.updateStatut(r.getId(), "TERMINEE"));

            } catch (Exception e) {
                System.err.println("Erreur ReminderService: " + e.getMessage());
            }
        }, 1, 5, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdown();
    }
}
