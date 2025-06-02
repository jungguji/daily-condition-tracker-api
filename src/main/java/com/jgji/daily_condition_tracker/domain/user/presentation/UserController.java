package com.jgji.daily_condition_tracker.domain.user.presentation;

import com.jgji.daily_condition_tracker.domain.auth.application.CustomUserPrincipal;
import com.jgji.daily_condition_tracker.domain.user.application.UserService;
import com.jgji.daily_condition_tracker.domain.user.domain.User;
import com.jgji.daily_condition_tracker.domain.user.presentation.dto.CurrentUserResponse;
import com.jgji.daily_condition_tracker.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserPrincipal userDetails) {
        User currentUser = userDetails.getUser();
        
        CurrentUserResponse response = userService.getCurrentUserInfo(currentUser);
        
        log.debug("현재 사용자 정보 조회 성공: userId={}, email={}", currentUser.getUserId(), currentUser.getEmail().getValue());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
} 