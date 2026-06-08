package com.health.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = e.getClass().getName() + ": " + (e.getCause() != null ? e.getCause().getMessage() : "unknown error");
        }
        return Result.error(message);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = e.getClass().getName() + ": " + (e.getCause() != null ? e.getCause().getMessage() : "unknown error");
        }
        return Result.error(message);
    }
}
