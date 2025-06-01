package com.jgji.daily_condition_tracker.global.exception;

/**
 * 비즈니스 규칙 위반 시 발생하는 예외
 * HTTP 400 Bad Request 상태 코드와 매핑됨
 */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }

    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
} 