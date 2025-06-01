package com.jgji.daily_condition_tracker.domain.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenRefreshResponse(
    @JsonProperty("access_token")
    String accessToken,
    
    @JsonProperty("token_type")
    String tokenType,
    
    @JsonProperty("refresh_token")
    String refreshToken
) {
    public static TokenRefreshResponse of(String accessToken, String refreshToken) {
        return new TokenRefreshResponse(accessToken, "bearer", refreshToken);
    }
} 