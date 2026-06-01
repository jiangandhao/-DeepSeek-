package com.health.service.impl;

import com.health.entity.WarningRecord;
import com.health.service.RiskWarningService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 疾病风险预警服务实现
 */
@Service
public class RiskWarningServiceImpl implements RiskWarningService {

    @Override
    public List<Map<String, Object>> getDiseaseRisks(Long userId) {
        List<Map<String, Object>> risks = new ArrayList<>();

        Map<String, Object> diabetes = new HashMap<>();
        diabetes.put("name", "糖尿病");
        diabetes.put("riskLevel", "中风险");
        diabetes.put("score", 58);
        diabetes.put("factors", Arrays.asList("空腹血糖偏高", "BMI 24.5", "家族史"));
        diabetes.put("trend", "上升");
        risks.add(diabetes);

        Map<String, Object> hypertension = new HashMap<>();
        hypertension.put("name", "高血压");
        hypertension.put("riskLevel", "中风险");
        hypertension.put("score", 45);
        hypertension.put("factors", Arrays.asList("收缩压偏高", "高盐饮食", "缺乏运动"));
        hypertension.put("trend", "稳定");
        risks.add(hypertension);

        Map<String, Object> fattyLiver = new HashMap<>();
        fattyLiver.put("name", "脂肪肝");
        fattyLiver.put("riskLevel", "高风险");
        fattyLiver.put("score", 72);
        fattyLiver.put("factors", Arrays.asList("BMI 28.5", "甘油三酯高", "饮酒"));
        fattyLiver.put("trend", "上升");
        risks.add(fattyLiver);

        Map<String, Object> heartDisease = new HashMap<>();
        heartDisease.put("name", "冠心病");
        heartDisease.put("riskLevel", "低风险");
        heartDisease.put("score", 25);
        heartDisease.put("factors", Arrays.asList("年龄", "血脂偏高"));
        heartDisease.put("trend", "稳定");
        risks.add(heartDisease);

        return risks;
    }

    @Override
    public void refreshAssessment(Long userId) {
        // TODO: 重新计算风险评估
    }

    @Override
    public List<WarningRecord> getWarnings(Long userId, String type) {
        List<WarningRecord> warnings = new ArrayList<>();

        if ("all".equals(type) || "critical".equals(type)) {
            WarningRecord critical = new WarningRecord();
            critical.setId(1L);
            critical.setUserId(userId);
            critical.setLevel("critical");
            critical.setTitle("血糖异常升高");
            critical.setDescription("您最近一次空腹血糖检测值为 8.5mmol/L，远超正常范围");
            critical.setTime("2024-01-15 09:30");
            critical.setRead(false);
            warnings.add(critical);
        }

        if ("all".equals(type) || "warning".equals(type)) {
            WarningRecord warning = new WarningRecord();
            warning.setId(2L);
            warning.setUserId(userId);
            warning.setLevel("warning");
            warning.setTitle("血压波动较大");
            warning.setDescription("您近一周血压波动较大，收缩压在125-145mmHg之间");
            warning.setTime("2024-01-14 14:20");
            warning.setRead(false);
            warnings.add(warning);
        }

        if ("all".equals(type) || "info".equals(type)) {
            WarningRecord info = new WarningRecord();
            info.setId(3L);
            info.setUserId(userId);
            info.setLevel("info");
            info.setTitle("复查提醒");
            info.setDescription("距离您上次甲状腺检查已过6个月，建议进行复查");
            info.setTime("2024-01-11 09:00");
            info.setRead(true);
            warnings.add(info);
        }

        return warnings;
    }

    @Override
    public void markWarningRead(Long id) {
        // TODO: 标记预警已读
    }

    @Override
    public Map<String, Object> getAlertSettings(Long userId) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("methods", Arrays.asList("app", "sms"));
        settings.put("emergencyPhone", "13800138000");
        return settings;
    }

    @Override
    public void updateAlertSettings(Long userId, Map<String, Object> settings) {
        // TODO: 更新预警设置
    }

    @Override
    public List<Map<String, Object>> getPreventionPlans(Long userId) {
        List<Map<String, Object>> plans = new ArrayList<>();

        Map<String, Object> diabetes = new HashMap<>();
        diabetes.put("id", 1);
        diabetes.put("name", "糖尿病");
        diabetes.put("riskLevel", "中风险");
        diabetes.put("reduction", 35);
        diabetes.put("cycle", 90);
        diabetes.put("progress", 45);
        plans.add(diabetes);

        Map<String, Object> hypertension = new HashMap<>();
        hypertension.put("id", 2);
        hypertension.put("name", "高血压");
        hypertension.put("riskLevel", "中风险");
        hypertension.put("reduction", 30);
        hypertension.put("cycle", 60);
        hypertension.put("progress", 60);
        plans.add(hypertension);

        Map<String, Object> fattyLiver = new HashMap<>();
        fattyLiver.put("id", 3);
        fattyLiver.put("name", "脂肪肝");
        fattyLiver.put("riskLevel", "高风险");
        fattyLiver.put("reduction", 40);
        fattyLiver.put("cycle", 120);
        fattyLiver.put("progress", 20);
        plans.add(fattyLiver);

        return plans;
    }

    @Override
    public Map<String, Object> getPreventionPlanDetail(Long diseaseId) {
        Map<String, Object> detail = new HashMap<>();

        if (diseaseId == 1) {
            detail.put("name", "糖尿病");
            detail.put("riskLevel", "中风险");
            detail.put("reduction", 35);
            detail.put("cycle", 90);
            detail.put("progress", 45);

            Map<String, Object> dietPlan = new HashMap<>();
            dietPlan.put("recommended", Arrays.asList("燕麦", "糙米", "苦瓜", "西兰花", "鱼肉", "豆制品"));
            dietPlan.put("restricted", Arrays.asList("白米饭", "白面包", "含糖饮料", "甜点", "油炸食品"));
            dietPlan.put("tips", Arrays.asList("每日主食控制在250-300g", "每餐蔬菜不少于200g", "蛋白质选择优质来源"));
            detail.put("dietPlan", dietPlan);

            detail.put("exercisePlan", Arrays.asList(
                Map.of("name", "快走", "frequency", "每周5次", "description", "每次30-45分钟", "benefits", Arrays.asList("降低血糖", "改善胰岛素敏感性")),
                Map.of("name", "游泳", "frequency", "每周2次", "description", "每次30-60分钟", "benefits", Arrays.asList("全身运动", "保护关节"))
            ));

            detail.put("checkupPlan", Arrays.asList(
                Map.of("item", "空腹血糖", "frequency", "每3个月", "purpose", "监测血糖控制情况", "nextDate", "2024-04-15"),
                Map.of("item", "糖化血红蛋白", "frequency", "每3个月", "purpose", "评估长期血糖水平", "nextDate", "2024-04-15")
            ));
        }

        return detail;
    }
}
