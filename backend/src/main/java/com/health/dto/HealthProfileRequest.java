package com.health.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class HealthProfileRequest {
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private String familyHistory;
    private String chronic;
    private Integer diabetesType; // 0无 1型 2型 3妊娠
}
