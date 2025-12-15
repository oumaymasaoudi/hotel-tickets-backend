package com.hotel.tickethub.repository;

import com.hotel.tickethub.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Récupérer l'historique d'une entité
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, UUID entityId);

    /**
     * Récupérer l'historique d'un utilisateur
     */
    List<AuditLog> findByUserIdOrderByTimestampDesc(UUID userId);

    /**
     * Récupérer l'historique d'un hôtel
     */
    List<AuditLog> findByHotelIdOrderByTimestampDesc(UUID hotelId);

    /**
     * Rechercher par action
     */
    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    /**
     * Récupérer les logs d'une période
     */
    @Query("SELECT a FROM AuditLog a WHERE a.hotel.id = :hotelId " +
           "AND a.timestamp >= :startDate AND a.timestamp <= :endDate " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findByPeriod(
        @Param("hotelId") UUID hotelId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Récupérer les logs par type d'action et hôtel
     */
    @Query("SELECT a FROM AuditLog a WHERE a.hotel.id = :hotelId AND a.action = :action")
    List<AuditLog> findByHotelAndAction(
        @Param("hotelId") UUID hotelId,
        @Param("action") String action
    );
}
