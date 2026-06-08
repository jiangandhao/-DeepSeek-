package com.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("diet_records")
public class DietRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String mealType;
    private String foodName;
    private Double foodAmount;
    private Double calorie;
    private Double carbohydrate;
    private Double protein;
    private Double fat;
    private LocalDate recordDate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
