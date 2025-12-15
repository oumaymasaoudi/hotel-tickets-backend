package com.hotel.tickethub.model;

import com.hotel.tickethub.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Enregistre les paiements des hôtels
 * Règle 4 & 10: Gestion des paiements
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_hotel_id", columnList = "hotel_id"),
    @Index(name = "idx_payment_date", columnList = "payment_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    /**
     * Montant payé pour cette période
     */
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    /**
     * Date de paiement
     */
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    /**
     * Prochain paiement attendu
     */
    @Column(name = "next_payment_date", nullable = false)
    private LocalDateTime nextPaymentDate;

    /**
     * Statut du paiement: PAID, PENDING, OVERDUE
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PAID;

    /**
     * Méthode de paiement: CARD, TRANSFER, CHECK, BANK_WIRE
     */
    @Column(name = "payment_method")
    private String paymentMethod;

    /**
     * Référence de paiement (numéro facture, numéro transaction)
     */
    @Column(name = "payment_reference")
    private String paymentReference;

    /**
     * Notes optionnelles
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Audit
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
