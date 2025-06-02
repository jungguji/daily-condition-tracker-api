package com.jgji.daily_condition_tracker.domain.user.domain;

import com.jgji.daily_condition_tracker.constants.UserConstants;
import com.jgji.daily_condition_tracker.fake.FakePasswordEncoder;
import com.jgji.daily_condition_tracker.global.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class HashedPasswordTest {

    PasswordEncoder passwordEncoder = new FakePasswordEncoder("test");

    @DisplayName("해시된 비밀번호 생성 - 성공 케이스")
    @Nested
    class Success {

        @DisplayName("해시된 비밀번호 생성 - 기본 해시 값")
        @Test
        void createHashedPassword() {
            // given
            RawPassword rawPassword = UserConstants.DEFAULT_RAW_PASSWORD;

            // when
            HashedPassword hashedPassword = HashedPassword.of(rawPassword, passwordEncoder);

            // then
            assertTrue(passwordEncoder.matches(rawPassword.getValue(), hashedPassword.getValue()));
        }
    }

    @DisplayName("해시된 비밀번호 생성 - 실패 케이스")
    @Nested
    class Fail {

        @DisplayName("해시된 비밀번호 생성 - null 값")
        @Test
        void createHashedPasswordWithNullValue() {
            assertThrows(NullPointerException.class, () -> HashedPassword.of(null, passwordEncoder));
        }

        @DisplayName("해시된 비밀번호 생성 - 빈 값")
        @Test
        void createHashedPasswordWithEmptyValue() {
            assertThrows(BusinessRuleViolationException.class, () -> HashedPassword.of(RawPassword.of(""), passwordEncoder));
        }

        @DisplayName("해시된 비밀번호 생성 - 공백 값")
        @Test
        void createHashedPasswordWithShortValue() {
            assertThrows(BusinessRuleViolationException.class, () -> HashedPassword.of(RawPassword.of("short"), passwordEncoder));
        }

        @DisplayName("해시된 비밀번호 생성 - 너무 짧은 해시 값")
        @Test
        void createHashedPasswordWithInvalidCharacters() {
            assertThrows(BusinessRuleViolationException.class, () -> HashedPassword.of(RawPassword.of("invalid@hash!"), passwordEncoder));
        }
    }

}