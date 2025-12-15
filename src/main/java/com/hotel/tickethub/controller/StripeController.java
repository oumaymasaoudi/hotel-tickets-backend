package com.hotel.tickethub.controller;

import com.hotel.tickethub.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:5173",
        "http://192.168.58.1:5173"
})
public class StripeController {

    private final StripeService stripeService;

    /**
     * POST /api/stripe/create-checkout-session
     * Créer une session Stripe Checkout pour un abonnement
     */
    @PostMapping("/create-checkout-session")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @RequestParam UUID hotelId,
            @RequestParam UUID planId) {
        try {
            Map<String, String> session = stripeService.createCheckoutSession(hotelId, planId);
            return ResponseEntity.ok(session);
        } catch (com.stripe.exception.StripeException e) {
            // Erreur spécifique Stripe
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur Stripe: " + e.getMessage());
            error.put("type", e.getClass().getSimpleName());
            return ResponseEntity.status(400).body(error);
        } catch (RuntimeException e) {
            // Erreur de validation ou autre
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(400).body(error);
        } catch (Exception e) {
            // Erreur inattendue
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la création de la session Stripe: " + e.getMessage());
            e.printStackTrace(); // Logger l'erreur complète
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * GET /api/stripe/session/{sessionId}
     * Récupérer les détails d'une session Stripe
     */
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable String sessionId) {
        try {
            var session = stripeService.getSession(sessionId);
            Map<String, Object> response = new HashMap<>();
            response.put("id", session.getId());
            response.put("status", session.getStatus());
            response.put("paymentStatus", session.getPaymentStatus());
            response.put("customerEmail", session.getCustomerEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}

