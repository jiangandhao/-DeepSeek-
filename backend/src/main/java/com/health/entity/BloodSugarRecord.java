package com.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("blood_sugar_records")
public class BloodSugarRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Double value;
    private String type;
    private LocalDateTime measureTime;
    private String note;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
