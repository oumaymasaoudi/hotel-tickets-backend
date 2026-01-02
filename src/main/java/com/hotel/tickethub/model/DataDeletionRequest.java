package com.hotel.tickethub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Demande de suppression de données (Droit à l'oubli - RGPD Article 17)
 */
@Entity
@Table(name = "data_deletion_requests", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DataDeletionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Utilisateur demandant la suppression
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Statut de la demande :
     * - PENDING : En attente
     * - PROCESSING : En cours de traitement
     * - COMPLETED : Complétée
     * - REJECTED : Rejetée (avec raison)
     */
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeletionStatus status;

    /**
     * Raison du rejet (si applicable)
     */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /**
     * Date de traitement
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * Utilisateur qui a traité la demande
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    /**
     * Adresse IP de la demande
     */
    @Column(name = "ip_address")
    private String ipAddress;

    /**
     * Confirmation de suppression envoyée
     */
    @Column(name = "confirmation_sent")
    @Builder.Default
    private Boolean confirmationSent = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum DeletionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        REJECTED
    }
}

