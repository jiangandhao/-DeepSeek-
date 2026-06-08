package com.health.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRequest {
    @NotNull(message = "请选择体检中心")
    private Long centerId;

    @NotNull(message = "请选择体检日期")
    private LocalDate examDate;

    private String pkg; // 体检套餐
}
