package com.hotel.tickethub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GdprConsentRequest {
    
    @NotBlank(message = "Le type de consentement est requis")
    private String consentType; // DATA_PROCESSING, MARKETING, ANALYTICS, THIRD_PARTY
    
    @NotNull(message = "Le statut du consentement est requis")
    private Boolean consented;
}

