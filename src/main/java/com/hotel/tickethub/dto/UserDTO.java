package com.hotel.tickethub.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private String photoUrl;
    private Boolean isActive;
    private UUID hotelId;
}
