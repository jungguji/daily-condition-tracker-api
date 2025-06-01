package com.jgji.daily_condition_tracker.domain.auth.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    
    Optional<PasswordResetTokenEntity> findByToken(String token);
    
    Optional<PasswordResetTokenEntity> findByTokenAndIsUsedFalseAndExpiryDateAfter(String token, OffsetDateTime now);
    
    void deleteByExpiryDateBefore(OffsetDateTime expiryDate);
} 