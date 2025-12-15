package com.hotel.tickethub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private UUID hotelId;
    private String hotelName;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private LocalDateTime nextPaymentDate;
    private String status; // PAID, PENDING, OVERDUE
    private String paymentMethod;
    private String paymentReference;
    private String notes;
}
