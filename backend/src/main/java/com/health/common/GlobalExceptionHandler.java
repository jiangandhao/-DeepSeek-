package com.health.common;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.extern.slf4j.Slf4j;

/** 全局异常处理。 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = fe != null ? fe.getDefaultMessage() : "参数校验失败";
        return Result.error(400, msg);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public Result<Void> handleAiResponse(WebClientResponseException e) {
        // AI 服务返回的错误体形如 {"detail": "..."}
        String body = e.getResponseBodyAsString();
        String msg = body != null && body.contains("detail") ? body : "AI 服务调用失败";
        log.warn("AI 服务返回错误: {}", body);
        return Result.error(502, "AI 服务:" + msg);
    }

    @ExceptionHandler(WebClientRequestException.class)
    public Result<Void> handleAiConn(WebClientRequestException e) {
        log.error("AI 服务连接失败", e);
        return Result.error(502, "无法连接 AI 服务,请确认 ai-service 已启动");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("未处理异常", e);
        return Result.error(500, "服务器内部错误");
    }
}
