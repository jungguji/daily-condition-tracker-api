package com.jgji.daily_condition_tracker.domain.auth.infrastructure;

import com.jgji.daily_condition_tracker.domain.auth.domain.PasswordResetToken;
import com.jgji.daily_condition_tracker.domain.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT unsigned")
    private Long passwordResetTokenId;

    @Comment("비밀번호 재설정 토큰")
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Comment("사용자 ID")
    @Column(name = "user_id", columnDefinition = "BIGINT unsigned", nullable = false)
    private long userId;

    @Comment("사용 여부")
    @Column(name = "is_used", columnDefinition = "default 0", nullable = false)
    private boolean isUsed;

    @Comment("토큰 만료 날짜")
    @Column(name = "expiry_date", nullable = false)
    private OffsetDateTime expiryDate;

    @Builder(access = AccessLevel.PRIVATE)
    private PasswordResetTokenEntity(Long passwordResetTokenId, String token, long userId, boolean isUsed, OffsetDateTime expiryDate) {
        this.passwordResetTokenId = passwordResetTokenId;
        this.token = token;
        this.userId = userId;
        this.isUsed = isUsed;
        this.expiryDate = expiryDate;
    }

    protected PasswordResetTokenEntity create(String token, long userId, OffsetDateTime expiryDate) {
        return PasswordResetTokenEntity.builder()
            .token(token)
            .userId(userId)
            .isUsed(false)
            .expiryDate(expiryDate)
            .build();
    }

    protected static PasswordResetTokenEntity fromDomain(PasswordResetToken passwordResetToken) {
        return PasswordResetTokenEntity.builder()
            .passwordResetTokenId(passwordResetToken.getPasswordResetTokenId())
            .token(passwordResetToken.getToken())
            .userId(passwordResetToken.getUserId())
            .isUsed(passwordResetToken.isUsed())
            .expiryDate(passwordResetToken.getExpiryDate())
            .build();
    }

    protected PasswordResetToken toDomain() {
        return PasswordResetToken.ofEntity(this);
    }
} 