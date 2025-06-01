package com.jgji.daily_condition_tracker.domain.user.domain;

import org.springframework.security.crypto.password.PasswordEncoder;

public final class HashedPassword {
    private final String value;

    HashedPassword(String hashedValue) {
        if (hashedValue == null || hashedValue.trim().isEmpty()) {
            throw new IllegalArgumentException("해시된 비밀번호는 비어있을 수 없습니다.");
        }

        if (hashedValue.length() < 32) {
            throw new IllegalArgumentException("유효하지 않은 해시 형식입니다. 해시된 비밀번호가 너무 짧습니다.");
        }

        if (hashedValue.length() > 512) {
            throw new IllegalArgumentException("유효하지 않은 해시 형식입니다. 해시된 비밀번호가 너무 깁니다.");
        }

        String validHashPattern = "^[A-Za-z0-9+/=.$-]+$";
        if (!hashedValue.matches(validHashPattern)) {
            throw new IllegalArgumentException("유효하지 않은 해시 형식입니다. 허용되지 않은 문자가 포함되어 있습니다.");
        }
        
        this.value = hashedValue;
    }

    /**
     * 비밀번호를 해시하여 HashedPassword 객체를 생성합니다.
     *
     * @param rawPassword 원본 비밀번호
     * @param passwordEncoder 비밀번호 인코더
     * @return HashedPassword 객체
     */
    public static HashedPassword of(RawPassword rawPassword, PasswordEncoder passwordEncoder) {
        return new HashedPassword(passwordEncoder.encode(rawPassword.getValue()));
    }

    public String getValue() {
        return value;
    }
}
