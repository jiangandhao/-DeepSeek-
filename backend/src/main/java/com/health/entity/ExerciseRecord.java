package com.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exercise_records")
public class ExerciseRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String exerciseType;
    private Integer duration;
    private Double calorieConsumed;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String note;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
