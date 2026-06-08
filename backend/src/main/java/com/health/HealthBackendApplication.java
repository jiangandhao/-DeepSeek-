package com.health;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.health.mapper")
public class HealthBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealthBackendApplication.class, args);
    }
}
