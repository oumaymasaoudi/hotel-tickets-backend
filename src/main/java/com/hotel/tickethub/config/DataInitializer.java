package com.hotel.tickethub.config;

import com.hotel.tickethub.model.Plan;
import com.hotel.tickethub.model.enums.SubscriptionPlan;
import com.hotel.tickethub.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Initialise les données par défaut dans la base de données
 * S'assure que les plans d'abonnement existent
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private final PlanRepository planRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing default data...");
        initializePlans();
        log.info("Data initialization completed.");
    }

    private void initializePlans() {
        List<Plan> defaultPlans = Arrays.asList(
                createPlanIfNotExists(SubscriptionPlan.STARTER, new BigDecimal("49.99"), 50, new BigDecimal("2.50"), 2, 24),
                createPlanIfNotExists(SubscriptionPlan.PRO, new BigDecimal("99.99"), 150, new BigDecimal("2.00"), 5, 12),
                createPlanIfNotExists(SubscriptionPlan.ENTERPRISE, new BigDecimal("199.99"), 500, new BigDecimal("1.50"), 15, 6)
        );

        // Corriger les plans avec des noms invalides (BASIC -> STARTER)
        planRepository.findAll().forEach(plan -> {
            if (plan.getName() == null) {
                log.warn("Found plan with null name: {}", plan.getId());
                plan.setName(SubscriptionPlan.STARTER);
                planRepository.save(plan);
            } else {
                try {
                    // Vérifier que le nom est valide
                    SubscriptionPlan.valueOf(plan.getName().name());
                } catch (IllegalArgumentException e) {
                    log.warn("Found plan with invalid name '{}', converting to STARTER", plan.getName());
                    plan.setName(SubscriptionPlan.STARTER);
                    planRepository.save(plan);
                }
            }
        });

        log.info("Plans initialized: {}", defaultPlans.size());
    }

    private Plan createPlanIfNotExists(SubscriptionPlan name, BigDecimal baseCost, int ticketQuota,
                                       BigDecimal excessTicketCost, int maxTechnicians, int slaHours) {
        return planRepository.findByName(name).orElseGet(() -> {
            Plan plan = new Plan();
            plan.setName(name);
            plan.setBaseCost(baseCost);
            plan.setTicketQuota(ticketQuota);
            plan.setExcessTicketCost(excessTicketCost);
            plan.setMaxTechnicians(maxTechnicians);
            plan.setSlaHours(slaHours);
            Plan saved = planRepository.save(plan);
            log.info("Created default plan: {}", name);
            return saved;
        });
    }
}
