package com.health.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.health.common.BizException;
import com.health.dto.AppointmentRequest;
import com.health.dto.CenterRecommendation;
import com.health.entity.Appointment;
import com.health.entity.ExamCenter;
import com.health.mapper.AppointmentMapper;
import com.health.mapper.ExamCenterMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    // 匹配分权重:距离 60%、繁忙度 40%
    private static final double W_DISTANCE = 0.6;
    private static final double W_BUSYNESS = 0.4;
    private static final double MAX_DISTANCE_KM = 30.0; // 距离归一化上限

    private final ExamCenterMapper centerMapper;
    private final AppointmentMapper appointmentMapper;

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /**
     * 智能推荐:综合「地理位置距离 + 机构繁忙度」打分排序。
     * 匹配分 = (1 - 0.6*距离归一 - 0.4*繁忙度归一) * 100,越高越推荐。
     */
    public List<CenterRecommendation> recommend(double lat, double lng) {
        List<ExamCenter> centers = centerMapper.selectList(null);
        return centers.stream().map(c -> {
            double dist = haversine(lat, lng, c.getLat().doubleValue(), c.getLng().doubleValue());
            double distNorm = Math.min(dist / MAX_DISTANCE_KM, 1.0);
            double busyNorm = (c.getBusyness() == null ? 50 : c.getBusyness()) / 100.0;
            double cost = W_DISTANCE * distNorm + W_BUSYNESS * busyNorm;
            int matchScore = (int) Math.round((1 - cost) * 100);
            return CenterRecommendation.of(c, dist, matchScore);
        }).sorted(Comparator.comparingInt(CenterRecommendation::getMatchScore).reversed()).toList();
    }

    public Appointment book(Long userId, AppointmentRequest req) {
        ExamCenter center = centerMapper.selectById(req.getCenterId());
        if (center == null) {
            throw new BizException("体检中心不存在");
        }
        Appointment appt = new Appointment();
        appt.setUserId(userId);
        appt.setCenterId(center.getId());
        appt.setCenterName(center.getName());
        appt.setExamDate(req.getExamDate());
        appt.setPkg(req.getPkg());
        appt.setStatus("BOOKED");
        appointmentMapper.insert(appt);
        return appt;
    }

    public List<Appointment> myAppointments(Long userId) {
        return appointmentMapper.selectList(Wrappers.<Appointment>lambdaQuery()
                .eq(Appointment::getUserId, userId)
                .orderByDesc(Appointment::getCreatedAt));
    }

    public void cancel(Long userId, Long id) {
        Appointment a = appointmentMapper.selectById(id);
        if (a == null || !a.getUserId().equals(userId)) {
            throw new BizException("预约不存在");
        }
        a.setStatus("CANCELLED");
        appointmentMapper.updateById(a);
    }
}
