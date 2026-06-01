package com.health.service;

import com.health.entity.BloodSugarRecord;
import java.util.List;
import java.util.Map;

public interface BloodSugarService {
    List<BloodSugarRecord> getRecords(Long userId);
    void addRecord(BloodSugarRecord record);
    Map<String, Object> getTrend(Long userId, String period);
    Map<String, Object> getPrediction(Long userId);
    List<Map<String, Object>> getDietRecommendation(Double bloodSugar);
    List<Map<String, Object>> getExerciseRecommendation(Double bloodSugar);
}
