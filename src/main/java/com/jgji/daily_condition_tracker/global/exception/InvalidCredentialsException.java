package com.jgji.daily_condition_tracker.global.exception;

/**
 * 잘못된 인증 정보 (이메일/비밀번호) 입력 시 발생하는 예외
 * HTTP 401 Unauthorized 상태 코드와 매핑됨
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
} 