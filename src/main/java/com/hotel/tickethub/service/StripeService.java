package com.hotel.tickethub.service;

import com.hotel.tickethub.model.Plan;
import com.hotel.tickethub.repository.PlanRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StripeService {

    private final PlanRepository planRepository;

    @Value("${stripe.secret.key:sk_test_51QEXAMPLE}")
    private String stripeSecretKey;

    @Value("${stripe.public.key:pk_test_51QEXAMPLE}")
    private String stripePublicKey;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Créer une session Stripe Checkout pour un abonnement
     */
    public Map<String, String> createCheckoutSession(UUID hotelId, UUID planId) throws StripeException {
        // Vérifier que la clé Stripe est configurée
        if (stripeSecretKey == null || stripeSecretKey.isEmpty() || 
            stripeSecretKey.equals("sk_test_your_secret_key_here") || 
            stripeSecretKey.equals("sk_test_51QEXAMPLE")) {
            throw new RuntimeException("Clé API Stripe non configurée. Veuillez configurer stripe.secret.key dans application.properties");
        }

        // Initialiser Stripe avec la clé secrète (thread-safe)
        initializeStripe();

        // Récupérer le plan
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan non trouvé avec l'ID: " + planId + ". Vérifiez que le plan existe dans la base de données."));

        // Créer la session Stripe Checkout
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(frontendUrl + "/dashboard/admin/payment?success=true&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/dashboard/admin/payment?canceled=true")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Abonnement " + plan.getName())
                                                                .setDescription("Plan " + plan.getName() + " - " + plan.getTicketQuota() + " tickets/mois")
                                                                .build()
                                                )
                                                .setUnitAmount(plan.getBaseCost().multiply(new BigDecimal("100")).longValue()) // Convertir en centimes
                                                .setRecurring(
                                                        SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                                                .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .putMetadata("hotelId", hotelId.toString())
                .putMetadata("planId", planId.toString())
                .build();

        Session session = Session.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("url", session.getUrl());
        response.put("publicKey", stripePublicKey);

        return response;
    }

    /**
     * Récupérer les détails d'une session Stripe
     */
    public Session getSession(String sessionId) throws StripeException {
        initializeStripe();
        return Session.retrieve(sessionId);
    }

    /**
     * Initialiser Stripe de manière thread-safe
     */
    private void initializeStripe() {
        synchronized (StripeService.class) {
            if (!stripeSecretKey.equals(Stripe.apiKey)) {
                Stripe.apiKey = stripeSecretKey;
            }
        }
    }
}

