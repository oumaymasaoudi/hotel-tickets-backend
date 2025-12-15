package com.hotel.tickethub.service;

import com.hotel.tickethub.model.Payment;
import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.enums.PaymentStatus;
import com.hotel.tickethub.repository.PaymentRepository;
import com.hotel.tickethub.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final HotelRepository hotelRepository;

    /**
     * Enregistrer un nouveau paiement
     * Règle 4: Gestion des paiements
     */
    public Payment createPayment(UUID hotelId, Payment payment) {
        Hotel hotel = hotelRepository.findById(hotelId)
            .orElseThrow(() -> new RuntimeException("Hôtel non trouvé"));

        payment.setHotel(hotel);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.PAID);

        Payment saved = paymentRepository.save(payment);

        // Mettre à jour les dates dans Hotel
        hotel.setLastPaymentDate(saved.getPaymentDate());
        hotel.setNextPaymentDate(saved.getNextPaymentDate());
        hotelRepository.save(hotel);

        return saved;
    }

    /**
     * Récupérer l'historique des paiements d'un hôtel
     * Règle 11: Historique des paiements
     */
    public List<Payment> getPaymentHistory(UUID hotelId) {
        return paymentRepository.findByHotelIdOrderByPaymentDateDesc(hotelId);
    }

    /**
     * Récupérer le dernier paiement d'un hôtel
     */
    public Optional<Payment> getLastPayment(UUID hotelId) {
        return paymentRepository.findFirstByHotelIdOrderByPaymentDateDesc(hotelId);
    }

    /**
     * Vérifier si le paiement d'un hôtel est à jour
     * Règle 4: Vérification automatique avant accès
     */
    public Boolean isPaymentUpToDate(UUID hotelId) {
        Optional<Payment> lastPayment = getLastPayment(hotelId);
        if (lastPayment.isEmpty()) {
            return false; // Aucun paiement = en retard
        }

        Payment payment = lastPayment.get();
        return payment.getNextPaymentDate().isAfter(LocalDateTime.now())
               && payment.getStatus() == PaymentStatus.PAID;
    }

    /**
     * Récupérer les paiements en retard
     * Pour envoi d'avertissement
     */
    public List<Payment> getOverduePayments() {
        return paymentRepository.findOverduePayments();
    }

    /**
     * Récupérer tous les paiements (pour SuperAdmin)
     * Triés par date de paiement décroissante
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll().stream()
            .sorted((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate()))
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Récupérer les paiements d'une période donnée
     * Règle 14: Rapports automatiques
     */
    public List<Payment> getPaymentsByPeriod(UUID hotelId, LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findPaymentsByPeriod(hotelId, startDate, endDate);
    }

    /**
     * Obtenir le montant total payé dans une période
     */
    public double getTotalPaymentAmount(UUID hotelId, LocalDateTime startDate, LocalDateTime endDate) {
        return getPaymentsByPeriod(hotelId, startDate, endDate).stream()
            .mapToDouble(p -> p.getAmount().doubleValue())
            .sum();
    }

    /**
     * Mettre à jour le statut d'un paiement
     */
    public Payment updatePaymentStatus(UUID paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));

        payment.setStatus(status);
        return paymentRepository.save(payment);
    }

    /**
     * Marquer un paiement comme en retard si dépassé
     * À appeler par scheduler chaque jour
     */
    public void checkAndMarkOverduePayments() {
        paymentRepository.findOverduePayments().forEach(payment -> {
            payment.setStatus(PaymentStatus.OVERDUE);
            payment.getHotel().setIsActive(false); // Bloquer l'accès
            paymentRepository.save(payment);
        });
    }
}
