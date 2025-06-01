package com.jgji.daily_condition_tracker.domain.auth.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
class TokenRepositoryImpl implements TokenRepository {

    private final LocalTokenRepository localTokenRepository;

    @Override
    public void addToBlacklist(String token) {
        localTokenRepository.addToBlacklist(token);
    }
    
    @Override
    public boolean isBlacklisted(String token) {
        return localTokenRepository.isBlacklisted(token);
    }
}
