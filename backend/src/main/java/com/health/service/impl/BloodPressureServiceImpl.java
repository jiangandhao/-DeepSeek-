package com.health.service.impl;

import com.health.entity.BloodPressureRecord;
import com.health.mapper.BloodPressureRecordMapper;
import com.health.service.BloodPressureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BloodPressureServiceImpl implements BloodPressureService {

    @Autowired
    private BloodPressureRecordMapper bloodPressureRecordMapper;

    @Override
    public List<BloodPressureRecord> getRecords(Long userId) {
        return bloodPressureRecordMapper.selectByUserId(userId);
    }

    @Override
    public Map<String, Object> getTrend(Long userId, String period) {
        // 计算起始时间
        LocalDate today = LocalDate.now();
        LocalDate startDate;

        if ("7d".equals(period)) {
            startDate = today.minusDays(6);
        } else if ("30d".equals(period)) {
            startDate = today.minusDays(29);
        } else {
            startDate = today.minusDays(6);
        }

        String startTime = startDate.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 查询数据
        List<BloodPressureRecord> records = bloodPressureRecordMapper.selectByUserIdAndTimeRange(userId, startTime);

        // 按日期分组，计算每天的平均值
        Map<String, List<BloodPressureRecord>> groupedByDate = new LinkedHashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd");

        // 初始化日期列表
        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            groupedByDate.put(date.format(dateFormatter), new ArrayList<>());
        }

        // 分组数据
        for (BloodPressureRecord record : records) {
            if (record.getMeasureTime() != null) {
                String dateKey = record.getMeasureTime().toLocalDate().format(dateFormatter);
                if (groupedByDate.containsKey(dateKey)) {
                    groupedByDate.get(dateKey).add(record);
                }
            }
        }

        // 计算每天的平均值
        List<String> dates = new ArrayList<>();
        List<Integer> systolicData = new ArrayList<>();
        List<Integer> diastolicData = new ArrayList<>();
        List<Integer> heartRateData = new ArrayList<>();

        for (Map.Entry<String, List<BloodPressureRecord>> entry : groupedByDate.entrySet()) {
            dates.add(entry.getKey());
            List<BloodPressureRecord> dayRecords = entry.getValue();

            if (dayRecords.isEmpty()) {
                systolicData.add(0);
                diastolicData.add(0);
                heartRateData.add(0);
            } else {
                int avgSystolic = (int) dayRecords.stream()
                        .mapToInt(BloodPressureRecord::getSystolic)
                        .average()
                        .orElse(0);
                int avgDiastolic = (int) dayRecords.stream()
                        .mapToInt(BloodPressureRecord::getDiastolic)
                        .average()
                        .orElse(0);
                int avgHeartRate = (int) dayRecords.stream()
                        .filter(r -> r.getHeartRate() != null)
                        .mapToInt(BloodPressureRecord::getHeartRate)
                        .average()
                        .orElse(0);

                systolicData.add(avgSystolic);
                diastolicData.add(avgDiastolic);
                heartRateData.add(avgHeartRate);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("systolic", systolicData);
        result.put("diastolic", diastolicData);
        result.put("heartRate", heartRateData);

        return result;
    }

    @Override
    public BloodPressureRecord addRecord(BloodPressureRecord record) {
        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
        }
        if (record.getMeasureTime() == null) {
            record.setMeasureTime(LocalDateTime.now());
        }
        bloodPressureRecordMapper.insert(record);
        return record;
    }

    @Override
    public BloodPressureRecord updateRecord(BloodPressureRecord record) {
        bloodPressureRecordMapper.updateById(record);
        return record;
    }

    @Override
    public boolean deleteRecord(Long id) {
        return bloodPressureRecordMapper.deleteById(id) > 0;
    }
}
