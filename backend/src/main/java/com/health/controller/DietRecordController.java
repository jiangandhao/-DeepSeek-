package com.health.controller;

import com.health.common.Result;
import com.health.entity.DietRecord;
import com.health.mapper.DietRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diet")
@CrossOrigin
public class DietRecordController {

    @Autowired
    private DietRecordMapper dietRecordMapper;

    @GetMapping("/records")
    public Result<List<DietRecord>> getRecords(@RequestParam Long userId) {
        List<DietRecord> records = dietRecordMapper.selectByUserId(userId);
        return Result.success(records);
    }

    @PostMapping("/records")
    public Result<Map<String, Object>> addRecord(@RequestBody DietRecord record) {
        dietRecordMapper.insert(record);
        Map<String, Object> data = new HashMap<>();
        data.put("id", record.getId());
        return Result.success(data);
    }

    @PutMapping("/records/{id}")
    public Result<Void> updateRecord(@PathVariable Long id, @RequestBody DietRecord record) {
        record.setId(id);
        dietRecordMapper.updateById(record);
        return Result.success();
    }

    @DeleteMapping("/records/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        dietRecordMapper.deleteById(id);
        return Result.success();
    }
}
