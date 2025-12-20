package com.hotel.tickethub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketImageDTO {
    private UUID id;
    private String storagePath;
    private String fileName;
}

