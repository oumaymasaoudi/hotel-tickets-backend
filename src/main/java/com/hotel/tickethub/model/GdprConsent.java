package com.hotel.tickethub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Consentement RGPD - Conformité avec le Règlement Général sur la Protection des Données
 * Article 6 et 7 du RGPD : Consentement explicite et traçable
 */
@Entity
@Table(name = "gdpr_consents", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_consent_type", columnList = "consent_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class GdprConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Utilisateur concerné
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Type de consentement :
     * - DATA_PROCESSING : Traitement des données personnelles
     * - MARKETING : Communications marketing
     * - ANALYTICS : Analyse et statistiques
     * - THIRD_PARTY : Partage avec des tiers
     */
    @Column(name = "consent_type", nullable = false)
    private String consentType;

    /**
     * Statut du consentement
     */
    @Column(name = "consented", nullable = false)
    private Boolean consented;

    /**
     * Date du consentement
     */
    @Column(name = "consent_date", nullable = false)
    private LocalDateTime consentDate;

    /**
     * Version de la politique de confidentialité acceptée
     */
    @Column(name = "privacy_policy_version")
    private String privacyPolicyVersion;

    /**
     * Adresse IP lors du consentement
     */
    @Column(name = "ip_address")
    private String ipAddress;

    /**
     * User-Agent lors du consentement
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

