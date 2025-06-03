package com.jgji.daily_condition_tracker.domain.user.application;

import com.jgji.daily_condition_tracker.domain.user.domain.Email;
import com.jgji.daily_condition_tracker.domain.user.domain.HashedPassword;
import com.jgji.daily_condition_tracker.domain.user.domain.RawPassword;
import com.jgji.daily_condition_tracker.domain.user.domain.User;
import com.jgji.daily_condition_tracker.domain.user.infrastructure.UserRepository;
import com.jgji.daily_condition_tracker.domain.user.presentation.dto.SignUpRequest;
import com.jgji.daily_condition_tracker.domain.user.presentation.dto.SignUpResponse;
import com.jgji.daily_condition_tracker.global.exception.BusinessRuleViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SignUpService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public SignUpResponse signUp(SignUpRequest request) {
        log.debug("회원가입 시도: email={}", request.email());

        assert request.email() != null : "이메일은 null일 수 없습니다.";

        validateEmailDuplication(request.email());

        HashedPassword hashedPassword = HashedPassword.of(RawPassword.of(request.password()), passwordEncoder);

        User newUser = User.createRegularUser(
                Email.of(request.email()),
                hashedPassword,
                extractNicknameFromEmail(request.email())
        );

        User savedUser = userRepository.create(newUser);

        assert savedUser.getUserId() > 0 : "저장된 사용자의 ID가 유효하지 않습니다.";

        log.debug("회원가입 완료: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());

        return new SignUpResponse(savedUser.getUserId(), savedUser.getEmail().getValue());
    }

    private void validateEmailDuplication(String emailValue) {
        if (userRepository.existsByEmail(emailValue)) {
            throw new BusinessRuleViolationException("이미 존재하는 이메일입니다: " + emailValue);
        }
    }

    /**
     * 이메일에서 닉네임을 추출하는 임시 메서드
     * 추후 별도의 닉네임 입력 필드나 랜덤 생성 로직으로 대체 가능
     */
    private String extractNicknameFromEmail(String email) {
        assert email != null && email.contains("@") : "유효하지 않은 이메일 형식입니다.";

        String localPart = email.substring(0, email.indexOf('@'));
        return localPart.length() > 20 ? localPart.substring(0, 20) : localPart;
    }
}
