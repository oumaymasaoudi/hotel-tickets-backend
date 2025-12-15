package com.hotel.tickethub.controller;

import com.hotel.tickethub.dto.AuditLogDTO;
import com.hotel.tickethub.model.AuditLog;
import com.hotel.tickethub.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:5173",
        "http://192.168.58.1:5173"
})
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * GET /api/audit-logs/all
     * Récupérer tous les logs d'audit (pour SuperAdmin)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<AuditLogDTO>> getAllLogs() {
        List<AuditLog> logs = auditLogService.getAllLogs();
        List<AuditLogDTO> dtos = logs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/audit-logs/hotel/{hotelId}
     * Récupérer les logs d'un hôtel
     */
    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<AuditLogDTO>> getHotelLogs(@PathVariable UUID hotelId) {
        List<AuditLog> logs = auditLogService.getHotelHistory(hotelId);
        List<AuditLogDTO> dtos = logs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/audit-logs/action/{action}
     * Récupérer les logs par type d'action
     */
    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<AuditLogDTO>> getLogsByAction(@PathVariable String action) {
        List<AuditLog> logs = auditLogService.getLogsByAction(action);
        List<AuditLogDTO> dtos = logs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private AuditLogDTO convertToDTO(AuditLog log) {
        return AuditLogDTO.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userName(log.getUser() != null ? log.getUser().getFullName() : "Système")
                .userEmail(log.getUser() != null ? log.getUser().getEmail() : null)
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .hotelId(log.getHotel() != null ? log.getHotel().getId() : null)
                .hotelName(log.getHotel() != null ? log.getHotel().getName() : null)
                .changes(log.getChanges())
                .ipAddress(log.getIpAddress())
                .description(log.getDescription())
                .timestamp(log.getTimestamp())
                .build();
    }
}

