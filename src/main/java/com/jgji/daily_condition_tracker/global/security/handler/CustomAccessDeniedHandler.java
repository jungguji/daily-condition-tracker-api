package com.jgji.daily_condition_tracker.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgji.daily_condition_tracker.domain.shared.presentation.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증은 되었지만 권한이 없는 사용자가 접근할 때 처리하는 Handler
 * Spring Security 6.x 버전에 맞게 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.warn("권한이 없는 사용자의 접근 시도: {} {}, 에러: {}",
                request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ApiResponse<Void> errorResponse = ApiResponse.fail(
                HttpServletResponse.SC_FORBIDDEN,
                "접근 권한이 없습니다."
        );

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
} 