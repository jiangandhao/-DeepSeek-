package com.health.controller;

import com.health.common.Result;
import com.health.entity.ExerciseRecord;
import com.health.mapper.ExerciseRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exercise")
@CrossOrigin
public class ExerciseRecordController {

    @Autowired
    private ExerciseRecordMapper exerciseRecordMapper;

    @GetMapping("/records")
    public Result<List<ExerciseRecord>> getRecords(@RequestParam Long userId) {
        List<ExerciseRecord> records = exerciseRecordMapper.selectByUserId(userId);
        return Result.success(records);
    }

    @PostMapping("/records")
    public Result<Map<String, Object>> addRecord(@RequestBody ExerciseRecord record) {
        exerciseRecordMapper.insert(record);
        Map<String, Object> data = new HashMap<>();
        data.put("id", record.getId());
        return Result.success(data);
    }

    @PutMapping("/records/{id}")
    public Result<Void> updateRecord(@PathVariable Long id, @RequestBody ExerciseRecord record) {
        record.setId(id);
        exerciseRecordMapper.updateById(record);
        return Result.success();
    }

    @DeleteMapping("/records/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        exerciseRecordMapper.deleteById(id);
        return Result.success();
    }
}
