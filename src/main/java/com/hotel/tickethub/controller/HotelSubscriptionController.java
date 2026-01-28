package com.hotel.tickethub.controller;

import com.hotel.tickethub.service.HotelSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:5173",
        "http://192.168.58.1:5173"
})
public class HotelSubscriptionController {

    private final HotelSubscriptionService subscriptionService;

    /**
     * GET /api/subscriptions/hotel/{hotelId}
     * Récupérer l'abonnement actuel d'un hôtel
     */
    @GetMapping("/hotel/{hotelId}")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')") // Temporairement désactivé pour debug
    public ResponseEntity<Map<String, Object>> getHotelSubscription(@PathVariable UUID hotelId) {
        return subscriptionService.getActiveSubscription(hotelId)
                .map(subscription -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", subscription.getId());
                    response.put("hotelId", subscription.getHotel().getId());
                    response.put("planId", subscription.getPlan().getId());
                    response.put("planName", subscription.getPlan().getName());
                    response.put("planBaseCost", subscription.getPlan().getBaseCost());
                    response.put("status", subscription.getStatus());
                    response.put("startDate", subscription.getStartDate());
                    response.put("endDate", subscription.getEndDate());
                    response.put("isPendingChange", subscription.getIsPendingChange());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.ok(Map.of("exists", false)));
    }
}

