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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Abonnement d'un hôtel à un plan
 * Avec possibilité d'ajouter des catégories supplémentaires
 * Règle 3 & 9: Abonnements et plans
 */
@Entity
@Table(name = "hotel_subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class HotelSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    /**
     * Catégories supplémentaires souscrites
     * (au-delà des catégories incluses dans le plan)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "hotel_subscription_additional_categories",
        joinColumns = @JoinColumn(name = "subscription_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> additionalCategories = new HashSet<>();

    /**
     * Date de début de l'abonnement
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * Date de fin de l'abonnement
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * Statut: ACTIVE, INACTIVE, PENDING, CANCELLED
     */
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, PENDING, CANCELLED

    /**
     * Si true, ce plan prend effet au prochain cycle de facturation
     */
    @Column(name = "is_pending_change")
    private Boolean isPendingChange = false;

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
