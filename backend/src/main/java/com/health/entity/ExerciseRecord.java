package com.health.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("exercise_record")
public class ExerciseRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;
    private Integer durationMin;
    private String intensity;
    private Integer calories;
    private LocalDateTime doneAt;
    private LocalDateTime createdAt;
}
