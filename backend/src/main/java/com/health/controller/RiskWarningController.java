package com.health.controller;

import com.health.entity.RiskAssessment;
import com.health.entity.WarningRecord;
import com.health.service.RiskWarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 疾病风险预警控制器
 */
@RestController
@RequestMapping("/api/risk-warning")
public class RiskWarningController {

    @Autowired
    private RiskWarningService riskWarningService;

    /**
     * 获取风险评估结果
     */
    @GetMapping("/assessment")
    public Map<String, Object> getAssessment(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        Map<String, Object> result = new HashMap<>();
        result.put("overallScore", 72);
        result.put("riskLevel", "中风险");
        result.put("diseaseRisks", riskWarningService.getDiseaseRisks(userId));
        return result;
    }

    /**
     * 刷新风险评估
     */
    @PostMapping("/refresh-assessment")
    public Map<String, Object> refreshAssessment(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        riskWarningService.refreshAssessment(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "评估已更新");
        return result;
    }

    /**
     * 获取预警列表
     */
    @GetMapping("/warnings")
    public List<WarningRecord> getWarnings(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "all") String type) {
        Long userId = getUserIdFromToken(token);
        return riskWarningService.getWarnings(userId, type);
    }

    /**
     * 标记预警已读
     */
    @PutMapping("/warning/{id}/read")
    public Map<String, Object> markWarningRead(@PathVariable Long id) {
        riskWarningService.markWarningRead(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    /**
     * 获取预警设置
     */
    @GetMapping("/settings")
    public Map<String, Object> getAlertSettings(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        return riskWarningService.getAlertSettings(userId);
    }

    /**
     * 更新预警设置
     */
    @PutMapping("/settings")
    public Map<String, Object> updateAlertSettings(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> settings) {
        Long userId = getUserIdFromToken(token);
        riskWarningService.updateAlertSettings(userId, settings);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "设置已保存");
        return result;
    }

    /**
     * 获取预防方案列表
     */
    @GetMapping("/prevention-plans")
    public List<Map<String, Object>> getPreventionPlans(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        return riskWarningService.getPreventionPlans(userId);
    }

    /**
     * 获取疾病预防方案详情
     */
    @GetMapping("/prevention-plan/{diseaseId}")
    public Map<String, Object> getPreventionPlanDetail(@PathVariable Long diseaseId) {
        return riskWarningService.getPreventionPlanDetail(diseaseId);
    }

    private Long getUserIdFromToken(String token) {
        // TODO: 从JWT token中解析用户ID
        return 1L;
    }
}
