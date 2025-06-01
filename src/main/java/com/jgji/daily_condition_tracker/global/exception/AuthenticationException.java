package com.jgji.daily_condition_tracker.global.exception;

/**
 * 인증 실패 시 발생하는 예외
 * HTTP 401 Unauthorized 상태 코드와 매핑됨
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
} 