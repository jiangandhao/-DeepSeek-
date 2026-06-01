package com.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("checkup_reports")
public class CheckupReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String reportNo;
    private LocalDateTime checkupDate;
    private String hospitalName;
    private String reportUrl;
    private String summary;
    private Integer riskLevel;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
