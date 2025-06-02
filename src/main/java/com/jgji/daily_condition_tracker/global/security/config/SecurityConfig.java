package com.jgji.daily_condition_tracker.global.security.config;

import com.jgji.daily_condition_tracker.global.security.filter.JwtAuthenticationFilter;
import com.jgji.daily_condition_tracker.global.security.handler.CustomAccessDeniedHandler;
import com.jgji.daily_condition_tracker.global.security.handler.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                         CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                         CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // REST API 환경에서는 csrf 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 사용으로 세션 비활성화
                .authorizeHttpRequests(authz -> authz
                        // 인증이 필요 없는 경로들
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll() // 로그인
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll() // 토큰 갱신
                        .requestMatchers(HttpMethod.POST, "/api/v1/user/signup").permitAll() // 회원가입
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll() // 헬스 체크
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/password-reset/request").permitAll() // 로그아웃
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll() // Swagger UI
                        // 인증이 필요한 경로들
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated() // 로그아웃
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()

                        .anyRequest().authenticated() // 그 외 모든 경로는 인증 필요
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // 인증 실패 시 처리
                        .accessDeniedHandler(customAccessDeniedHandler) // 권한 없음 시 처리
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
