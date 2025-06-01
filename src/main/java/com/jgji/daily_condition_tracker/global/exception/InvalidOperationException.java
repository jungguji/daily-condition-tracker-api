package com.jgji.daily_condition_tracker.global.exception;

/**
 * 잘못된 요청이나 작업 시 발생하는 예외
 * HTTP 400 Bad Request 상태 코드와 매핑됨
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
} 