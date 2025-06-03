package com.jgji.daily_condition_tracker.domain.auth.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Repository
class LocalTokenRepository {
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void addToBlacklist(String token) {
        blacklistedTokens.add(token);
        log.debug("토큰이 블랙리스트에 추가되었습니다. 현재 블랙리스트 크기: {}", blacklistedTokens.size());
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
