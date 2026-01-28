package com.hotel.tickethub.scheduler;

import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.Payment;
import com.hotel.tickethub.repository.HotelRepository;
import com.hotel.tickethub.service.EmailService;
import com.hotel.tickethub.service.PaymentService;
import com.hotel.tickethub.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler pour les tâches automatiques
 * Règle 4 & 14: Rappels paiement et rapports automatiques
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportScheduler {

    private final ReportService reportService;
    private final EmailService emailService;
    private final PaymentService paymentService;
    private final HotelRepository hotelRepository;

    /**
     * Envoyer les rapports mensuels le 1er de chaque mois à 8h00
     * Règle 14: Rapports mensuels automatiques
     */
    @Scheduled(cron = "0 0 8 1 * *") // 1er du mois à 8h00
    public void sendMonthlyReports() {
        log.info("SCHEDULER - Starting monthly reports sending");

        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue() - 1; // Mois précédent

        if (month == 0) {
            month = 12;
            year--;
        }

        List<Hotel> hotels = hotelRepository.findAll();

        for (Hotel hotel : hotels) {
            try {
                var report = reportService.generateMonthlyReport(hotel.getId(), year, month);
                emailService.sendReport(hotel, report, "Mensuel");
                log.info("Monthly report sent for: {}", hotel.getName());
            } catch (Exception e) {
                log.error("Error sending monthly report for {}: {}", hotel.getName(), e.getMessage());
            }
        }

        log.info("SCHEDULER - Finished monthly reports sending");
    }

    /**
     * Envoyer les rapports hebdomadaires chaque lundi à 8h00
     * Règle 14: Rapports hebdomadaires automatiques
     */
    @Scheduled(cron = "0 0 8 * * MON") // Chaque lundi à 8h00
    public void sendWeeklyReports() {
        log.info("SCHEDULER - Starting weekly reports sending");

        LocalDateTime weekStart = LocalDateTime.now().minusWeeks(1).withHour(0).withMinute(0).withSecond(0);

        List<Hotel> hotels = hotelRepository.findAll();

        for (Hotel hotel : hotels) {
            try {
                var report = reportService.generateWeeklyReport(hotel.getId(), weekStart);
                emailService.sendReport(hotel, report, "Hebdomadaire");
                log.info("Weekly report sent for: {}", hotel.getName());
            } catch (Exception e) {
                log.error("Error sending weekly report for {}: {}", hotel.getName(),
                        e.getMessage());
            }
        }

        log.info("SCHEDULER - Finished weekly reports sending");
    }

    /**
     * Vérifier et envoyer les rappels de paiement chaque jour à 9h00
     * Règle 4: Rappels automatiques avant échéance
     */
    @Scheduled(cron = "0 0 9 * * *") // Chaque jour à 9h00
    public void sendPaymentReminders() {
        log.info("💰 SCHEDULER - Début vérification rappels paiement");

        List<Hotel> hotels = hotelRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in7Days = now.plusDays(7);

        for (Hotel hotel : hotels) {
            try {
                if (hotel.getNextPaymentDate() != null) {
                    LocalDateTime nextPayment = hotel.getNextPaymentDate();

                    // Envoyer un rappel 7 jours avant l'échéance
                    if (nextPayment.isAfter(now) && nextPayment.isBefore(in7Days)) {
                        paymentService.getLastPayment(hotel.getId()).ifPresent(payment -> {
                            emailService.sendPaymentReminder(hotel, payment);
                            log.info("Payment reminder sent for: {} (due date: {})",
                                    hotel.getName(), nextPayment);
                        });
                    }
                }
            } catch (Exception e) {
                log.error("Error sending payment reminder for {}: {}",
                        hotel.getName(), e.getMessage());
            }
        }

        log.info("💰 SCHEDULER - Fin vérification rappels paiement");
    }

    /**
     * Vérifier et marquer les paiements en retard chaque jour à 10h00
     * Règle 4: Vérification automatique des paiements
     */
    @Scheduled(cron = "0 0 10 * * *") // Chaque jour à 10h00
    public void checkOverduePayments() {
        log.info("SCHEDULER - Starting overdue payments check");

        paymentService.checkAndMarkOverduePayments();

        // Envoyer des notifications pour les paiements en retard
        List<Payment> overduePayments = paymentService.getOverduePayments();

        for (Payment payment : overduePayments) {
            try {
                Hotel hotel = payment.getHotel();
                emailService.sendOverdueNotification(hotel, payment);
                log.info("Overdue notification sent for: {}", hotel.getName());
            } catch (Exception e) {
                log.error("Error sending overdue notification: {}", e.getMessage());
            }
        }

        log.info("SCHEDULER - Finished overdue payments check ({} found)", overduePayments.size());
    }
}
