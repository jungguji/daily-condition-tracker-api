package com.jgji.daily_condition_tracker.domain.auth.application;

import com.jgji.daily_condition_tracker.domain.user.domain.User;
import com.jgji.daily_condition_tracker.domain.user.infrastructure.UserRepository;
import com.jgji.daily_condition_tracker.global.security.filter.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security에서 사용하는 UserDetailsService 구현체
 * 로그인 시에만 데이터베이스에서 사용자 정보를 조회
 * JWT 인증에서는 토큰 정보만으로 UserDetails 생성
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인 시에만 사용되는 메서드 - DB에서 사용자 정보 조회
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("로그인 인증 시도: email={}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        if (user.isNotActive()) {
            throw new UsernameNotFoundException("비활성화된 사용자입니다: " + email);
        }
        
        log.debug("로그인 인증 성공: email={}, verified={}", email, user.isVerified());
        
        return new CustomUserPrincipal(user);
    }

    /**
     * JWT 토큰 정보만으로 UserDetails 생성 (DB 조회 없음)
     * JwtAuthenticationFilter에서 사용
     */
    public UserDetails loadUserFromToken(String token) {
        log.debug("JWT 토큰으로 사용자 인증 처리");
        
        try {
            // 토큰에서 사용자 정보 추출
            Long userId = jwtTokenProvider.getUserId(token);
            String email = jwtTokenProvider.getEmail(token);
            String nickname = jwtTokenProvider.getNickname(token);
            Boolean isSuperuser = jwtTokenProvider.getIsSuperuser(token);
            
            log.debug("JWT 토큰 인증 성공: userId={}, email={}", userId, email);
            
            return new CustomUserPrincipal(userId, email, nickname, isSuperuser != null && isSuperuser);
        } catch (Exception e) {
            log.error("JWT 토큰에서 사용자 정보 추출 실패", e);
            throw new UsernameNotFoundException("토큰에서 사용자 정보를 추출할 수 없습니다", e);
        }
    }
} 