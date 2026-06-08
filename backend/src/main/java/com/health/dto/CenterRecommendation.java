package com.health.dto;

import com.health.entity.ExamCenter;

import lombok.Data;

/** 体检中心推荐结果:在中心基础信息上附带距离与匹配分。 */
@Data
public class CenterRecommendation {
    private Long id;
    private String name;
    private String address;
    private Integer busyness;
    private String packages;
    private Double distanceKm;
    private Integer matchScore; // 0-100,越高越推荐

    public static CenterRecommendation of(ExamCenter c, double distanceKm, int matchScore) {
        CenterRecommendation r = new CenterRecommendation();
        r.id = c.getId();
        r.name = c.getName();
        r.address = c.getAddress();
        r.busyness = c.getBusyness();
        r.packages = c.getPackages();
        r.distanceKm = Math.round(distanceKm * 10) / 10.0;
        r.matchScore = matchScore;
        return r;
    }
}
