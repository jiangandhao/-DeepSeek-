package com.health.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.health.common.AesUtil;

import jakarta.annotation.PostConstruct;

/** 启动时用配置密钥初始化字段加密工具。 */
@Configuration
public class CryptoConfig {

    @Value("${app.crypto.secret:${app.jwt.secret}}")
    private String secret;

    @PostConstruct
    public void init() {
        AesUtil.init(secret);
    }
}
