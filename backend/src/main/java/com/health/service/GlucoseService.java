package com.health.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.health.common.BizException;
import com.health.dto.GlucoseRecordRequest;
import com.health.entity.GlucoseRecord;
import com.health.mapper.GlucoseRecordMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GlucoseService {

    private final GlucoseRecordMapper mapper;

    public GlucoseRecord add(Long userId, GlucoseRecordRequest req) {
        GlucoseRecord r = new GlucoseRecord();
        r.setUserId(userId);
        r.setValueMmol(req.getValueMmol());
        r.setPeriod(req.getPeriod());
        r.setMeasuredAt(req.getMeasuredAt());
        r.setNote(req.getNote());
        mapper.insert(r);
        return r;
    }

    /** 查询用户血糖记录,可选时间范围,按测量时间升序。 */
    public List<GlucoseRecord> list(Long userId, LocalDateTime from, LocalDateTime to) {
        return mapper.selectList(Wrappers.<GlucoseRecord>lambdaQuery()
                .eq(GlucoseRecord::getUserId, userId)
                .ge(from != null, GlucoseRecord::getMeasuredAt, from)
                .le(to != null, GlucoseRecord::getMeasuredAt, to)
                .orderByAsc(GlucoseRecord::getMeasuredAt));
    }

    /** 最近 N 条记录(按测量时间倒序取,再正序返回)。 */
    public List<GlucoseRecord> recent(Long userId, int limit) {
        List<GlucoseRecord> desc = mapper.selectList(Wrappers.<GlucoseRecord>lambdaQuery()
                .eq(GlucoseRecord::getUserId, userId)
                .orderByDesc(GlucoseRecord::getMeasuredAt)
                .last("limit " + limit));
        java.util.Collections.reverse(desc);
        return desc;
    }

    public void delete(Long userId, Long id) {
        GlucoseRecord r = mapper.selectById(id);
        if (r == null || !r.getUserId().equals(userId)) {
            throw new BizException("记录不存在");
        }
        mapper.deleteById(id);
    }
}
