package com.health.dto;

import lombok.Data;

@Data
public class AiQuestionRequest {
    /** 用户提问;为空表示生成综合健康方案。 */
    private String question;
}
