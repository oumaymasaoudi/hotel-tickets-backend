package com.hotel.tickethub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelRequest {
    private String name;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String zipCode;
    private UUID planId;
}
