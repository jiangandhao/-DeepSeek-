package com.health.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.health.common.BizException;
import com.health.dto.ExerciseRecordRequest;
import com.health.entity.ExerciseRecord;
import com.health.mapper.ExerciseRecordMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRecordMapper mapper;

    public ExerciseRecord add(Long userId, ExerciseRecordRequest req) {
        ExerciseRecord r = new ExerciseRecord();
        r.setUserId(userId);
        r.setType(req.getType());
        r.setDurationMin(req.getDurationMin());
        r.setIntensity(req.getIntensity());
        r.setCalories(req.getCalories());
        r.setDoneAt(req.getDoneAt());
        mapper.insert(r);
        return r;
    }

    public List<ExerciseRecord> list(Long userId, LocalDateTime from, LocalDateTime to) {
        return mapper.selectList(Wrappers.<ExerciseRecord>lambdaQuery()
                .eq(ExerciseRecord::getUserId, userId)
                .ge(from != null, ExerciseRecord::getDoneAt, from)
                .le(to != null, ExerciseRecord::getDoneAt, to)
                .orderByDesc(ExerciseRecord::getDoneAt));
    }

    public List<ExerciseRecord> recent(Long userId, int limit) {
        return mapper.selectList(Wrappers.<ExerciseRecord>lambdaQuery()
                .eq(ExerciseRecord::getUserId, userId)
                .orderByDesc(ExerciseRecord::getDoneAt)
                .last("limit " + limit));
    }

    public void delete(Long userId, Long id) {
        ExerciseRecord r = mapper.selectById(id);
        if (r == null || !r.getUserId().equals(userId)) {
            throw new BizException("记录不存在");
        }
        mapper.deleteById(id);
    }
}
