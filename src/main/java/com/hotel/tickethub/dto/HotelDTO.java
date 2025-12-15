package com.hotel.tickethub.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class HotelDTO {
    private UUID id;
    private String name;
    private String address;
    private String email;
    private String phone;
    private UUID planId;
    private String planName;
    private Boolean isActive;
}
