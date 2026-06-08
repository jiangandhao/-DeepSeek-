package com.health.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.health.common.AesUtil;
import com.health.common.Result;
import com.health.dto.UpdateUserRequest;
import com.health.entity.User;
import com.health.mapper.UserMapper;
import com.health.security.UserContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "账户")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;

    @Operation(summary = "获取当前用户信息(手机号脱敏展示)")
    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        User u = userMapper.selectById(UserContext.currentUserId());
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("nickname", u.getNickname());
        m.put("gender", u.getGender());
        // phone 经 TypeHandler 解密后再脱敏展示,原文不返回前端
        m.put("phoneMasked", AesUtil.maskPhone(u.getPhone()));
        m.put("hasPhone", u.getPhone() != null && !u.getPhone().isEmpty());
        return Result.ok(m);
    }

    @Operation(summary = "更新账户信息(手机号加密存储)")
    @PutMapping("/me")
    public Result<Void> update(@RequestBody UpdateUserRequest req) {
        User u = userMapper.selectById(UserContext.currentUserId());
        if (req.getNickname() != null) u.setNickname(req.getNickname());
        if (req.getGender() != null) u.setGender(req.getGender());
        if (req.getPhone() != null) u.setPhone(req.getPhone()); // TypeHandler 自动加密
        userMapper.updateById(u);
        return Result.ok();
    }
}
