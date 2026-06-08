package com.health.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String nickname;
    private String phone;
    private Integer gender;
}
