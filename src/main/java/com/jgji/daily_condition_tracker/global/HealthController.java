package com.jgji.daily_condition_tracker.global;

import com.jgji.daily_condition_tracker.domain.shared.presentation.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 서버 상태 확인을 위한 헬스 체크 컨트롤러
 * Swagger UI 테스트용 기본 API
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * 서버 상태 확인
     * 인증이 필요하지 않은 공개 API
     * 
     * @return 서버 상태 정보
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthData = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "service", "Daily Condition Tracker API 서버",
                "version", "v1.0.0"
        );
        
        return ResponseEntity.ok(ApiResponse.success(healthData));
    }

    /**
     * 서버 정보 확인 (인증 필요)
     * JWT 토큰 테스트용 API
     * 
     * @return 서버 상세 정보
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> serverInfo() {
        Map<String, Object> serverInfo = Map.of(
                "application", "Daily Condition Tracker API 서버",
                "version", "v1.0.0",
                "environment", "development",
                "timestamp", LocalDateTime.now(),
                "description", "Daily Condition Tracker 서비스"
        );
        
        return ResponseEntity.ok(ApiResponse.success(serverInfo));
    }
} 