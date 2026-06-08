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
import com.health.dto.DietRecordRequest;
import com.health.entity.DietRecord;
import com.health.security.UserContext;
import com.health.service.DietService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "饮食记录")
@RestController
@RequestMapping("/api/diet")
@RequiredArgsConstructor
public class DietController {

    private final DietService service;

    @Operation(summary = "新增饮食记录")
    @PostMapping
    public Result<DietRecord> add(@Valid @RequestBody DietRecordRequest req) {
        return Result.ok(service.add(UserContext.currentUserId(), req));
    }

    @Operation(summary = "查询饮食记录")
    @GetMapping
    public Result<List<DietRecord>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return Result.ok(service.list(UserContext.currentUserId(), from, to));
    }

    @Operation(summary = "删除饮食记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(UserContext.currentUserId(), id);
        return Result.ok();
    }
}
