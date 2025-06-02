package com.jgji.daily_condition_tracker.domain.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    String refreshToken
) {
} 