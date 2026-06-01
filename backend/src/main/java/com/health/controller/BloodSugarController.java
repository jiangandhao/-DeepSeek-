package com.health.controller;

import com.health.entity.BloodSugarRecord;
import com.health.service.BloodSugarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blood-sugar")
public class BloodSugarController {

    @Autowired
    private BloodSugarService bloodSugarService;

    @GetMapping("/records")
    public List<BloodSugarRecord> getRecords(@RequestParam Long userId) {
        return bloodSugarService.getRecords(userId);
    }

    @PostMapping("/records")
    public void addRecord(@RequestBody BloodSugarRecord record) {
        bloodSugarService.addRecord(record);
    }

    @GetMapping("/trend")
    public Map<String, Object> getTrend(@RequestParam Long userId, @RequestParam String period) {
        return bloodSugarService.getTrend(userId, period);
    }

    @GetMapping("/prediction")
    public Map<String, Object> getPrediction(@RequestParam Long userId) {
        return bloodSugarService.getPrediction(userId);
    }

    @PostMapping("/diet/recommendation")
    public List<Map<String, Object>> getDietRecommendation(@RequestBody Map<String, Double> params) {
        return bloodSugarService.getDietRecommendation(params.get("bloodSugar"));
    }

    @PostMapping("/exercise/recommendation")
    public List<Map<String, Object>> getExerciseRecommendation(@RequestBody Map<String, Double> params) {
        return bloodSugarService.getExerciseRecommendation(params.get("bloodSugar"));
    }
}
