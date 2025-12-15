package com.hotel.tickethub.controller;

import com.hotel.tickethub.model.Plan;
import com.hotel.tickethub.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller pour les plans d'abonnement
 */
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:5173",
        "http://192.168.58.1:5173"
})
public class PlanController {

    private final PlanRepository planRepository;

    /**
     * GET /api/plans - Récupérer tous les plans d'abonnement
     */
    @GetMapping
    public ResponseEntity<List<Plan>> getAllPlans() {
        return ResponseEntity.ok(planRepository.findAll());
    }

    /**
     * GET /api/plans/statistics - Récupérer les statistiques des plans
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPlanStatistics() {
        List<Plan> plans = planRepository.findAll();

        if (plans.isEmpty()) {
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("total", 0);
            emptyStats.put("avgPrice", 0.0);
            emptyStats.put("avgQuota", 0);
            emptyStats.put("avgSla", 0);
            return ResponseEntity.ok(emptyStats);
        }

        // Calculer les statistiques
        int total = plans.size();

        BigDecimal totalPrice = plans.stream()
                .map(Plan::getBaseCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgPrice = totalPrice.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        int totalQuota = plans.stream()
                .mapToInt(Plan::getTicketQuota)
                .sum();
        int avgQuota = totalQuota / total;

        int totalSla = plans.stream()
                .mapToInt(Plan::getSlaHours)
                .sum();
        int avgSla = totalSla / total;

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", total);
        statistics.put("avgPrice", avgPrice.doubleValue());
        statistics.put("avgQuota", avgQuota);
        statistics.put("avgSla", avgSla);

        return ResponseEntity.ok(statistics);
    }
}
