package com.health.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GlucoseRecordRequest {
    @NotNull(message = "血糖值不能为空")
    private BigDecimal valueMmol;

    @NotBlank(message = "测量时段不能为空")
    private String period; // FASTING/BEFORE_MEAL/AFTER_MEAL/BEDTIME/RANDOM

    @NotNull(message = "测量时间不能为空")
    private LocalDateTime measuredAt;

    private String note;
}
