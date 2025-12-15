package com.hotel.tickethub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String action;
    private String entityType;
    private UUID entityId;
    private UUID hotelId;
    private String hotelName;
    private String changes;
    private String ipAddress;
    private String description;
    private LocalDateTime timestamp;
}

