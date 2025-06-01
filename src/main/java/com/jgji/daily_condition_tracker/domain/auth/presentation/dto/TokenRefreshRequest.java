package com.jgji.daily_condition_tracker.domain.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    @JsonProperty("refresh_token")
    String refreshToken
) {
} 