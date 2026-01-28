package com.hotel.tickethub.service;

import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * Service pour l'envoi d'emails automatiques
 * Règle 4 & 14: Rappels paiement et rapports automatiques
 * 
 * Utilise Spring Mail pour l'envoi d'emails.
 * En développement, si la configuration email n'est pas disponible, les emails sont loggés.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final Optional<JavaMailSender> mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Envoyer un rappel de paiement avant l'échéance
     * Règle 4: Rapport automatique avant échéance
     */
    public void sendPaymentReminder(Hotel hotel, Payment payment) {
        if (hotel.getEmail() == null || hotel.getEmail().isEmpty()) {
            log.warn("No email configured for hotel: {}", hotel.getName());
            return;
        }

        String subject = "Rappel de paiement - " + hotel.getName();
        String body = String.format(
            "Bonjour,\n\n" +
            "Ceci est un rappel que votre prochain paiement est prévu le %s.\n\n" +
            "Détails:\n" +
            "- Hôtel: %s\n" +
            "- Montant: %.2f €\n" +
            "- Date d'échéance: %s\n\n" +
            "Veuillez régulariser votre paiement pour continuer à bénéficier de nos services.\n\n" +
            "Cordialement,\n" +
            "L'équipe Hotel Ticket Hub",
            payment.getNextPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            hotel.getName(),
            payment.getAmount().doubleValue(),
            payment.getNextPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        sendEmail(hotel.getEmail(), subject, body);
    }

    /**
     * Envoyer un rapport mensuel/hebdo/quotidien
     * Règle 14: Rapports automatiques
     */
    public void sendReport(Hotel hotel, Map<String, Object> report, String reportType) {
        if (hotel.getEmail() == null || hotel.getEmail().isEmpty()) {
            log.warn("No email configured for hotel: {}", hotel.getName());
            return;
        }

        String subject = String.format("Rapport %s - %s", reportType, hotel.getName());
        
        StringBuilder body = new StringBuilder();
        body.append("Bonjour,\n\n");
        body.append(String.format("Voici votre rapport %s pour la période du %s au %s.\n\n", 
            reportType,
            ((Map<?, ?>) report.get("period")).get("start"),
            ((Map<?, ?>) report.get("period")).get("end")));
        
        body.append("Statistiques des tickets:\n");
        body.append(String.format("- Total: %d\n", report.get("totalTickets")));
        body.append(String.format("- Ouverts: %d\n", report.get("openTickets")));
        body.append(String.format("- En cours: %d\n", report.get("inProgressTickets")));
        body.append(String.format("- Résolus: %d\n", report.get("resolvedTickets")));
        body.append(String.format("- Urgents: %d\n", report.get("urgentTickets")));
        
        if (report.containsKey("averageResolutionTimeHours")) {
            body.append(String.format("- Temps moyen de résolution: %.2f heures\n", 
                report.get("averageResolutionTimeHours")));
        }

        body.append("\nCordialement,\n");
        body.append("L'équipe Hotel Ticket Hub");

        sendEmail(hotel.getEmail(), subject, body.toString());
    }

    /**
     * Envoyer une notification de paiement en retard
     */
    public void sendOverdueNotification(Hotel hotel, Payment payment) {
        if (hotel.getEmail() == null || hotel.getEmail().isEmpty()) {
            log.warn("No email configured for hotel: {}", hotel.getName());
            return;
        }

        String subject = "Paiement en retard - " + hotel.getName();
        String body = String.format(
            "Bonjour,\n\n" +
            "Votre paiement est en retard depuis le %s.\n\n" +
            "Détails:\n" +
            "- Hôtel: %s\n" +
            "- Montant dû: %.2f €\n" +
            "- Date d'échéance: %s\n\n" +
            "Votre accès aux services est actuellement suspendu.\n" +
            "Veuillez régulariser votre paiement dès que possible pour rétablir l'accès.\n\n" +
            "Cordialement,\n" +
            "L'équipe Hotel Ticket Hub",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            hotel.getName(),
            payment.getAmount().doubleValue(),
            payment.getNextPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        sendEmail(hotel.getEmail(), subject, body);
    }

    /**
     * Méthode privée pour envoyer un email
     * Utilise JavaMailSender si disponible, sinon log l'email
     */
    private void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("EMAIL (disabled) - To: {}, Subject: {}", to, subject);
            return;
        }

        if (mailSender.isPresent() && fromEmail != null && !fromEmail.isEmpty()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                
                mailSender.get().send(message);
                log.info("EMAIL sent successfully to: {}", to);
            } catch (Exception e) {
                log.error("Error sending email to {}: {}", to, e.getMessage(), e);
                // Fallback: log l'email même en cas d'erreur
                log.info("EMAIL (fallback) - To: {}, Subject: {}", to, subject);
                log.info("Body: {}", body);
            }
        } else {
            // Fallback: log l'email si la configuration n'est pas disponible
            log.info("EMAIL (development mode) - To: {}, Subject: {}", to, subject);
            log.info("Body: {}", body);
            log.info("To enable email sending, configure spring.mail.* in application.properties");
        }
    }
}

