package com.health.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("health_profile")
public class HealthProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private String familyHistory;
    private String chronic;
    private Integer diabetesType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
