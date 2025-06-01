package com.jgji.daily_condition_tracker.domain.auth.infrastructure;

import com.jgji.daily_condition_tracker.domain.auth.domain.PasswordResetToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {
    
    private final PasswordResetTokenJpaRepository passwordResetTokenJpaRepository;
    
    @Override
    public PasswordResetToken save(PasswordResetToken passwordResetToken) {
        PasswordResetTokenEntity entity = PasswordResetTokenEntity.fromDomain(passwordResetToken);
        PasswordResetTokenEntity saveEntity = passwordResetTokenJpaRepository.save(entity);
        return saveEntity.toDomain();
    }
    
    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return passwordResetTokenJpaRepository.findByToken(token).map(PasswordResetToken::ofEntity);
    }
    
    @Override
    public Optional<PasswordResetToken> findByTokenAndIsUsedFalseAndExpiryDateAfter(String token, OffsetDateTime now) {
        return passwordResetTokenJpaRepository.findByTokenAndIsUsedFalseAndExpiryDateAfter(token, now)
                .map(PasswordResetToken::ofEntity);
    }
    
    @Override
    public void deleteByExpiryDateBefore(OffsetDateTime expiryDate) {
        passwordResetTokenJpaRepository.deleteByExpiryDateBefore(expiryDate);
    }
} 