package com.hotel.tickethub.model;

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
    private SubscriptionPlan name;

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
