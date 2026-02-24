package com.hotel.tickethub.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.hotel.tickethub.model.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    @JsonValue
    private SubscriptionPlan name;
    
    /**
     * Méthode sécurisée pour obtenir le nom du plan
     * Retourne STARTER si le plan est null ou invalide
     */
    public SubscriptionPlan getNameSafe() {
        if (name == null) {
            return SubscriptionPlan.STARTER;
        }
        try {
            // Vérifier que le nom est valide
            SubscriptionPlan.valueOf(name.name());
            return name;
        } catch (IllegalArgumentException e) {
            // Si le nom n'est pas valide, retourner STARTER par défaut
            return SubscriptionPlan.STARTER;
        }
    }

    @Column(name = "base_cost", nullable = false)
    private BigDecimal baseCost;

    @Column(name = "ticket_quota", nullable = false)
    private Integer ticketQuota;

    @Column(name = "excess_ticket_cost", nullable = false)
    private BigDecimal excessTicketCost;

    @Column(name = "max_technicians", nullable = false)
    private Integer maxTechnicians;

    @Column(name = "sla_hours", nullable = false)
    private Integer slaHours;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
