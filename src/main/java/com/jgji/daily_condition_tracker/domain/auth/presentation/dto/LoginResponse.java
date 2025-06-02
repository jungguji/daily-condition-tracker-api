package com.jgji.daily_condition_tracker.domain.auth.presentation.dto;

public record LoginResponse(
    String accessToken,
    String tokenType,
    String refreshToken
) {
    public static LoginResponse of(String accessToken, String refreshToken) {
        return new LoginResponse(accessToken, "bearer", refreshToken);
    }
} 