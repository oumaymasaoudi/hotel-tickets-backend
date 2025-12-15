package com.hotel.tickethub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    private String name;
    private String icon;
    private String color;
    private Boolean isMandatory = false;
    private BigDecimal additionalCost;
}

