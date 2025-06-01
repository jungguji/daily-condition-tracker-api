package com.jgji.daily_condition_tracker.domain.user.domain;

public final class RawPassword {
    private final String value;

    private RawPassword(String rawValue) {
        validatePassword(rawValue);
        this.value = rawValue;
    }

    public static RawPassword of(String rawValue) {
        return new RawPassword(rawValue);
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 비어있을 수 없습니다.");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }

        if (password.length() > 100) {
            throw new IllegalArgumentException("비밀번호는 100자를 초과할 수 없습니다.");
        }

        boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowerCase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialChar = password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(c) >= 0);

        if (!hasUpperCase) {
            throw new IllegalArgumentException("비밀번호는 대문자를 최소 1개 포함해야 합니다.");
        }
        if (!hasLowerCase) {
            throw new IllegalArgumentException("비밀번호는 소문자를 최소 1개 포함해야 합니다.");
        }
        if (!hasDigit) {
            throw new IllegalArgumentException("비밀번호는 숫자를 최소 1개 포함해야 합니다.");
        }
        if (!hasSpecialChar) {
            throw new IllegalArgumentException("비밀번호는 특수문자를 최소 1개 포함해야 합니다.");
        }
    }

    String getValue() {
        return value;
    }
}
