package com.jgji.daily_condition_tracker.global.exception;

/**
 * 권한 없음 시 발생하는 예외
 * HTTP 403 Forbidden 상태 코드와 매핑됨
 */
public class AuthorizationException extends RuntimeException {

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
} 