package com.jgji.daily_condition_tracker.domain.auth.application;

import com.jgji.daily_condition_tracker.domain.auth.infrastructure.TokenRepository;
import com.jgji.daily_condition_tracker.domain.auth.presentation.dto.LoginRequest;
import com.jgji.daily_condition_tracker.domain.auth.presentation.dto.LoginResponse;
import com.jgji.daily_condition_tracker.domain.auth.presentation.dto.TokenRefreshRequest;
import com.jgji.daily_condition_tracker.domain.auth.presentation.dto.TokenRefreshResponse;
import com.jgji.daily_condition_tracker.domain.user.domain.User;
import com.jgji.daily_condition_tracker.domain.user.infrastructure.UserRepository;
import com.jgji.daily_condition_tracker.global.exception.InvalidCredentialsException;
import com.jgji.daily_condition_tracker.global.exception.InvalidTokenException;
import com.jgji.daily_condition_tracker.global.security.filter.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            return LoginResponse.of(accessToken, refreshToken);
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    @Transactional(readOnly = true)
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String refreshToken = request.refreshToken();
        
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다.");
        }
        
        if (tokenRepository.isBlacklisted(refreshToken)) {
            throw new InvalidTokenException("블랙리스트에 등록된 토큰입니다.");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidCredentialsException("사용자를 찾을 수 없습니다."));

        CustomUserPrincipal userPrincipal = new CustomUserPrincipal(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities());

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        tokenRepository.addToBlacklist(refreshToken);
        
        return TokenRefreshResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional(rollbackFor = Exception.class)
    public void logout(User user, String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("유효하지 않은 Authorization 헤더입니다.");
        }
        
        String accessToken = authorizationHeader.substring(7);
        
        String tokenUsername = jwtTokenProvider.getUsername(accessToken);
        if (!user.getEmail().getValue().equals(tokenUsername)) {
            throw new InvalidTokenException("토큰의 사용자 정보가 일치하지 않습니다.");
        }

        tokenRepository.addToBlacklist(accessToken);
        log.debug("사용자 로그아웃 처리 완료: email={}, 토큰을 블랙리스트에 추가", user.getEmail().getValue());
    }
} 