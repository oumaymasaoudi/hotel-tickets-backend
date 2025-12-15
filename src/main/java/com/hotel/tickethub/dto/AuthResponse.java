package com.hotel.tickethub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String userId;

    @JsonProperty("fullName")
    private String fullName;

    private String role;

    @JsonProperty("hotelId")
    private String hotelId;
}
