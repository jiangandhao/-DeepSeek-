package com.health.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.health.common.BizException;
import com.health.dto.DietRecordRequest;
import com.health.entity.DietRecord;
import com.health.mapper.DietRecordMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DietService {

    private final DietRecordMapper mapper;

    public DietRecord add(Long userId, DietRecordRequest req) {
        DietRecord r = new DietRecord();
        r.setUserId(userId);
        r.setMealType(req.getMealType());
        r.setFood(req.getFood());
        r.setCalories(req.getCalories());
        r.setCarbsG(req.getCarbsG());
        r.setEatenAt(req.getEatenAt());
        mapper.insert(r);
        return r;
    }

    public List<DietRecord> list(Long userId, LocalDateTime from, LocalDateTime to) {
        return mapper.selectList(Wrappers.<DietRecord>lambdaQuery()
                .eq(DietRecord::getUserId, userId)
                .ge(from != null, DietRecord::getEatenAt, from)
                .le(to != null, DietRecord::getEatenAt, to)
                .orderByDesc(DietRecord::getEatenAt));
    }

    public List<DietRecord> recent(Long userId, int limit) {
        return mapper.selectList(Wrappers.<DietRecord>lambdaQuery()
                .eq(DietRecord::getUserId, userId)
                .orderByDesc(DietRecord::getEatenAt)
                .last("limit " + limit));
    }

    public void delete(Long userId, Long id) {
        DietRecord r = mapper.selectById(id);
        if (r == null || !r.getUserId().equals(userId)) {
            throw new BizException("记录不存在");
        }
        mapper.deleteById(id);
    }
}
