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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@Slf4j
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:5173",
        "http://192.168.58.1:5173",
        "http://13.50.221.51"
})
public class GdprController {

    private final GdprService gdprService;
    private final UserRepository userRepository;
    private final com.hotel.tickethub.repository.UserRoleRepository userRoleRepository;

    /**
     * POST /api/gdpr/consent
     * Enregistrer ou mettre à jour un consentement RGPD
     */
    @PostMapping("/consent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> recordConsent(
            @Valid @RequestBody GdprConsentRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            log.debug("recordConsent called for user: {}", 
                authentication != null ? authentication.getName() : "NULL");
            
            User user = getUserFromRequest(authentication, httpRequest);
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            log.debug("Recording consent - User: {}, Type: {}, Consented: {}", 
                user.getEmail(), request.getConsentType(), request.getConsented());

            GdprConsent consent = gdprService.recordConsent(
                    user.getId(),
                    request.getConsentType(),
                    request.getConsented(),
                    ipAddress,
                    userAgent);

            log.info("Consent recorded successfully - User: {}, Type: {}", 
                user.getEmail(), request.getConsentType());
            
            // Create DTO to avoid infinite recursion
            Map<String, Object> consentDto = new HashMap<>();
            consentDto.put("id", consent.getId());
            consentDto.put("userId", consent.getUser().getId());
            consentDto.put("consentType", consent.getConsentType());
            consentDto.put("consented", consent.getConsented());
            consentDto.put("consentDate", consent.getConsentDate());
            consentDto.put("privacyPolicyVersion", consent.getPrivacyPolicyVersion());
            consentDto.put("ipAddress", consent.getIpAddress());
            consentDto.put("userAgent", consent.getUserAgent());
            consentDto.put("createdAt", consent.getCreatedAt());
            consentDto.put("updatedAt", consent.getUpdatedAt());
            
            return ResponseEntity.ok(consentDto);
        } catch (Exception e) {
            log.error("Error recording consent: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur lors de l'enregistrement du consentement");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * GET /api/gdpr/consent
     * Récupérer tous les consentements de l'utilisateur connecté
     */
    @GetMapping("/consent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getUserConsents(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            log.debug("getUserConsents called for user: {}", 
                authentication != null ? authentication.getName() : "NULL");
            
            User user = getUserFromRequest(authentication, httpRequest);
            log.debug("Fetching consents for user: {}", user.getEmail());
            
            List<GdprConsent> consents = gdprService.getUserConsents(user.getId());
            log.debug("Found {} consents for user: {}", consents.size(), user.getEmail());
            
            // Convert to DTO to avoid infinite recursion
            List<Map<String, Object>> consentDtos = consents.stream().map(consent -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", consent.getId());
                dto.put("userId", consent.getUser().getId());
                dto.put("consentType", consent.getConsentType());
                dto.put("consented", consent.getConsented());
                dto.put("consentDate", consent.getConsentDate());
                dto.put("privacyPolicyVersion", consent.getPrivacyPolicyVersion());
                dto.put("ipAddress", consent.getIpAddress());
                dto.put("userAgent", consent.getUserAgent());
                dto.put("createdAt", consent.getCreatedAt());
                dto.put("updatedAt", consent.getUpdatedAt());
                return dto;
            }).toList();
            
            return ResponseEntity.ok(consentDtos);
        } catch (Exception e) {
            log.error("Error fetching user consents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * GET /api/gdpr/available-consents
     * Récupérer les types de consentements disponibles selon le rôle de l'utilisateur
     */
    @GetMapping("/available-consents")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAvailableConsents(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            log.debug("getAvailableConsents called");
            User user = getUserFromRequest(authentication, httpRequest);
            log.debug("User found: {}", user.getEmail());
            
            com.hotel.tickethub.model.enums.UserRole userRole = getUserRole(user);
            log.debug("User role: {}", userRole);
            
            List<Map<String, Object>> availableConsents = getConsentsForRole(userRole);
            log.debug("Available consents count: {}", availableConsents.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("role", userRole.name());
            response.put("availableConsents", availableConsents);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting available consents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Get user's primary role
     * Uses same methods as AuthService to handle UUID/VARCHAR type issues
     */
    private com.hotel.tickethub.model.enums.UserRole getUserRole(User user) {
        log.debug("getUserRole for user: {}, ID: {}", user.getEmail(), user.getId());
        
        // Try three methods like AuthService
        Optional<com.hotel.tickethub.model.UserRole> userRoleOpt = userRoleRepository.findByUserId(user.getId())
                .or(() -> userRoleRepository.findByUserIdCustom(user.getId()))
                .or(() -> userRoleRepository.findByUserIdNative(user.getId()));
        
        if (userRoleOpt.isPresent()) {
            com.hotel.tickethub.model.enums.UserRole role = userRoleOpt.get().getRole();
            log.debug("Role found: {}", role);
            return role;
        }
        
        log.warn("No role found for user: {}, defaulting to CLIENT", user.getEmail());
        // Default: CLIENT
        return com.hotel.tickethub.model.enums.UserRole.CLIENT;
    }
    
    /**
     * Get available consent types by role
     */
    private List<Map<String, Object>> getConsentsForRole(com.hotel.tickethub.model.enums.UserRole role) {
        List<Map<String, Object>> consents = new java.util.ArrayList<>();
        
        switch (role) {
            case CLIENT:
                // Standard consents for clients
                consents.add(createConsentInfo("DATA_PROCESSING", "Traitement des données personnelles", 
                    "Autoriser le traitement de vos données pour la fourniture du service", true));
                consents.add(createConsentInfo("MARKETING", "Communications marketing", 
                    "Recevoir des emails promotionnels et des offres spéciales", false));
                consents.add(createConsentInfo("ANALYTICS", "Analyse et statistiques", 
                    "Autoriser l'utilisation de vos données pour améliorer le service", false));
                consents.add(createConsentInfo("THIRD_PARTY", "Partage avec des tiers", 
                    "Autoriser le partage de données avec nos partenaires de confiance", false));
                break;
                
            case TECHNICIAN:
                // Professional consents for technicians
                consents.add(createConsentInfo("DATA_PROCESSING", "Traitement des données professionnelles", 
                    "Autoriser le traitement de vos données pour l'exécution de vos missions", true));
                consents.add(createConsentInfo("PROFESSIONAL_DATA", "Données professionnelles", 
                    "Autoriser l'utilisation de vos données professionnelles (spécialités, performances)", false));
                consents.add(createConsentInfo("NOTIFICATIONS", "Notifications professionnelles", 
                    "Recevoir des notifications sur les tickets assignés et les mises à jour", true));
                consents.add(createConsentInfo("ANALYTICS", "Analyse et statistiques", 
                    "Autoriser l'utilisation de vos données pour améliorer le service", false));
                break;
                
            case ADMIN:
                // Management consents for admins
                consents.add(createConsentInfo("DATA_PROCESSING", "Traitement des données administratives", 
                    "Autoriser le traitement de vos données pour la gestion de l'hôtel", true));
                consents.add(createConsentInfo("HOTEL_DATA", "Données de l'hôtel", 
                    "Autoriser l'utilisation des données de votre hôtel pour les rapports et analyses", true));
                consents.add(createConsentInfo("TEAM_MANAGEMENT", "Gestion d'équipe", 
                    "Autoriser l'accès aux données de votre équipe pour la gestion", true));
                consents.add(createConsentInfo("NOTIFICATIONS", "Notifications administratives", 
                    "Recevoir des notifications sur les activités de l'hôtel", true));
                consents.add(createConsentInfo("ANALYTICS", "Analyse et statistiques", 
                    "Autoriser l'utilisation de vos données pour améliorer le service", false));
                break;
                
            case SUPERADMIN:
                // System consents for superadmins
                consents.add(createConsentInfo("DATA_PROCESSING", "Traitement des données système", 
                    "Autoriser le traitement de vos données pour la gestion globale de la plateforme", true));
                consents.add(createConsentInfo("SYSTEM_DATA", "Données système", 
                    "Autoriser l'accès aux données système pour la gestion globale", true));
                consents.add(createConsentInfo("AUDIT_LOGS", "Logs d'audit", 
                    "Autoriser l'accès aux logs d'audit pour la sécurité", true));
                consents.add(createConsentInfo("NOTIFICATIONS", "Notifications système", 
                    "Recevoir des notifications sur les activités système", true));
                consents.add(createConsentInfo("ANALYTICS", "Analyse et statistiques", 
                    "Autoriser l'utilisation de vos données pour améliorer le service", false));
                break;
        }
        
        return consents;
    }
    
    private Map<String, Object> createConsentInfo(String id, String label, String description, boolean required) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", id);
        info.put("label", label);
        info.put("description", description);
        info.put("required", required);
        return info;
    }

    /**
     * GET /api/gdpr/export
     * Exporter toutes les données personnelles (Article 15 RGPD)
     */
    @GetMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GdprDataExportResponse> exportUserData(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            User user = getUserFromRequest(authentication, httpRequest);
            log.info("Exporting user data for user: {}", user.getEmail());
            Map<String, Object> data = gdprService.exportUserData(user.getId());

            GdprDataExportResponse response = GdprDataExportResponse.builder()
                    .userId(user.getId())
                    .exportDate(java.time.LocalDateTime.now())
                    .data(data)
                    .build();

            log.info("User data exported successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error exporting user data: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
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
        try {
            User user = getUserFromRequest(authentication, httpRequest);
            String ipAddress = getClientIpAddress(httpRequest);
            log.info("Data deletion request received for user: {}", user.getEmail());

            DataDeletionRequest request = gdprService.requestDataDeletion(user.getId(), ipAddress);

            GdprDeletionRequestResponse response = GdprDeletionRequestResponse.builder()
                    .requestId(request.getId())
                    .status(request.getStatus().name())
                    .message("Votre demande de suppression a été enregistrée. Elle sera traitée dans les 30 jours.")
                    .build();

            log.info("Data deletion request created successfully for user: {}, requestId: {}", 
                user.getEmail(), request.getId());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (Exception e) {
            log.error("Error creating data deletion request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
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
     * Get user from authentication or X-User-Email header
     */
    private User getUserFromRequest(Authentication authentication, HttpServletRequest request) {
        final String email;
        
        // Try authentication first
        if (authentication != null && authentication.getName() != null) {
            email = authentication.getName();
            log.debug("Getting user from authentication: {}", email);
        } else {
            // Otherwise use X-User-Email header (for dev-token)
            String headerEmail = request.getHeader("X-User-Email");
            if (headerEmail == null || headerEmail.isEmpty()) {
                log.error("No email found in authentication or headers");
                throw new RuntimeException("Authentication required: No user email found");
            }
            email = headerEmail;
            log.debug("Getting user from X-User-Email header: {}", email);
        }
        
        final String finalEmail = email; // Final variable for lambda
        return userRepository.findByEmail(finalEmail)
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", finalEmail);
                    return new RuntimeException("User not found for email: " + finalEmail);
                });
    }
    
    /**
     * Get user from authentication (legacy method)
     */
    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            log.error("Authentication is null");
            throw new RuntimeException("Authentication required");
        }
        
        String email = authentication.getName();
        log.debug("Getting user from email: {}", email);
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", email);
                    return new RuntimeException("User not found");
                });
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
