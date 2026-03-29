package com.conference.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/**
 * Service d'envoi d'emails pour les confirmations et rappels de réservation.
 */
public class EmailService {

    private static EmailService instance;
    private Properties mailProps;
    private String username;
    private String password;
    private String fromEmail;

    private EmailService() {
        Properties appProps = DatabaseConnection.getInstance().getProps();
        username  = appProps.getProperty("mail.username");
        password  = appProps.getProperty("mail.password");
        fromEmail = appProps.getProperty("mail.from");

        mailProps = new Properties();
        mailProps.put("mail.smtp.auth",            "true");
        mailProps.put("mail.smtp.starttls.enable", "true");
        mailProps.put("mail.smtp.host",            appProps.getProperty("mail.host"));
        mailProps.put("mail.smtp.port",            appProps.getProperty("mail.port"));
        mailProps.put("mail.smtp.ssl.trust",       appProps.getProperty("mail.host"));
    }

    public static synchronized EmailService getInstance() {
        if (instance == null) instance = new EmailService();
        return instance;
    }

    public boolean sendEmail(String to, String subject, String htmlBody) {
        try {
            Session session = Session.getInstance(mailProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(htmlBody, "text/html; charset=UTF-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);
            message.setContent(multipart);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Erreur envoi email: " + e.getMessage());
            return false;
        }
    }

    public String buildConfirmationEmail(String prenom, String salleNom,
                                         String dateDebut, String dateFin) {
        return """
            <html><body style="font-family:Arial,sans-serif;max-width:600px;margin:auto;">
              <div style="background:#2196F3;color:white;padding:20px;border-radius:8px 8px 0 0;">
                <h2>✅ Confirmation de Réservation</h2>
              </div>
              <div style="padding:20px;border:1px solid #ddd;border-radius:0 0 8px 8px;">
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Votre réservation a été confirmée avec succès :</p>
                <table style="width:100%%;border-collapse:collapse;">
                  <tr style="background:#f5f5f5;">
                    <td style="padding:10px;border:1px solid #ddd;"><strong>Salle</strong></td>
                    <td style="padding:10px;border:1px solid #ddd;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding:10px;border:1px solid #ddd;"><strong>Début</strong></td>
                    <td style="padding:10px;border:1px solid #ddd;">%s</td>
                  </tr>
                  <tr style="background:#f5f5f5;">
                    <td style="padding:10px;border:1px solid #ddd;"><strong>Fin</strong></td>
                    <td style="padding:10px;border:1px solid #ddd;">%s</td>
                  </tr>
                </table>
                <p style="color:#666;font-size:12px;">Système de Gestion des Salles de Conférence</p>
              </div>
            </body></html>
            """.formatted(prenom, salleNom, dateDebut, dateFin);
    }

    public String buildReminderEmail(String prenom, String salleNom,
                                     String dateDebut, int minutesAvant) {
        return """
            <html><body style="font-family:Arial,sans-serif;max-width:600px;margin:auto;">
              <div style="background:#FF9800;color:white;padding:20px;border-radius:8px 8px 0 0;">
                <h2>⏰ Rappel de Réservation</h2>
              </div>
              <div style="padding:20px;border:1px solid #ddd;border-radius:0 0 8px 8px;">
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Rappel : votre réservation de la salle <strong>%s</strong>
                   commence dans <strong>%d minutes</strong>.</p>
                <p>Heure de début : <strong>%s</strong></p>
                <p style="color:#666;font-size:12px;">Système de Gestion des Salles de Conférence</p>
              </div>
            </body></html>
            """.formatted(prenom, salleNom, minutesAvant, dateDebut);
    }
}