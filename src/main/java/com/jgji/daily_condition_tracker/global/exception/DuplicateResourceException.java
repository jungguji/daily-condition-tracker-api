package com.jgji.daily_condition_tracker.global.exception;

/**
 * 중복된 리소스 생성 시도 시 발생하는 예외
 * HTTP 409 Conflict 상태 코드와 매핑됨
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("이미 존재하는 %s입니다. %s: %s", resourceName, fieldName, fieldValue));
    }
} 