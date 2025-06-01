package com.jgji.daily_condition_tracker.domain.auth.domain;

import com.jgji.daily_condition_tracker.domain.user.domain.HashedPassword;
import com.jgji.daily_condition_tracker.domain.user.domain.RawPassword;
import com.jgji.daily_condition_tracker.fake.FakePasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class HashedPasswordTest {

    PasswordEncoder passwordEncoder = new FakePasswordEncoder();

    @DisplayName("해시된 비밀번호 생성 - 성공 케이스")
    @Nested
    class Success {

        @DisplayName("해시된 비밀번호 생성 - 기본 해시 값")
        @Test
        void createHashedPassword() {
            // when
            RawPassword rawPassword = RawPassword.of("testtesttesttesttesttesttesttest");
            HashedPassword hashedPassword = HashedPassword.of(rawPassword,passwordEncoder);

            // then
            assertEquals("fakeEncoded" + rawPassword, hashedPassword.getValue());
        }
    }

    @DisplayName("해시된 비밀번호 생성 - 실패 케이스")
    @Nested
    class Fail {

        @DisplayName("해시된 비밀번호 생성 - null 값")
        @Test
        void createHashedPasswordWithNullValue() {
            assertThrows(IllegalArgumentException.class, () -> HashedPassword.of(null, passwordEncoder));
        }

        @DisplayName("해시된 비밀번호 생성 - 빈 값")
        @Test
        void createHashedPasswordWithEmptyValue() {
            assertThrows(IllegalArgumentException.class, () -> HashedPassword.of(RawPassword.of(""), passwordEncoder));
        }

        @DisplayName("해시된 비밀번호 생성 - 공백 값")
        @Test
        void createHashedPasswordWithShortValue() {
            assertThrows(IllegalArgumentException.class, () -> HashedPassword.of(RawPassword.of("short"), passwordEncoder));
        }

        @DisplayName("해시된 비밀번호 생성 - 너무 짧은 해시 값")
        @Test
        void createHashedPasswordWithInvalidCharacters() {
            assertThrows(IllegalArgumentException.class, () -> HashedPassword.of(RawPassword.of("invalid@hash!"), passwordEncoder));
        }
    }

}