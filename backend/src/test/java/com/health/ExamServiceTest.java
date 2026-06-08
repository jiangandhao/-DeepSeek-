package com.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.health.dto.CenterRecommendation;
import com.health.entity.ExamCenter;
import com.health.mapper.AppointmentMapper;
import com.health.mapper.ExamCenterMapper;
import com.health.service.ExamService;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    ExamCenterMapper centerMapper;
    @Mock
    AppointmentMapper appointmentMapper;
    @InjectMocks
    ExamService examService;

    private ExamCenter center(long id, String name, double lat, double lng, int busy) {
        ExamCenter c = new ExamCenter();
        c.setId(id);
        c.setName(name);
        c.setAddress("addr");
        c.setLat(BigDecimal.valueOf(lat));
        c.setLng(BigDecimal.valueOf(lng));
        c.setBusyness(busy);
        return c;
    }

    @Test
    void nearestLeastBusyRanksFirst() {
        // 用户位于浦东 (31.2215, 121.5443)
        when(centerMapper.selectList(any())).thenReturn(List.of(
                center(1, "远且忙", 31.2304, 121.4737, 80),   // ~6.8km, busy 80
                center(2, "近且闲", 31.2215, 121.5443, 30)));  // 0km, busy 30

        List<CenterRecommendation> recs = examService.recommend(31.2215, 121.5443);

        assertEquals(2, recs.size());
        assertEquals("近且闲", recs.get(0).getName(), "更近更闲的应排第一");
        assertTrue(recs.get(0).getMatchScore() > recs.get(1).getMatchScore());
        assertEquals(0.0, recs.get(0).getDistanceKm(), 0.2, "同坐标距离约 0km");
    }

    @Test
    void distanceComputedReasonably() {
        when(centerMapper.selectList(any())).thenReturn(List.of(
                center(1, "A", 31.2304, 121.4737, 50)));
        // 从浦东到市中心约 6-8 km
        double d = examService.recommend(31.2215, 121.5443).get(0).getDistanceKm();
        assertTrue(d > 3 && d < 12, "距离应在合理范围,实际=" + d);
    }
}
