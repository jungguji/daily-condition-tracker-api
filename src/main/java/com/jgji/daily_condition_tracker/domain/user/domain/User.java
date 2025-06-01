package com.jgji.daily_condition_tracker.domain.user.domain;

import com.jgji.daily_condition_tracker.domain.user.infrastructure.UserEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
public final class User {

    private final Long userId;
    private final Email email;
    private final HashedPassword passwordHash;
    private final String socialProvider;
    private final String socialId;
    private final String nickname;
    private final boolean isActive;
    private final boolean isSuperuser;
    private final boolean isVerified;
    private final boolean isDeleted;

    @Builder(access = lombok.AccessLevel.PRIVATE)
    private User(Long userId, Email email, HashedPassword passwordHash, String socialProvider, String socialId, String nickname, boolean isActive, boolean isSuperuser, boolean isVerified, boolean isDeleted) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        this.nickname = nickname;
        this.isActive = isActive;
        this.isSuperuser = isSuperuser;
        this.isVerified = isVerified;
        this.isDeleted = isDeleted;
    }

    public static User loginUser(
            Long userId,
            Email email,
            String nickname,
            boolean isSuperuser) {
        return User.builder()
                .userId(userId)
                .email(email)
                .passwordHash(null)
                .socialProvider(null)
                .socialId(null)
                .nickname(nickname)
                .isActive(true)
                .isSuperuser(isSuperuser)
                .isVerified(true)
                .isDeleted(false)
                .build();
    }

    // 레이어 규칙을 위반하지만 실수 방지를 위해 사용
    // 모두 오픈한 메서드를 생성한다면, 다른 곳에서 무분별하게 사용가능한 것을 막기 위해
    public static User ofEntity(UserEntity entity) {
        return User.builder()
                .userId(entity.getUserId())
                .email(Email.of(entity.getEmail()))
                .passwordHash(new HashedPassword(entity.getPasswordHash()))
                .socialProvider(entity.getSocialProvider())
                .socialId(entity.getSocialId())
                .nickname(entity.getNickname())
                .isActive(entity.isActive())
                .isSuperuser(entity.isSuperuser())
                .isVerified(entity.isVerified())
                .isDeleted(entity.isDeleted())
                .build();
    }

    public static User createRegularUser(Email email, HashedPassword passwordHash, String nickname) {
        validateEmail(email);
        validatePasswordHash(passwordHash);
        validateNickname(nickname);
        
        return User.builder()
                .email(email)
                .passwordHash(passwordHash)
                .nickname(nickname)
                .isActive(true)
                .isSuperuser(false)
                .isVerified(false)
                .isDeleted(false)
                .build();
    }

    public boolean isSocialUser() {
        return socialProvider != null && socialId != null;
    }

    public boolean isRegularUser() {
        return !isSocialUser();
    }

    public boolean isNotActive() {
        return !this.isActive || this.isDeleted;
    }

    public boolean canLogin() {
        return isActive && isVerified;
    }

    public boolean hasAdminPrivileges() {
        return isSuperuser && isActive;
    }

    public User updateNickname(String newNickname) {
        validateNickname(newNickname);
        
        return User.builder()
                .userId(this.userId)
                .email(this.email)
                .passwordHash(this.passwordHash)
                .socialProvider(this.socialProvider)
                .socialId(this.socialId)
                .nickname(newNickname)
                .isActive(this.isActive)
                .isDeleted(this.isDeleted)
                .isSuperuser(this.isSuperuser)
                .isVerified(this.isVerified)
                .build();
    }

    public User updatePassword(HashedPassword hashedPassword) {
        validatePasswordHash(hashedPassword);

        return User.builder()
                .userId(this.userId)
                .email(this.email)
                .passwordHash(hashedPassword)
                .socialProvider(this.socialProvider)
                .socialId(this.socialId)
                .nickname(this.nickname)
                .isActive(this.isActive)
                .isDeleted(this.isDeleted)
                .isSuperuser(this.isSuperuser)
                .isVerified(this.isVerified)
                .build();
    }

    public User deactivate() {
        return User.builder()
                .userId(this.userId)
                .email(this.email)
                .passwordHash(this.passwordHash)
                .socialProvider(this.socialProvider)
                .socialId(this.socialId)
                .nickname(this.nickname)
                .isActive(false)
                .isDeleted(this.isDeleted)
                .isSuperuser(this.isSuperuser)
                .isVerified(this.isVerified)
                .build();
    }

    public User activate() {
        return User.builder()
                .userId(this.userId)
                .email(this.email)
                .passwordHash(this.passwordHash)
                .socialProvider(this.socialProvider)
                .socialId(this.socialId)
                .nickname(this.nickname)
                .isActive(true)
                .isDeleted(this.isDeleted)
                .isSuperuser(this.isSuperuser)
                .isVerified(this.isVerified)
                .build();
    }

    public User verify() {
        return User.builder()
                .userId(this.userId)
                .email(this.email)
                .passwordHash(this.passwordHash)
                .socialProvider(this.socialProvider)
                .socialId(this.socialId)
                .nickname(this.nickname)
                .isActive(this.isActive)
                .isDeleted(this.isDeleted)
                .isSuperuser(this.isSuperuser)
                .isVerified(true)
                .build();
    }

    public User grantSuperuserPrivileges() {
        return User.builder()
                .userId(this.userId)
                .email(this.email)
                .passwordHash(this.passwordHash)
                .socialProvider(this.socialProvider)
                .socialId(this.socialId)
                .nickname(this.nickname)
                .isActive(this.isActive)
                .isDeleted(this.isDeleted)
                .isSuperuser(true)
                .isVerified(this.isVerified)
                .build();
    }

    public User revokeSuperuserPrivileges() {
        return User.builder()
                .userId(this.userId)
                .email(this.email)
                .passwordHash(this.passwordHash)
                .socialProvider(this.socialProvider)
                .socialId(this.socialId)
                .nickname(this.nickname)
                .isActive(this.isActive)
                .isDeleted(this.isDeleted)
                .isSuperuser(false)
                .isVerified(this.isVerified)
                .build();
    }

    public User softDelete() {
        return User.builder()
                .userId(this.userId)
                .email(this.email)
                .passwordHash(this.passwordHash)
                .socialProvider(this.socialProvider)
                .socialId(this.socialId)
                .nickname(this.nickname)
                .isActive(false)
                .isDeleted(true)
                .isSuperuser(this.isSuperuser)
                .isVerified(this.isVerified)
                .build();
    }

    private static void validateEmail(Email email) {
        if (email == null) {
            throw new IllegalArgumentException("이메일은 필수값입니다.");
        }
    }

    private static void validatePasswordHash(HashedPassword passwordHash) {
        if (passwordHash == null) {
            throw new IllegalArgumentException("비밀번호 해시는 필수값입니다.");
        }
    }
    
    private static void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수값입니다.");
        }
        
        if (nickname.length() > 50) {
            throw new IllegalArgumentException("닉네임은 50자를 초과할 수 없습니다.");
        }
        
        if (nickname.trim().length() < 2) {
            throw new IllegalArgumentException("닉네임은 최소 2자 이상이어야 합니다.");
        }

        String nicknameRegex = "^[가-힣a-zA-Z0-9\\s]+$";
        if (!nickname.matches(nicknameRegex)) {
            throw new IllegalArgumentException("닉네임은 한글, 영문, 숫자, 공백만 사용할 수 있습니다.");
        }
    }
}
