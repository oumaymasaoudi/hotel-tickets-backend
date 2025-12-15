package com.hotel.tickethub.dto;

import com.hotel.tickethub.model.enums.TicketStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateTicketStatusRequest {
    private TicketStatus status;
    private UUID technicianId;
}
