package com.hotel.tickethub.dto;

import com.hotel.tickethub.model.enums.TicketStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TicketResponse {
    private UUID id;
    private String ticketNumber;
    private UUID hotelId;
    private String hotelName;
    private UUID categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private String clientEmail;
    private String clientPhone;
    private String description;
    private TicketStatus status;
    private Boolean isUrgent;
    private UUID assignedTechnicianId;
    private String assignedTechnicianName;
    private LocalDateTime slaDeadline;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TicketImageDTO> images;
}
