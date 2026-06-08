package com.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blood_pressure_records")
public class BloodPressureRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer systolic;

    private Integer diastolic;

    private Integer heartRate;

    private LocalDateTime measureTime;

    private String note;

    private LocalDateTime createTime;
}
