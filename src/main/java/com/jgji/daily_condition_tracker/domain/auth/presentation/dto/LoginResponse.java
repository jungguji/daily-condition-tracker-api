package com.jgji.daily_condition_tracker.domain.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
    @JsonProperty("access_token")
    String accessToken,
    
    @JsonProperty("token_type")
    String tokenType,
    
    @JsonProperty("refresh_token")
    String refreshToken
) {
    public static LoginResponse of(String accessToken, String refreshToken) {
        return new LoginResponse(accessToken, "bearer", refreshToken);
    }
} 