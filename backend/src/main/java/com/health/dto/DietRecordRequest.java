package com.health.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DietRecordRequest {
    @NotBlank(message = "餐次不能为空")
    private String mealType; // BREAKFAST/LUNCH/DINNER/SNACK

    @NotBlank(message = "食物描述不能为空")
    private String food;

    private Integer calories;
    private BigDecimal carbsG;

    @NotNull(message = "进食时间不能为空")
    private LocalDateTime eatenAt;
}
