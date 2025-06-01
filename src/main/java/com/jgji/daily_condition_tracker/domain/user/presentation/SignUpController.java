package com.jgji.daily_condition_tracker.domain.user.presentation;

import com.jgji.daily_condition_tracker.domain.user.presentation.dto.SignUpRequest;
import com.jgji.daily_condition_tracker.domain.user.presentation.dto.SignUpResponse;
import com.jgji.daily_condition_tracker.domain.user.application.SignUpService;
import com.jgji.daily_condition_tracker.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@RestController
public class SignUpController {

    private final SignUpService signUpService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public ApiResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("회원가입 요청 수신: email={}", request.email());

        SignUpResponse response = signUpService.signUp(request);

        log.info("회원가입 응답 전송: userId={}", response.userId());

        return ApiResponse.success(response);
    }
}
