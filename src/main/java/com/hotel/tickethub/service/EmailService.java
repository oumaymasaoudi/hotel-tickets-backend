package com.hotel.tickethub.service;

import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Service pour l'envoi d'emails automatiques
 * Règle 4 & 14: Rappels paiement et rapports automatiques
 * 
 * NOTE: En développement, les emails sont loggés. 
 * En production, configurer un service d'email (SMTP, SendGrid, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    /**
     * Envoyer un rappel de paiement avant l'échéance
     * Règle 4: Rapport automatique avant échéance
     */
    public void sendPaymentReminder(Hotel hotel, Payment payment) {
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

        log.info("📧 EMAIL - Rappel paiement envoyé à: {}", hotel.getEmail());
        log.info("📧 Subject: {}", subject);
        log.info("📧 Body: {}", body);

        // TODO: En production, utiliser un service d'email réel
        // emailSender.send(hotel.getEmail(), subject, body);
    }

    /**
     * Envoyer un rapport mensuel/hebdo/quotidien
     * Règle 14: Rapports automatiques
     */
    public void sendReport(Hotel hotel, Map<String, Object> report, String reportType) {
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

        log.info("📧 EMAIL - Rapport {} envoyé à: {}", reportType, hotel.getEmail());
        log.info("📧 Subject: {}", subject);
        log.info("📧 Body: {}", body.toString());

        // TODO: En production, utiliser un service d'email réel
        // emailSender.send(hotel.getEmail(), subject, body.toString());
    }

    /**
     * Envoyer une notification de paiement en retard
     */
    public void sendOverdueNotification(Hotel hotel, Payment payment) {
        String subject = "⚠️ Paiement en retard - " + hotel.getName();
        String body = String.format(
            "Bonjour,\n\n" +
            "Votre paiement est en retard depuis le %s.\n\n" +
            "Détails:\n" +
            "- Hôtel: %s\n" +
            "- Montant dû: %.2f €\n" +
            "- Date d'échéance: %s\n\n" +
            "⚠️ Votre accès aux services est actuellement suspendu.\n" +
            "Veuillez régulariser votre paiement dès que possible pour rétablir l'accès.\n\n" +
            "Cordialement,\n" +
            "L'équipe Hotel Ticket Hub",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            hotel.getName(),
            payment.getAmount().doubleValue(),
            payment.getNextPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        log.warn("📧 EMAIL - Notification retard envoyée à: {}", hotel.getEmail());
        log.warn("📧 Subject: {}", subject);
        log.warn("📧 Body: {}", body);

        // TODO: En production, utiliser un service d'email réel
        // emailSender.send(hotel.getEmail(), subject, body);
    }
}

