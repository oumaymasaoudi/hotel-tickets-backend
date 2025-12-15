package com.hotel.tickethub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateTicketRequest {
    @NotNull
    private UUID hotelId;
    
    @NotNull
    private UUID categoryId;
    
    @NotBlank
    @Email
    private String clientEmail;
    
    private String clientPhone;
    
    @NotBlank
    private String description;
    
    private Boolean isUrgent;
}
