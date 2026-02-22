package com.hotel.tickethub.service;

import com.hotel.tickethub.dto.AuditLogRequest;
import com.hotel.tickethub.exception.ConflictException;
import com.hotel.tickethub.exception.ResourceNotFoundException;
import com.hotel.tickethub.exception.StorageException;
import com.hotel.tickethub.model.*;
import com.hotel.tickethub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service RGPD - Conformité avec le Règlement Général sur la Protection des Données
 * 
 * Fonctionnalités :
 * - Gestion du consentement (Article 6 et 7)
 * - Droit à l'oubli (Article 17)
 * - Export des données personnelles (Article 15)
 * - Traçabilité des actions
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GdprService {

    private final GdprConsentRepository consentRepository;
    private final DataDeletionRequestRepository deletionRequestRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;
    private static final String PRIVACY_POLICY_VERSION = "1.0";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String CREATED_AT_FIELD = "createdAt";

    /**
     * Enregistrer ou mettre à jour un consentement RGPD
     * Article 7 : Conditions du consentement
     */
    public GdprConsent recordConsent(UUID userId, String consentType, Boolean consented, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_MESSAGE));

        GdprConsent existingConsent = consentRepository
                .findByUserIdAndConsentType(userId, consentType)
                .orElse(null);

        GdprConsent consent;
        if (existingConsent != null) {
            existingConsent.setConsented(consented);
            existingConsent.setConsentDate(LocalDateTime.now());
            existingConsent.setPrivacyPolicyVersion(PRIVACY_POLICY_VERSION);
            existingConsent.setIpAddress(ipAddress);
            existingConsent.setUserAgent(userAgent);
            consent = consentRepository.save(existingConsent);
        } else {
            consent = GdprConsent.builder()
                    .user(user)
                    .consentType(consentType)
                    .consented(consented)
                    .consentDate(LocalDateTime.now())
                    .privacyPolicyVersion(PRIVACY_POLICY_VERSION)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();
            consent = consentRepository.save(consent);
        }

        // Log de l'action
        boolean isConsented = Boolean.TRUE.equals(consented);
        auditLogService.logAction(AuditLogRequest.builder()
                .user(user)
                .action("GDPR_CONSENT_" + (isConsented ? "GRANTED" : "REVOKED"))
                .entityType("GdprConsent")
                .entityId(consent.getId())
                .hotel(user.getHotel())
                .changes(Map.of("consentType", consentType, "consented", consented))
                .description("Consentement RGPD " + (isConsented ? "accordé" : "révoqué") + " : " + consentType)
                .ipAddress(ipAddress)
                .build());

        log.info("GDPR consent recorded: userId={}, type={}, consented={}", userId, consentType, consented);
        return consent;
    }

    /**
     * Vérifier si un utilisateur a donné son consentement
     */
    public Boolean hasConsent(UUID userId, String consentType) {
        return consentRepository.findByUserIdAndConsentType(userId, consentType)
                .map(GdprConsent::getConsented)
                .orElse(false);
    }

    /**
     * Récupérer tous les consentements d'un utilisateur
     */
    public List<GdprConsent> getUserConsents(UUID userId) {
        return consentRepository.findByUserId(userId);
    }

    /**
     * Exporter toutes les données personnelles d'un utilisateur (Article 15)
     */
    public Map<String, Object> exportUserData(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_MESSAGE));

        Map<String, Object> data = new HashMap<>();
        
        // Données de profil
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("fullName", user.getFullName());
        profile.put("phone", user.getPhone());
        profile.put("photoUrl", user.getPhotoUrl());
        profile.put(CREATED_AT_FIELD, user.getCreatedAt());
        profile.put("updatedAt", user.getUpdatedAt());
        data.put("profile", profile);

        // Tickets (par email du client)
        List<Map<String, Object>> tickets = ticketRepository.findByClientEmail(user.getEmail())
                .stream()
                .map(ticket -> {
                    Map<String, Object> t = new HashMap<>();
                    t.put("id", ticket.getId());
                    t.put("ticketNumber", ticket.getTicketNumber());
                    t.put("description", ticket.getDescription());
                    t.put("status", ticket.getStatus());
                    t.put("isUrgent", ticket.getIsUrgent());
                    t.put(CREATED_AT_FIELD, ticket.getCreatedAt());
                    t.put("updatedAt", ticket.getUpdatedAt());
                    return t;
                })
                .toList();
        data.put("tickets", tickets);

        // Paiements (si l'utilisateur est admin d'un hôtel)
        List<Map<String, Object>> payments = new java.util.ArrayList<>();
        if (user.getHotel() != null) {
            payments = paymentRepository.findByHotelIdOrderByPaymentDateDesc(user.getHotel().getId())
                    .stream()
                    .map(payment -> {
                        Map<String, Object> p = new HashMap<>();
                        p.put("id", payment.getId());
                        p.put("amount", payment.getAmount());
                        p.put("status", payment.getStatus());
                        p.put("paymentMethod", payment.getPaymentMethod());
                        p.put("paymentDate", payment.getPaymentDate());
                        p.put(CREATED_AT_FIELD, payment.getCreatedAt());
                        // Ne pas inclure les données sensibles (numéro de carte, etc.)
                        return p;
                    })
                    .toList();
        }
        data.put("payments", payments);

        // Consentements RGPD
        List<Map<String, Object>> consents = consentRepository.findByUserId(userId)
                .stream()
                .map(consent -> {
                    Map<String, Object> c = new HashMap<>();
                    c.put("type", consent.getConsentType());
                    c.put("consented", consent.getConsented());
                    c.put("consentDate", consent.getConsentDate());
                    c.put("privacyPolicyVersion", consent.getPrivacyPolicyVersion());
                    return c;
                })
                .toList();
        data.put("gdprConsents", consents);

        // Logs d'audit
        List<Map<String, Object>> auditLogs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(log -> {
                    Map<String, Object> a = new HashMap<>();
                    a.put("action", log.getAction());
                    a.put("entityType", log.getEntityType());
                    a.put("description", log.getDescription());
                    a.put("timestamp", log.getTimestamp());
                    return a;
                })
                .toList();
        data.put("auditLogs", auditLogs);

        // Log de l'export
        auditLogService.logAction(AuditLogRequest.builder()
                .user(user)
                .action("GDPR_DATA_EXPORT")
                .entityType("User")
                .entityId(userId)
                .hotel(user.getHotel())
                .changes(null)
                .description("Export des données personnelles demandé")
                .ipAddress(null)
                .build());

        log.info("GDPR data export completed for userId={}", userId);
        return data;
    }

    /**
     * Créer une demande de suppression de données (Droit à l'oubli - Article 17)
     */
    public DataDeletionRequest requestDataDeletion(UUID userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_MESSAGE));

        // Vérifier s'il existe déjà une demande en cours
        List<DataDeletionRequest> pendingRequests = deletionRequestRepository
                .findByUserId(userId)
                .stream()
                .filter(req -> req.getStatus() == DataDeletionRequest.DeletionStatus.PENDING
                        || req.getStatus() == DataDeletionRequest.DeletionStatus.PROCESSING)
                .toList();

        if (!pendingRequests.isEmpty()) {
            throw new ConflictException("Une demande de suppression est déjà en cours");
        }

        DataDeletionRequest request = DataDeletionRequest.builder()
                .user(user)
                .status(DataDeletionRequest.DeletionStatus.PENDING)
                .ipAddress(ipAddress)
                .confirmationSent(false)
                .build();

        request = deletionRequestRepository.save(request);

        // Log de la demande
        auditLogService.logAction(AuditLogRequest.builder()
                .user(user)
                .action("GDPR_DELETION_REQUESTED")
                .entityType("DataDeletionRequest")
                .entityId(request.getId())
                .hotel(user.getHotel())
                .changes(null)
                .description("Demande de suppression des données (droit à l'oubli)")
                .ipAddress(ipAddress)
                .build());

        log.info("GDPR deletion request created: userId={}, requestId={}", userId, request.getId());
        return request;
    }

    /**
     * Traiter une demande de suppression de données
     * ATTENTION : Cette opération est irréversible
     */
    public void processDataDeletion(UUID requestId, UUID processedByUserId) {
        DataDeletionRequest request = deletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Deletion request not found"));

        if (request.getStatus() == DataDeletionRequest.DeletionStatus.COMPLETED) {
            throw new ConflictException("Cette demande a déjà été traitée");
        }

        User user = request.getUser();
        User processedBy = userRepository.findById(processedByUserId)
                .orElseThrow(() -> new RuntimeException("Processing user not found"));

        request.setStatus(DataDeletionRequest.DeletionStatus.PROCESSING);
        request.setProcessedBy(processedBy);
        deletionRequestRepository.save(request);

        try {
            // 1. Anonymiser les tickets (garder les données pour statistiques mais anonymiser)
            ticketRepository.findByClientEmail(user.getEmail()).forEach(ticket -> {
                ticket.setClientEmail("deleted_" + ticket.getId()); // Anonymiser l'email
                ticket.setClientPhone(null); // Supprimer le téléphone
                ticketRepository.save(ticket);
            });

            // 2. Supprimer les consentements RGPD
            consentRepository.findByUserId(user.getId()).forEach(consentRepository::delete);

            // 3. Anonymiser les logs d'audit (garder pour traçabilité mais anonymiser)
            auditLogRepository.findByUserIdOrderByTimestampDesc(user.getId()).forEach(log -> {
                log.setUser(null); // Anonymiser
                auditLogRepository.save(log);
            });

            // 4. Supprimer les rôles utilisateur
            // (géré par cascade dans UserRole)

            // 5. Finalement, supprimer l'utilisateur
            userRepository.delete(user);

            // 6. Marquer la demande comme complétée
            request.setStatus(DataDeletionRequest.DeletionStatus.COMPLETED);
            request.setProcessedAt(LocalDateTime.now());
            request.setConfirmationSent(true);
            deletionRequestRepository.save(request);

            // Log final
            auditLogService.logAction(AuditLogRequest.builder()
                    .user(processedBy)
                    .action("GDPR_DELETION_COMPLETED")
                    .entityType("DataDeletionRequest")
                    .entityId(requestId)
                    .hotel(null)
                    .changes(Map.of("deletedUserId", user.getId().toString()))
                    .description("Suppression des données complétée pour l'utilisateur : " + user.getEmail())
                    .ipAddress(null)
                    .build());

            log.info("GDPR deletion completed: requestId={}, userId={}", requestId, user.getId());
        } catch (Exception e) {
            request.setStatus(DataDeletionRequest.DeletionStatus.REJECTED);
            request.setRejectionReason("Erreur lors du traitement : " + e.getMessage());
            deletionRequestRepository.save(request);
            log.error("Error processing GDPR deletion request: requestId={}", requestId, e);
            throw new StorageException("Erreur lors de la suppression des données", e);
        }
    }

    /**
     * Récupérer toutes les demandes de suppression
     */
    public List<DataDeletionRequest> getAllDeletionRequests() {
        return deletionRequestRepository.findAll();
    }

    /**
     * Récupérer les demandes en attente
     */
    public List<DataDeletionRequest> getPendingDeletionRequests() {
        return deletionRequestRepository.findByStatus(DataDeletionRequest.DeletionStatus.PENDING);
    }
}

