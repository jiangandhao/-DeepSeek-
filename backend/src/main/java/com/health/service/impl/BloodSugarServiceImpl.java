package com.health.service.impl;

import com.health.entity.BloodSugarRecord;
import com.health.mapper.BloodSugarRecordMapper;
import com.health.service.BloodSugarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.LocalDateTime;

@Service
public class BloodSugarServiceImpl implements BloodSugarService {

    @Autowired
    private BloodSugarRecordMapper bloodSugarRecordMapper;

    @Override
    public List<BloodSugarRecord> getRecords(Long userId) {
        return bloodSugarRecordMapper.selectByUserId(userId);
    }

    @Override
    public void addRecord(BloodSugarRecord record) {
        record.setCreateTime(LocalDateTime.now());
        bloodSugarRecordMapper.insert(record);
    }

    @Override
    public Map<String, Object> getTrend(Long userId, String period) {
        List<BloodSugarRecord> records = bloodSugarRecordMapper.selectByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("period", period);
        return result;
    }

    @Override
    public Map<String, Object> getPrediction(Long userId) {
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("userId", userId);
        prediction.put("predictedValue", 6.0);
        prediction.put("confidence", 0.85);
        return prediction;
    }

    @Override
    public List<Map<String, Object>> getDietRecommendation(Double bloodSugar) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        Map<String, Object> rec1 = new HashMap<>();
        rec1.put("tag", "推荐");
        rec1.put("type", "success");
        rec1.put("content", "早餐可食用全麦面包配低脂牛奶");
        recommendations.add(rec1);

        Map<String, Object> rec2 = new HashMap<>();
        rec2.put("tag", "注意");
        rec2.put("type", "warning");
        rec2.put("content", "午餐减少精制碳水化合物摄入");
        recommendations.add(rec2);

        return recommendations;
    }

    @Override
    public List<Map<String, Object>> getExerciseRecommendation(Double bloodSugar) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        Map<String, Object> rec1 = new HashMap<>();
        rec1.put("tag", "推荐");
        rec1.put("type", "success");
        rec1.put("content", "餐后30分钟进行15分钟散步");
        recommendations.add(rec1);

        Map<String, Object> rec2 = new HashMap<>();
        rec2.put("tag", "建议");
        rec2.put("type", "info");
        rec2.put("content", "每周进行3-5次有氧运动");
        recommendations.add(rec2);

        return recommendations;
    }
}
