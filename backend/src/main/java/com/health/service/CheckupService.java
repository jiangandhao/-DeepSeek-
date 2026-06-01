package com.health.service;

import com.health.entity.CheckupReport;
import com.health.entity.CheckupAppointment;
import java.util.List;
import java.util.Map;

/**
 * 智能体检服务接口
 */
public interface CheckupService {

    /**
     * 搜索体检中心
     */
    List<Map<String, Object>> searchCenters(Map<String, Object> params);

    /**
     * 创建预约
     */
    CheckupAppointment createAppointment(Long userId, Map<String, Object> params);

    /**
     * 获取预约列表
     */
    List<CheckupAppointment> getAppointments(Long userId, Map<String, Object> params);

    /**
     * 取消预约
     */
    void cancelAppointment(Long id);

    /**
     * 获取体检报告列表
     */
    List<CheckupReport> getReports(Long userId, Map<String, Object> params);

    /**
     * 获取报告详情
     */
    CheckupReport getReportDetail(Long id);

    /**
     * 影像分析
     */
    Map<String, Object> analyzeImage(Long userId, Map<String, Object> params);

    /**
     * 获取分析历史
     */
    List<Map<String, Object>> getAnalysisHistory(Long userId, Map<String, Object> params);

    /**
     * 获取套餐列表
     */
    List<Map<String, Object>> getPackages(Map<String, Object> params);
}
