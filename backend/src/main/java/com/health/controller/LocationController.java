package com.health.controller;

import com.health.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/location")
@CrossOrigin
public class LocationController {

    /**
     * 获取附近医院列表
     * @param lat 纬度
     * @param lng 经度
     * @param radius 搜索半径(公里)，默认5km
     */
    @GetMapping("/nearby-hospitals")
    public Result<List<Map<String, Object>>> getNearbyHospitals(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "5") Double radius) {

        List<Map<String, Object>> hospitals = new ArrayList<>();

        // 模拟附近医院数据（实际项目中可接入高德/百度地图API）
        hospitals.add(createHospital(1L, "美年大健康(光谷店)", "武汉市洪山区光谷大道77号",
                30.5065, 114.4215, 4.5, "2.3km", 15, "027-87654321"));
        hospitals.add(createHospital(2L, "爱康国宾(武昌店)", "武汉市武昌区中南路99号",
                30.5465, 114.3415, 4.3, "5.1km", 25, "027-87654322"));
        hospitals.add(createHospital(3L, "瑞慈体检(汉口店)", "武汉市江汉区解放大道688号",
                30.5865, 114.2815, 4.7, "8.5km", 45, "027-87654323"));
        hospitals.add(createHospital(4L, "协和医院体检中心", "武汉市江汉区解放大道1277号",
                30.5765, 114.2915, 4.8, "6.2km", 30, "027-85726114"));
        hospitals.add(createHospital(5L, "武汉大学人民医院", "武汉市武昌区解放路238号",
                30.5365, 114.3215, 4.6, "4.8km", 20, "027-88041911"));
        hospitals.add(createHospital(6L, "同济医院体检中心", "武汉市硚口区解放大道1095号",
                30.5965, 114.2615, 4.9, "9.3km", 35, "027-83662688"));
        hospitals.add(createHospital(7L, "湖北省人民医院", "武汉市武昌区张之洞路99号",
                30.5265, 114.3315, 4.4, "3.7km", 18, "027-88041911"));
        hospitals.add(createHospital(8L, "武汉市中心医院", "武汉市江岸区胜利街26号",
                30.5665, 114.3015, 4.2, "7.1km", 22, "027-82811080"));

        // 按距离排序
        hospitals.sort((a, b) -> {
            double distA = calculateDistance(lat, lng, (Double) a.get("lat"), (Double) a.get("lng"));
            double distB = calculateDistance(lat, lng, (Double) b.get("lat"), (Double) b.get("lng"));
            return Double.compare(distA, distB);
        });

        return Result.success(hospitals);
    }

    /**
     * 计算两点之间的距离（公里）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371; // 地球半径（公里）
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Map<String, Object> createHospital(Long id, String name, String address,
                                               Double lat, Double lng, Double rating,
                                               String distance, int waitTime, String phone) {
        Map<String, Object> hospital = new HashMap<>();
        hospital.put("id", id);
        hospital.put("name", name);
        hospital.put("address", address);
        hospital.put("lat", lat);
        hospital.put("lng", lng);
        hospital.put("rating", rating);
        hospital.put("distance", distance);
        hospital.put("waitTime", waitTime);
        hospital.put("phone", phone);
        return hospital;
    }
}
