package com.jgji.daily_condition_tracker.domain.user.domain;

public final class Email {
    private final String value;

    private Email(String email) {
        validateEmail(email);
        this.value = email.toLowerCase().trim();
    }

    public static Email of(String email) {
        return new Email(email);
    }

    public String getValue() {
        return value;
    }

    private static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수 입력값입니다.");
        }
        
        String trimmedEmail = email.trim();

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!trimmedEmail.matches(emailRegex)) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
        
        if (trimmedEmail.length() > 100) {
            throw new IllegalArgumentException("이메일은 100자를 초과할 수 없습니다.");
        }
        
        String localPart = trimmedEmail.split("@")[0];
        if (localPart.length() > 64) {
            throw new IllegalArgumentException("이메일의 로컬 부분은 64자를 초과할 수 없습니다.");
        }
        
        String domainPart = trimmedEmail.split("@")[1];
        if (domainPart.length() > 253) {
            throw new IllegalArgumentException("이메일의 도메인 부분은 253자를 초과할 수 없습니다.");
        }
    }
} 