package com.health.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.health.common.Result;
import com.health.dto.HealthProfileRequest;
import com.health.entity.HealthProfile;
import com.health.security.UserContext;
import com.health.service.HealthProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "健康档案")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class HealthProfileController {

    private final HealthProfileService service;

    @Operation(summary = "获取健康档案")
    @GetMapping
    public Result<HealthProfile> get() {
        return Result.ok(service.get(UserContext.currentUserId()));
    }

    @Operation(summary = "保存/更新健康档案")
    @PutMapping
    public Result<HealthProfile> upsert(@RequestBody HealthProfileRequest req) {
        return Result.ok(service.upsert(UserContext.currentUserId(), req));
    }
}
