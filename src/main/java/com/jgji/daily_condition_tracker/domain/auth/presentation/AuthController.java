package com.jgji.daily_condition_tracker.domain.auth.presentation;

import com.jgji.daily_condition_tracker.domain.auth.application.AuthService;
import com.jgji.daily_condition_tracker.domain.auth.application.CustomUserPrincipal;
import com.jgji.daily_condition_tracker.domain.auth.application.PasswordResetTokenService;
import com.jgji.daily_condition_tracker.domain.auth.presentation.dto.*;
import com.jgji.daily_condition_tracker.domain.user.domain.User;
import com.jgji.daily_condition_tracker.domain.shared.presentation.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetTokenService passwordResetTokenService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.debug("로그인 요청 수신: email={}", request.email());
        LoginResponse response = authService.login(request);
        log.debug("로그인 성공: email={}", request.email());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        log.debug("토큰 갱신 요청 수신");
        TokenRefreshResponse response = authService.refresh(request);
        log.debug("토큰 갱신 성공");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserPrincipal userDetails,
            HttpServletRequest request) {
        User user = userDetails.getUser();
        String authorizationHeader = request.getHeader("Authorization");
        
        log.debug("로그아웃 요청 수신: email={}", user.getEmail().getValue());
        authService.logout(user, authorizationHeader);
        log.debug("로그아웃 성공: email={}", user.getEmail().getValue());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequestRequest request) {
        log.debug("비밀번호 재설정 요청 수신: email={}", request.email());
        passwordResetTokenService.processPasswordResetRequest(request.email());
        log.debug("비밀번호 재설정 요청 처리 완료: email={}", request.email());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        log.debug("비밀번호 재설정 확인 요청 수신: token={}", request.token());
        passwordResetTokenService.processPasswordResetConfirm(request.token(), request.newPassword());
        log.debug("비밀번호 재설정 확인 처리 완료: token={}", request.token());
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }
} 