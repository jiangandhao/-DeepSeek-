package com.health.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.health.dto.HealthProfileRequest;
import com.health.entity.HealthProfile;
import com.health.mapper.HealthProfileMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HealthProfileService {

    private final HealthProfileMapper mapper;

    public HealthProfile get(Long userId) {
        return mapper.selectOne(Wrappers.<HealthProfile>lambdaQuery()
                .eq(HealthProfile::getUserId, userId));
    }

    /** 不存在则插入,存在则更新(upsert)。 */
    public HealthProfile upsert(Long userId, HealthProfileRequest req) {
        HealthProfile profile = get(userId);
        if (profile == null) {
            profile = new HealthProfile();
            profile.setUserId(userId);
        }
        profile.setHeightCm(req.getHeightCm());
        profile.setWeightKg(req.getWeightKg());
        profile.setFamilyHistory(req.getFamilyHistory());
        profile.setChronic(req.getChronic());
        profile.setDiabetesType(req.getDiabetesType());
        if (profile.getId() == null) {
            mapper.insert(profile);
        } else {
            mapper.updateById(profile);
        }
        return profile;
    }
}
