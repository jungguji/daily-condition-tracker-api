package com.jgji.daily_condition_tracker.domain.auth.domain;

import com.jgji.daily_condition_tracker.domain.auth.infrastructure.PasswordResetTokenEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public final class PasswordResetToken {

    private final Long passwordResetTokenId;
    private final String token;
    private final long userId;
    private final boolean isUsed;
    private final OffsetDateTime expiryDate;

    // 토큰 유효 시간 (분 단위)
    private static final int TOKEN_EXPIRATION_TIME_IN_MINUTES = 60;

    @Builder(access = AccessLevel.PRIVATE)
    private PasswordResetToken(Long passwordResetTokenId, String token, long userId, boolean isUsed, OffsetDateTime expiryDate) {
        this.passwordResetTokenId = passwordResetTokenId;
        this.token = token;
        this.userId = userId;
        this.isUsed = isUsed;
        this.expiryDate = expiryDate;
    }

    public static PasswordResetToken create(long userId) {
        return PasswordResetToken.builder()
                .userId(userId)
                .token(generateToken())
                .isUsed(false)
                .expiryDate(calculateExpiryDate())
                .build();
    }

    public static PasswordResetToken ofEntity(PasswordResetTokenEntity passwordResetTokenEntity) {
        return PasswordResetToken.builder()
                .passwordResetTokenId(passwordResetTokenEntity.getPasswordResetTokenId())
                .token(passwordResetTokenEntity.getToken())
                .userId(passwordResetTokenEntity.getUserId())
                .isUsed(passwordResetTokenEntity.isUsed())
                .expiryDate(passwordResetTokenEntity.getExpiryDate())
                .build();
    }

    public PasswordResetToken markAsUsed() {
        return PasswordResetToken.builder()
                .passwordResetTokenId(this.passwordResetTokenId)
                .token(this.token)
                .userId(this.userId)
                .isUsed(true)
                .expiryDate(this.expiryDate)
                .build();
    }

    public boolean isValid() {
        return !isUsed && !isExpired();
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiryDate);
    }

    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    private static OffsetDateTime calculateExpiryDate() {
        return OffsetDateTime.now().plusMinutes(TOKEN_EXPIRATION_TIME_IN_MINUTES);
    }
}
