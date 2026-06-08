package com.health.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.health.common.Result;
import com.health.dto.GlucoseRecordRequest;
import com.health.entity.GlucoseRecord;
import com.health.security.UserContext;
import com.health.service.GlucoseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "血糖记录")
@RestController
@RequestMapping("/api/glucose")
@RequiredArgsConstructor
public class GlucoseController {

    private final GlucoseService service;

    @Operation(summary = "新增血糖记录")
    @PostMapping
    public Result<GlucoseRecord> add(@Valid @RequestBody GlucoseRecordRequest req) {
        return Result.ok(service.add(UserContext.currentUserId(), req));
    }

    @Operation(summary = "查询血糖记录(可选时间范围)")
    @GetMapping
    public Result<List<GlucoseRecord>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return Result.ok(service.list(UserContext.currentUserId(), from, to));
    }

    @Operation(summary = "删除血糖记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(UserContext.currentUserId(), id);
        return Result.ok();
    }
}
