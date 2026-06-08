package com.health.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long centerId;
    private String centerName;
    private LocalDate examDate;
    @com.baomidou.mybatisplus.annotation.TableField("`package`")
    private String pkg;
    private String status;
    private LocalDateTime createdAt;
}
