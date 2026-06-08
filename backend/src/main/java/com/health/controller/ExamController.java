package com.health.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.health.common.Result;
import com.health.dto.AppointmentRequest;
import com.health.dto.CenterRecommendation;
import com.health.entity.Appointment;
import com.health.security.UserContext;
import com.health.service.ExamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "智能体检")
@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService service;

    @Operation(summary = "智能推荐体检中心(经纬度 + 繁忙度匹配)")
    @GetMapping("/centers")
    public Result<List<CenterRecommendation>> centers(
            @RequestParam(defaultValue = "31.2304") double lat,
            @RequestParam(defaultValue = "121.4737") double lng) {
        return Result.ok(service.recommend(lat, lng));
    }

    @Operation(summary = "预约体检")
    @PostMapping("/appointments")
    public Result<Appointment> book(@Valid @RequestBody AppointmentRequest req) {
        return Result.ok(service.book(UserContext.currentUserId(), req));
    }

    @Operation(summary = "我的预约")
    @GetMapping("/appointments")
    public Result<List<Appointment>> myAppointments() {
        return Result.ok(service.myAppointments(UserContext.currentUserId()));
    }

    @Operation(summary = "取消预约")
    @DeleteMapping("/appointments/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        service.cancel(UserContext.currentUserId(), id);
        return Result.ok();
    }
}
