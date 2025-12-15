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
 * Journal d'audit pour tracer les actions critiques
 * Règle 12: Journal des activités
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_entity_type_id", columnList = "entity_type,entity_id"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Utilisateur qui a effectué l'action
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Type d'action: CREATE_TICKET, UPDATE_TICKET, ASSIGN_TICKET,
     *                ESCALATE_TICKET, ADD_COMMENT, PAYMENT_RECEIVED,
     *                PLAN_CHANGED, USER_CREATED, etc.
     */
    @Column(name = "action", nullable = false)
    private String action;

    /**
     * Type d'entité affectée: Ticket, Payment, Hotel, User, Plan, etc.
     */
    @Column(name = "entity_type", nullable = false)
    private String entityType;

    /**
     * ID de l'entité affectée
     */
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    /**
     * Hotelid pour filtrage (pour audit hotel-spécifique)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    /**
     * Détails de la modification (JSON: {before: {}, after: {}})
     * Exemple: {"before": {"status": "OPEN"}, "after": {"status": "IN_PROGRESS"}}
     */
    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes;

    /**
     * Adresse IP de l'utilisateur
     */
    @Column(name = "ip_address")
    private String ipAddress;

    /**
     * Description textuelle de l'action
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Timestamp de l'action
     */
    @CreatedDate
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
