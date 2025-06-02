package com.jgji.daily_condition_tracker.global.exception;

/**
 * 유효하지 않은 JWT 토큰 관련 예외
 * HTTP 401 Unauthorized 상태 코드와 매핑됨
 */
public class InvalidTokenException extends InvalidCredentialsException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
} 