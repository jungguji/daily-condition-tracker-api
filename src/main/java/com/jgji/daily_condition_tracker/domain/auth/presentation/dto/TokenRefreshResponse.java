package com.jgji.daily_condition_tracker.domain.auth.presentation.dto;

public record TokenRefreshResponse(
    String accessToken,

    String tokenType,

    String refreshToken
) {
    public static TokenRefreshResponse of(String accessToken, String refreshToken) {
        return new TokenRefreshResponse(accessToken, "bearer", refreshToken);
    }
} 