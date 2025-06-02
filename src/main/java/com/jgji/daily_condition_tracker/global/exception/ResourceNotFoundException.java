package com.jgji.daily_condition_tracker.global.exception;

/**
 * 요청한 리소스를 찾을 수 없을 때 발생하는 예외
 * HTTP 404 Not Found 상태 코드와 매핑됨
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s를 찾을 수 없습니다. %s: %s", resourceName, fieldName, fieldValue));
    }
}