package com.hotel.tickethub.dto;

import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO pour regrouper les paramètres de logAction afin de réduire le nombre de paramètres
 */
@Data
@Builder
public class AuditLogRequest {
    private User user;
    private String action;
    private String entityType;
    private UUID entityId;
    private Hotel hotel;
    private Object changes;
    private String description;
    private String ipAddress;
}
