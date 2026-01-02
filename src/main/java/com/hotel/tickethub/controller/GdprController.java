package com.hotel.tickethub.controller;

import com.hotel.tickethub.dto.GdprConsentRequest;
import com.hotel.tickethub.dto.GdprDataExportResponse;
import com.hotel.tickethub.dto.GdprDeletionRequestResponse;
import com.hotel.tickethub.model.DataDeletionRequest;
import com.hotel.tickethub.model.GdprConsent;
import com.hotel.tickethub.model.User;
import com.hotel.tickethub.repository.UserRepository;
import com.hotel.tickethub.service.GdprService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller RGPD - Conformité avec le Règlement Général sur la Protection des
 * Données
 * 
 * Endpoints :
 * - POST /api/gdpr/consent : Enregistrer un consentement
 * - GET /api/gdpr/consent : Récupérer les consentements de l'utilisateur
 * - GET /api/gdpr/export : Exporter les données personnelles
 * - POST /api/gdpr/deletion-request : Demander la suppression des données
 * - GET /api/gdpr/deletion-requests : Liste des demandes (Admin)
 * - POST /api/gdpr/deletion-requests/{id}/process : Traiter une demande (Admin)
 */
@RestController
@RequestMapping("/api/gdpr")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:5173",
        "http://192.168.58.1:5173",
        "http://51.21.196.104"
})
public class GdprController {

    private final GdprService gdprService;
    private final UserRepository userRepository;

    /**
     * POST /api/gdpr/consent
     * Enregistrer ou mettre à jour un consentement RGPD
     */
    @PostMapping("/consent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GdprConsent> recordConsent(
            @RequestBody GdprConsentRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        User user = getUserFromAuthentication(authentication);
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        GdprConsent consent = gdprService.recordConsent(
                user.getId(),
                request.getConsentType(),
                request.getConsented(),
                ipAddress,
                userAgent);

        return ResponseEntity.ok(consent);
    }

    /**
     * GET /api/gdpr/consent
     * Récupérer tous les consentements de l'utilisateur connecté
     */
    @GetMapping("/consent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GdprConsent>> getUserConsents(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        List<GdprConsent> consents = gdprService.getUserConsents(user.getId());
        return ResponseEntity.ok(consents);
    }

    /**
     * GET /api/gdpr/export
     * Exporter toutes les données personnelles (Article 15 RGPD)
     */
    @GetMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GdprDataExportResponse> exportUserData(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Map<String, Object> data = gdprService.exportUserData(user.getId());

        GdprDataExportResponse response = GdprDataExportResponse.builder()
                .userId(user.getId())
                .exportDate(java.time.LocalDateTime.now())
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/gdpr/deletion-request
     * Demander la suppression des données personnelles (Droit à l'oubli - Article
     * 17)
     */
    @PostMapping("/deletion-request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GdprDeletionRequestResponse> requestDataDeletion(
            Authentication authentication,
            HttpServletRequest httpRequest) {

        User user = getUserFromAuthentication(authentication);
        String ipAddress = getClientIpAddress(httpRequest);

        DataDeletionRequest request = gdprService.requestDataDeletion(user.getId(), ipAddress);

        GdprDeletionRequestResponse response = GdprDeletionRequestResponse.builder()
                .requestId(request.getId())
                .status(request.getStatus().name())
                .message("Votre demande de suppression a été enregistrée. Elle sera traitée dans les 30 jours.")
                .build();

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * GET /api/gdpr/deletion-requests
     * Récupérer toutes les demandes de suppression (Admin uniquement)
     */
    @GetMapping("/deletion-requests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<DataDeletionRequest>> getAllDeletionRequests() {
        List<DataDeletionRequest> requests = gdprService.getAllDeletionRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * GET /api/gdpr/deletion-requests/pending
     * Récupérer les demandes en attente (Admin uniquement)
     */
    @GetMapping("/deletion-requests/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<DataDeletionRequest>> getPendingDeletionRequests() {
        List<DataDeletionRequest> requests = gdprService.getPendingDeletionRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * POST /api/gdpr/deletion-requests/{id}/process
     * Traiter une demande de suppression (Admin uniquement)
     * ATTENTION : Opération irréversible
     */
    @PostMapping("/deletion-requests/{id}/process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, String>> processDeletionRequest(
            @PathVariable UUID id,
            Authentication authentication) {

        User processedBy = getUserFromAuthentication(authentication);
        gdprService.processDataDeletion(id, processedBy.getId());

        return ResponseEntity.ok(Map.of(
                "message", "La demande de suppression a été traitée avec succès",
                "requestId", id.toString()));
    }

    /**
     * Récupérer l'utilisateur depuis l'authentification
     */
    private User getUserFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Récupérer l'adresse IP du client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
