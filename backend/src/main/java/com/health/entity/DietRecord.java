package com.health.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("diet_record")
public class DietRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String mealType;
    private String food;
    private Integer calories;
    private BigDecimal carbsG;
    private LocalDateTime eatenAt;
    private LocalDateTime createdAt;
}
