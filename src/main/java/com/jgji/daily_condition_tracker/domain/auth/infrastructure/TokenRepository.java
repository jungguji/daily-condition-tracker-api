package com.jgji.daily_condition_tracker.domain.auth.infrastructure;

/**
 * JWT 토큰 블랙리스트 관리 저장소
 * 현재는 메모리 저장소를 사용하고, 추후 Redis로 확장 가능
 */
public interface TokenRepository {
    
    void addToBlacklist(String token);
    
    boolean isBlacklisted(String token);
}
