package com.health.controller;

import com.health.common.Result;
import com.health.entity.BloodPressureRecord;
import com.health.service.BloodPressureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blood-pressure")
public class BloodPressureController {

    @Autowired
    private BloodPressureService bloodPressureService;

    /**
     * 获取血压记录列表
     */
    @GetMapping("/records")
    public Result<List<BloodPressureRecord>> getRecords(@RequestParam Long userId) {
        return Result.success(bloodPressureService.getRecords(userId));
    }

    /**
     * 获取血压趋势数据
     */
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "7d") String period) {
        return Result.success(bloodPressureService.getTrend(userId, period));
    }

    /**
     * 添加血压记录
     */
    @PostMapping("/records")
    public Result<BloodPressureRecord> addRecord(@RequestBody BloodPressureRecord record) {
        return Result.success(bloodPressureService.addRecord(record));
    }

    /**
     * 更新血压记录
     */
    @PutMapping("/records/{id}")
    public Result<BloodPressureRecord> updateRecord(@PathVariable Long id, @RequestBody BloodPressureRecord record) {
        record.setId(id);
        return Result.success(bloodPressureService.updateRecord(record));
    }

    /**
     * 删除血压记录
     */
    @DeleteMapping("/records/{id}")
    public Result<Boolean> deleteRecord(@PathVariable Long id) {
        return Result.success(bloodPressureService.deleteRecord(id));
    }
}
