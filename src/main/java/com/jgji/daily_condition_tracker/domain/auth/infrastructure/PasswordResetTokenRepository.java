package com.jgji.daily_condition_tracker.domain.auth.infrastructure;

import com.jgji.daily_condition_tracker.domain.auth.domain.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository {
    
    PasswordResetToken save(PasswordResetToken passwordResetTokenEntity);
    
    Optional<PasswordResetToken> findByToken(String token);
}