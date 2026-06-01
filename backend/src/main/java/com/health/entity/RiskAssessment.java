package com.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("risk_assessments")
public class RiskAssessment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String riskType;
    private Integer riskLevel;
    private Double riskScore;
    private String description;
    private String suggestions;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
