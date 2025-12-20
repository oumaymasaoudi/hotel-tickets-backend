package com.hotel.tickethub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private BigDecimal amount;
    private LocalDateTime nextPaymentDate;
    private String paymentMethod; // CARD, TRANSFER, CHECK, BANK_WIRE
    private String paymentReference;
    private String notes;
}
