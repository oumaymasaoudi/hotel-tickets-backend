package com.hotel.tickethub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GdprDataExportResponse {
    private UUID userId;
    private LocalDateTime exportDate;
    private Map<String, Object> data;
}

