package com.hotel.tickethub.service;

import com.hotel.tickethub.model.AuditLog;
import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.User;
import com.hotel.tickethub.repository.AuditLogRepository;
import com.hotel.tickethub.repository.HotelRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final HotelRepository hotelRepository;
    private final ObjectMapper objectMapper;

    /**
     * Logger une action critique
     * Règle 12: Journal des activités
     *
     * @param user Utilisateur qui effectue l'action
     * @param action Type d'action (CREATE_TICKET, ASSIGN_TICKET, etc.)
     * @param entityType Type d'entité (Ticket, Payment, User, etc.)
     * @param entityId ID de l'entité affectée
     * @param hotelId Hôtel concerné
     * @param changes Changements effectués (JSON)
     * @param description Description textuelle
     * @param ipAddress Adresse IP
     */
    public AuditLog logAction(
            User user,
            String action,
            String entityType,
            UUID entityId,
            Hotel hotel,
            Object changes,
            String description,
            String ipAddress) {

        String changesJson = null;
        try {
            if (changes != null) {
                changesJson = objectMapper.writeValueAsString(changes);
            }
        } catch (Exception e) {
            changesJson = changes != null ? changes.toString() : null;
        }

        AuditLog log = AuditLog.builder()
            .user(user)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .hotel(hotel)
            .changes(changesJson)
            .description(description)
            .ipAddress(ipAddress)
            .timestamp(LocalDateTime.now())
            .build();

        return auditLogRepository.save(log);
    }

    /**
     * Récupérer l'historique d'une entité
     */
    public List<AuditLog> getEntityHistory(String entityType, UUID entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    /**
     * Récupérer l'historique d'un utilisateur
     */
    public List<AuditLog> getUserHistory(UUID userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * Récupérer l'historique d'un hôtel
     */
    public List<AuditLog> getHotelHistory(UUID hotelId) {
        return auditLogRepository.findByHotelIdOrderByTimestampDesc(hotelId);
    }

    /**
     * Récupérer les logs par type d'action
     */
    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action);
    }

    /**
     * Récupérer les logs d'un hôtel pour une période
     * Utilisé pour les rapports
     */
    public List<AuditLog> getHotelLogsByPeriod(UUID hotelId, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByPeriod(hotelId, startDate, endDate);
    }

    /**
     * Récupérer tous les logs (pour SuperAdmin)
     * Triés par date décroissante
     */
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll().stream()
            .sorted((l1, l2) -> l2.getTimestamp().compareTo(l1.getTimestamp()))
            .toList();
    }

    /**
     * Actions standards à logger
     */
    public static final class ActionType {
        public static final String CREATE_TICKET = "CREATE_TICKET";
        public static final String UPDATE_TICKET = "UPDATE_TICKET";
        public static final String ASSIGN_TICKET = "ASSIGN_TICKET";
        public static final String REASSIGN_TICKET = "REASSIGN_TICKET";
        public static final String ESCALATE_TICKET = "ESCALATE_TICKET";
        public static final String ADD_COMMENT = "ADD_COMMENT";
        public static final String CREATE_PAYMENT = "CREATE_PAYMENT";
        public static final String PAYMENT_OVERDUE = "PAYMENT_OVERDUE";
        public static final String PLAN_CHANGED = "PLAN_CHANGED";
        public static final String USER_CREATED = "USER_CREATED";
        public static final String USER_DELETED = "USER_DELETED";
        public static final String USER_ASSIGNED = "USER_ASSIGNED";
        public static final String HOTEL_CREATED = "HOTEL_CREATED";
        public static final String HOTEL_UPDATED = "HOTEL_UPDATED";
    }

    /**
     * Entités standards
     */
    public static final class EntityType {
        public static final String TICKET = "Ticket";
        public static final String PAYMENT = "Payment";
        public static final String USER = "User";
        public static final String HOTEL = "Hotel";
        public static final String PLAN = "Plan";
        public static final String COMMENT = "Comment";
    }
}
