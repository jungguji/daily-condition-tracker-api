package com.jgji.daily_condition_tracker.global.exception;

import com.jgji.daily_condition_tracker.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 * 모든 컨트롤러에서 발생하는 예외를 일관된 형태로 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 리소스를 찾을 수 없는 경우 (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("리소스를 찾을 수 없음: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(404, ex.getMessage()));
    }

    /**
     * 비즈니스 규칙 위반 예외 (400)
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRuleViolation(BusinessRuleViolationException ex) {
        log.warn("비즈니스 규칙 위반: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(400, ex.getMessage()));
    }

    /**
     * 잘못된 요청 예외 (400)
     */
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidOperation(InvalidOperationException ex) {
        log.warn("잘못된 요청: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(400, ex.getMessage()));
    }

    /**
     * 유효성 검증 실패 (400)
     * @Valid 어노테이션으로 인한 검증 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("유효성 검증 실패: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .status("VALIDATION_ERROR")
                        .message("입력 데이터 검증에 실패했습니다.")
                        .data(errors)
                        .build());
    }

    /**
     * 중복 리소스 예외 (409)
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(DuplicateResourceException ex) {
        log.warn("중복 리소스: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(409, ex.getMessage()));
    }

    /**
     * 인증 관련 예외 (401)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        log.warn("인증 실패: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(401, ex.getMessage()));
    }

    /**
     * 권한 관련 예외 (403)
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorization(AuthorizationException ex) {
        log.warn("권한 없음: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(403, ex.getMessage()));
    }

    /**
     * 유효하지 않은 토큰 예외 (401)
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException ex) {
        log.warn("유효하지 않은 토큰: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(401, ex.getMessage()));
    }

    /**
     * 일반적인 예외 처리 (500)
     * 예상하지 못한 모든 예외를 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("예상하지 못한 오류 발생", ex);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(500, "서버 내부 오류가 발생했습니다."));
    }

    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotActive(UserNotActiveException ex) {
        log.warn("비활성화된 사용자: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(403, ex.getMessage()));
    }

    @ExceptionHandler(UserDeletedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserDeleted(UserDeletedException ex) {
        log.warn("삭제된 사용자: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(403, ex.getMessage()));
    }

    /**
     * IllegalArgumentException (400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("잘못된 인수 오류 발생", ex);
        log.warn("잘못된 인수: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(400, "잘못된 요청 파라미터입니다."));
    }

    /**
     * 잘못된 인증 정보 예외 (401)
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.error("{}", ex);
        log.warn("잘못된 인증 정보: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(401, ex.getMessage()));
    }
} 