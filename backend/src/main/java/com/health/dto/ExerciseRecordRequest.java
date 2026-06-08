package com.health.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExerciseRecordRequest {
    @NotBlank(message = "运动类型不能为空")
    private String type;

    @NotNull(message = "时长不能为空")
    private Integer durationMin;

    @NotBlank(message = "强度不能为空")
    private String intensity; // LOW/MEDIUM/HIGH

    private Integer calories;

    @NotNull(message = "运动时间不能为空")
    private LocalDateTime doneAt;
}
