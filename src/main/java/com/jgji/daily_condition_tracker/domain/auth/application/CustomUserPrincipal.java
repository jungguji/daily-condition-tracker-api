package com.jgji.daily_condition_tracker.domain.auth.application;

import com.jgji.daily_condition_tracker.domain.user.domain.Email;
import com.jgji.daily_condition_tracker.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class CustomUserPrincipal implements UserDetails {
    private final User user;
    
    // 토큰 정보만으로 생성하는 생성자 (JWT 인증용)
    public CustomUserPrincipal(Long userId, String email, String nickname, boolean isSuperuser) {
        this.user = User.loginUser(userId, Email.of(email), nickname, isSuperuser);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 기본 사용자 권한
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // 관리자 권한
        if (user.isSuperuser()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash().getValue();
    }

    @Override
    public String getUsername() {
        return user.getEmail().getValue();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 기능은 현재 구현하지 않음
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isActive(); // 활성화된 계정만 잠금 해제
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명 만료 기능은 현재 구현하지 않음
    }

    @Override
    public boolean isEnabled() {
        return user.isActive() && !user.isDeleted();
    }

    // 도메인 객체 접근용 메서드
    public User getUser() {
        return user;
    }
}
