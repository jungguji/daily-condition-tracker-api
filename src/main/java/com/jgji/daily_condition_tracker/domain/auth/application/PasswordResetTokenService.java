package com.jgji.daily_condition_tracker.domain.auth.application;

import com.jgji.daily_condition_tracker.domain.auth.domain.PasswordResetToken;
import com.jgji.daily_condition_tracker.domain.auth.infrastructure.PasswordResetTokenRepository;
import com.jgji.daily_condition_tracker.domain.shared.infrastructure.EmailSender;
import com.jgji.daily_condition_tracker.domain.user.domain.HashedPassword;
import com.jgji.daily_condition_tracker.domain.user.domain.RawPassword;
import com.jgji.daily_condition_tracker.domain.user.domain.User;
import com.jgji.daily_condition_tracker.domain.user.infrastructure.UserRepository;
import com.jgji.daily_condition_tracker.global.exception.InvalidTokenException;
import com.jgji.daily_condition_tracker.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PasswordResetTokenService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public void processPasswordResetRequest(String email) {
        long startTime = System.currentTimeMillis();
        Optional<User> userOptional = userRepository.findByEmail(email);

        // 존재하지 않아도 동일한 응답을 주기 위해 오류를 발생시키지 않음
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            PasswordResetToken passwordResetToken = PasswordResetToken.create(user.getUserId());
            passwordResetTokenRepository.save(passwordResetToken);

            try {
                emailSender.sendPasswordResetEmail(email, passwordResetToken.getToken());
                log.info("비밀번호 재설정 이메일 발송 완료: email={}", email);
            } catch (Exception e) {
                log.error("비밀번호 재설정 이메일 발송 실패: email={}, error={}", email, e.getMessage(), e);
                // 이메일 발송 실패 시에도 사용자에게는 성공 응답을 반환하여 보안을 유지
            }
        } else {
            log.info("존재하지 않는 이메일로 비밀번호 재설정 요청: email={}", email);
            ensureConstantResponseTime();
        }

        // 사용자 존재 여부에 관계없이 동일한 시간 내에 응답하도록 하여 타이밍 공격 방지
        ensureMinimumResponseTime(startTime);
    }

    private void ensureConstantResponseTime() {
        try {
            Thread.sleep(1000 + new SecureRandom().nextInt(500)); // 1~1.5초 랜덤
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void ensureMinimumResponseTime(long startTime) {
        final int minTimeMs = 1500;
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed < minTimeMs) {
            try {
                Thread.sleep(minTimeMs - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void processPasswordResetConfirm(String token, String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 토큰입니다."));
        
        if (!passwordResetToken.isValid()) {
            throw new InvalidTokenException("유효하지 않거나 만료된 토큰입니다.");
        }
        
        User user = userRepository.findByUserId(passwordResetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        
        RawPassword rawPassword = RawPassword.of(newPassword);
        HashedPassword hashedPassword = HashedPassword.of(rawPassword, passwordEncoder);
        User updatedUser = user.updatePassword(hashedPassword);
        userRepository.update(updatedUser);
        
        PasswordResetToken usedToken = passwordResetToken.markAsUsed();
        passwordResetTokenRepository.save(usedToken);
        
        log.info("비밀번호 재설정 완료: userId={}", user.getUserId());
    }
}
