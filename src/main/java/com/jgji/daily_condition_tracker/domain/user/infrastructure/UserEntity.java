package com.jgji.daily_condition_tracker.domain.user.infrastructure;

import com.jgji.daily_condition_tracker.domain.user.domain.User;
import com.jgji.daily_condition_tracker.domain.shared.domain.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class UserEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Comment("이메일 주소 (로그인 ID)")
    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Comment("해시된 비밀번호")
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Comment("소셜 로그인 제공자 (e.g., google, kakao)")
    @Column(name = "social_provider", length = 50)
    private String socialProvider;

    @Comment("소셜 로그인 고유 ID")
    @Column(name = "social_id", length = 255)
    private String socialId;

    @Comment("닉네임 (커뮤니티 기능 대비)")
    @Column(name = "nickname", length = 50)
    private String nickname;

    @Comment("계정 활성화 여부")
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Comment("계정 소프트 삭제 여부")
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Comment("관리자 여부")
    @Column(name = "is_superuser", nullable = false)
    private boolean isSuperuser;

    @Comment("계정 인증 여부")
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Builder(access = AccessLevel.PRIVATE)
    private UserEntity(Long userId, String email, String passwordHash, String socialProvider, String socialId,
                       String nickname, boolean isActive, boolean isDeleted, boolean isSuperuser, boolean isVerified) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        this.nickname = nickname;
        this.isActive = isActive;
        this.isDeleted = isDeleted;
        this.isSuperuser = isSuperuser;
        this.isVerified = isVerified;
    }

    // 일반 회원가입용 정적 팩토리 메서드
    public static UserEntity createUser(String email, String passwordHash, String nickname) {
        return UserEntity.builder()
                .email(email)
                .passwordHash(passwordHash)
                .nickname(nickname)
                .isActive(true)
                .isDeleted(false)
                .isSuperuser(false)
                .isVerified(false)
                .build();
    }

    // 소셜 로그인용 정적 팩토리 메서드
    public static UserEntity createSocialUser(String email, String socialProvider, String socialId, String nickname) {
        return UserEntity.builder()
                .email(email)
                .socialProvider(socialProvider)
                .socialId(socialId)
                .nickname(nickname)
                .isActive(true)
                .isDeleted(false)
                .isSuperuser(false)
                .isVerified(false)
                .build();
    }

    // 관리자용 정적 팩토리 메서드
    public static UserEntity createSuperUser(String email, String passwordHash, String nickname) {
        return UserEntity.builder()
                .email(email)
                .passwordHash(passwordHash)
                .nickname(nickname)
                .isActive(true)
                .isDeleted(false)
                .isSuperuser(true)
                .isVerified(true)
                .build();
    }

    // 도메인 객체로부터 엔티티 생성 (신규 생성용)
    public static UserEntity fromDomain(User user) {

        return UserEntity.builder()
                .userId(user.getUserId())
                .email(user.getEmail().getValue())
                .passwordHash(user.getPasswordHash().getValue())
                .socialProvider(user.getSocialProvider())
                .socialId(user.getSocialId())
                .nickname(user.getNickname())
                .isActive(user.isActive())
                .isDeleted(user.isDeleted())
                .isSuperuser(user.isSuperuser())
                .isVerified(user.isVerified())
                .build();
    }

    public User toDomain() {
        return User.ofEntity(this);
    }

    public void updateFromDomain(User user) {
        this.email = user.getEmail().getValue();
        this.passwordHash = user.getPasswordHash().getValue();
        this.socialProvider = user.getSocialProvider();
        this.socialId = user.getSocialId();
        this.nickname = user.getNickname();
        this.isActive = user.isActive();
        this.isDeleted = user.isDeleted();
        this.isSuperuser = user.isSuperuser();
        this.isVerified = user.isVerified();
    }

    // 개별 필드 업데이트 메서드들 (영속성 최적화용)
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void updateActiveStatus(Boolean isActive) {
        this.isActive = isActive;
    }

    public void updateVerificationStatus(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public void updateSuperuserStatus(Boolean isSuperuser) {
        this.isSuperuser = isSuperuser;
    }

    // 소프트 삭제 (계정 비활성화)
    public void deactivateAccount() {
        this.isActive = false;
    }

    // 계정 복구
    public void reactivateAccount() {
        this.isActive = true;
    }

    public void deleteAccount() {
        this.isDeleted = true;
        this.softDelete(); // SoftDeletableEntity의 메서드 사용
    }
}
