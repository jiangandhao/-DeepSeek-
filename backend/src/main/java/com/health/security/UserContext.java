package com.health.security;

import org.springframework.security.core.context.SecurityContextHolder;

import com.health.common.BizException;

/** 从 Spring Security 上下文获取当前登录用户ID。 */
public final class UserContext {

    private UserContext() {}

    public static Long currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long id) {
            return id;
        }
        throw new BizException(401, "未登录");
    }
}
