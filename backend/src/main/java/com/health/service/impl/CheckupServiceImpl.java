package com.health.service.impl;

import com.health.entity.CheckupReport;
import com.health.entity.CheckupAppointment;
import com.health.service.CheckupService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 智能体检服务实现
 */
@Service
public class CheckupServiceImpl implements CheckupService {

    @Override
    public List<Map<String, Object>> searchCenters(Map<String, Object> params) {
        List<Map<String, Object>> centers = new ArrayList<>();

        Map<String, Object> center1 = new HashMap<>();
        center1.put("id", 1);
        center1.put("name", "美年大健康体检中心");
        center1.put("address", "武汉市武昌区中南路1号");
        center1.put("rating", 4.5);
        center1.put("reviewCount", 1256);
        center1.put("minPrice", 299);
        center1.put("distance", 1.2);
        center1.put("available", true);
        center1.put("tags", Arrays.asList("三甲合作", "周末可约", "报告快速"));
        centers.add(center1);

        Map<String, Object> center2 = new HashMap<>();
        center2.put("id", 2);
        center2.put("name", "慈铭体检中心");
        center2.put("address", "武汉市江汉区建设大道568号");
        center2.put("rating", 4.3);
        center2.put("reviewCount", 892);
        center2.put("minPrice", 399);
        center2.put("distance", 2.5);
        center2.put("available", true);
        center2.put("tags", Arrays.asList("环境优雅", "设备先进", "专家团队"));
        centers.add(center2);

        Map<String, Object> center3 = new HashMap<>();
        center3.put("id", 3);
        center3.put("name", "爱康国宾体检中心");
        center3.put("address", "武汉市洪山区珞瑜路10号");
        center3.put("rating", 4.6);
        center3.put("reviewCount", 1580);
        center3.put("minPrice", 349);
        center3.put("distance", 3.8);
        center3.put("available", false);
        center3.put("tags", Arrays.asList("口碑好评", "套餐丰富", "免费早餐"));
        centers.add(center3);

        return centers;
    }

    @Override
    public CheckupAppointment createAppointment(Long userId, Map<String, Object> params) {
        CheckupAppointment appointment = new CheckupAppointment();
        appointment.setId(System.currentTimeMillis());
        appointment.setUserId(userId);
        appointment.setCenterId(Long.valueOf(params.get("centerId").toString()));
        appointment.setAppointmentDate(params.get("date").toString());
        appointment.setPackageType(params.get("package").toString());
        appointment.setPhone(params.get("phone").toString());
        appointment.setStatus("已预约");
        appointment.setCreateTime(new Date());
        return appointment;
    }

    @Override
    public List<CheckupAppointment> getAppointments(Long userId, Map<String, Object> params) {
        return new ArrayList<>();
    }

    @Override
    public void cancelAppointment(Long id) {
        // TODO: 实现取消预约逻辑
    }

    @Override
    public List<CheckupReport> getReports(Long userId, Map<String, Object> params) {
        List<CheckupReport> reports = new ArrayList<>();

        CheckupReport report1 = new CheckupReport();
        report1.setId(1L);
        report1.setUserId(userId);
        report1.setReportDate("2024-01-15");
        report1.setReportType("年度体检");
        report1.setStatus("已解读");
        report1.setScore(82);
        report1.setAbnormalCount(3);
        report1.setNormalCount(42);
        reports.add(report1);

        return reports;
    }

    @Override
    public CheckupReport getReportDetail(Long id) {
        CheckupReport report = new CheckupReport();
        report.setId(id);
        report.setReportDate("2024-01-15");
        report.setReportType("年度体检");
        report.setStatus("已解读");
        report.setScore(82);
        report.setAbnormalCount(3);
        report.setNormalCount(42);
        report.setReviewCount(1);
        return report;
    }

    @Override
    public Map<String, Object> analyzeImage(Long userId, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("imageName", params.get("imageType") + "_analysis.jpg");
        result.put("riskLevel", "低风险");
        result.put("findings", Arrays.asList(
            Map.of("location", "右肺上叶", "description", "肺野透亮度正常，未见明显异常密度影", "significance", "正常"),
            Map.of("location", "心脏", "description", "心影大小形态正常", "significance", "正常", "measurement", "心胸比: 0.48")
        ));
        result.put("suggestion", Map.of(
            "title", "检查结果正常",
            "urgent", false,
            "content", "您的检查未发现明显异常，建议保持良好的生活习惯，定期体检。"
        ));
        return result;
    }

    @Override
    public List<Map<String, Object>> getAnalysisHistory(Long userId, Map<String, Object> params) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getPackages(Map<String, Object> params) {
        List<Map<String, Object>> packages = new ArrayList<>();

        Map<String, Object> basic = new HashMap<>();
        basic.put("id", "basic");
        basic.put("name", "基础套餐");
        basic.put("price", 299);
        basic.put("items", Arrays.asList("血常规", "尿常规", "肝功能", "肾功能", "血糖", "血脂"));
        packages.add(basic);

        Map<String, Object> standard = new HashMap<>();
        standard.put("id", "standard");
        standard.put("name", "标准套餐");
        standard.put("price", 599);
        standard.put("items", Arrays.asList("血常规", "尿常规", "肝功能", "肾功能", "血糖", "血脂", "心电图", "B超", "胸透"));
        packages.add(standard);

        Map<String, Object> advanced = new HashMap<>();
        advanced.put("id", "advanced");
        advanced.put("name", "高级套餐");
        advanced.put("price", 999);
        advanced.put("items", Arrays.asList("血常规", "尿常规", "肝功能", "肾功能", "血糖", "血脂", "心电图", "B超", "胸透", "CT", "肿瘤标志物", "甲状腺功能"));
        packages.add(advanced);

        return packages;
    }
}
