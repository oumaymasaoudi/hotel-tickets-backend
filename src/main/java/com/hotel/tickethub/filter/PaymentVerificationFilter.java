package com.hotel.tickethub.filter;

import com.hotel.tickethub.service.PaymentService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filtre pour vérifier que les paiements des hôtels sont à jour avant
 * d'autoriser l'accès
 * Règle 4: Vérification automatique avant accès
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class PaymentVerificationFilter extends OncePerRequestFilter {

    private final PaymentService paymentService;

    // Endpoints publics qui ne nécessitent pas de vérification de paiement
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/categories/public",
            "/api/tickets/public",
            "/api/hotels/public" // Endpoint public pour la liste des hôtels
    };

    // Endpoints GET uniquement qui permettent la consultation même si paiement en
    // retard
    // (pour que les admins puissent voir les infos de leur hôtel)
    private static final String[] READ_ONLY_ENDPOINTS = {
            "/api/hotels/" // GET /api/hotels/{id} - consultation uniquement
    };

    // Endpoints de paiement qui doivent être accessibles même si paiement en retard
    // (pour permettre à l'utilisateur de régulariser sa situation)
    private static final String[] PAYMENT_ENDPOINTS = {
            "/api/stripe/create-checkout-session", // Création de session Stripe
            "/api/stripe/session/", // Consultation de session Stripe
            "/api/subscriptions/hotel/", // Consultation d'abonnement
            "/api/plans", // Consultation des plans
            "/api/payments/hotel/" // Consultation des paiements
    };

    // Pattern pour extraire hotelId depuis l'URL
    private static final Pattern HOTEL_ID_PATTERN = Pattern.compile("/api/hotels/([a-f0-9-]+)");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Ignorer les endpoints publics
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Permettre les requêtes GET de consultation même si paiement en retard
        // (pour que les admins puissent voir les infos de leur hôtel)
        if (isReadOnlyEndpoint(path) && "GET".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Permettre l'accès aux endpoints de paiement même si paiement en retard
        // (pour permettre à l'utilisateur de régulariser sa situation)
        if (isPaymentEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraire hotelId depuis l'URL ou les paramètres
        UUID hotelId = extractHotelId(path, request);

        if (hotelId != null) {
            // Vérifier si le paiement est à jour
            Boolean isUpToDate = paymentService.isPaymentUpToDate(hotelId);

            if (Boolean.FALSE.equals(isUpToDate)) {
                response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\": \"Paiement en retard\", " +
                                "\"message\": \"L'accès aux services est suspendu. Veuillez régulariser votre paiement.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        for (String endpoint : PUBLIC_ENDPOINTS) {
            if (path.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }

    private boolean isReadOnlyEndpoint(String path) {
        for (String endpoint : READ_ONLY_ENDPOINTS) {
            if (path.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPaymentEndpoint(String path) {
        for (String endpoint : PAYMENT_ENDPOINTS) {
            if (path.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }

    private UUID extractHotelId(String path, HttpServletRequest request) {
        // Essayer d'extraire depuis l'URL
        Matcher matcher = HOTEL_ID_PATTERN.matcher(path);
        if (matcher.find()) {
            try {
                return UUID.fromString(matcher.group(1));
            } catch (IllegalArgumentException e) {
                // Ignorer
            }
        }

        // Essayer depuis les paramètres de requête
        String hotelIdParam = request.getParameter("hotelId");
        if (hotelIdParam != null) {
            try {
                return UUID.fromString(hotelIdParam);
            } catch (IllegalArgumentException e) {
                // Ignorer
            }
        }

        // Essayer depuis le header (si fourni par le frontend)
        String hotelIdHeader = request.getHeader("X-Hotel-Id");
        if (hotelIdHeader != null) {
            try {
                return UUID.fromString(hotelIdHeader);
            } catch (IllegalArgumentException e) {
                // Ignorer
            }
        }

        return null;
    }
}
