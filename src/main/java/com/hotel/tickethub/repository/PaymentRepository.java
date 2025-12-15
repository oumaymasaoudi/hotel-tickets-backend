package com.hotel.tickethub.repository;

import com.hotel.tickethub.model.Payment;
import com.hotel.tickethub.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Récupérer tous les paiements d'un hôtel
     */
    List<Payment> findByHotelIdOrderByPaymentDateDesc(UUID hotelId);

    /**
     * Récupérer le dernier paiement d'un hôtel
     */
    Optional<Payment> findFirstByHotelIdOrderByPaymentDateDesc(UUID hotelId);

    /**
     * Récupérer les paiements en retard
     */
    @Query("SELECT p FROM Payment p WHERE p.status = com.hotel.tickethub.model.enums.PaymentStatus.OVERDUE AND p.nextPaymentDate < CURRENT_TIMESTAMP")
    List<Payment> findOverduePayments();

    /**
     * Récupérer les paiements en attente
     */
    @Query("SELECT p FROM Payment p WHERE p.status = ?1 AND p.hotel.id = ?2")
    List<Payment> findByStatusAndHotelId(PaymentStatus status, UUID hotelId);

    /**
     * Vérifier si le paiement d'un hôtel est à jour
     */
    @Query("SELECT CASE WHEN p IS NULL OR p.nextPaymentDate >= CURRENT_TIMESTAMP THEN true ELSE false END " +
           "FROM Payment p WHERE p.hotel.id = ?1 ORDER BY p.paymentDate DESC LIMIT 1")
    Boolean isPaymentUpToDate(UUID hotelId);

    /**
     * Récupérer les paiements d'une période
     */
    @Query("SELECT p FROM Payment p WHERE p.hotel.id = :hotelId " +
           "AND p.paymentDate >= :startDate AND p.paymentDate <= :endDate")
    List<Payment> findPaymentsByPeriod(
        @Param("hotelId") UUID hotelId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
