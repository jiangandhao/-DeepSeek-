package com.health.service;

import com.health.entity.BloodPressureRecord;

import java.util.List;
import java.util.Map;

public interface BloodPressureService {

    /**
     * 获取血压记录列表
     */
    List<BloodPressureRecord> getRecords(Long userId);

    /**
     * 获取血压趋势数据（最近7天）
     */
    Map<String, Object> getTrend(Long userId, String period);

    /**
     * 添加血压记录
     */
    BloodPressureRecord addRecord(BloodPressureRecord record);

    /**
     * 更新血压记录
     */
    BloodPressureRecord updateRecord(BloodPressureRecord record);

    /**
     * 删除血压记录
     */
    boolean deleteRecord(Long id);
}
