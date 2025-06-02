package com.jgji.daily_condition_tracker.domain.user.domain;

import com.jgji.daily_condition_tracker.constants.UserConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @DisplayName("이메일 생성 - 성공 케이스")
    @Nested
    class Success {

        @DisplayName("유효한 이메일들로 Email 생성")
        @ParameterizedTest(name = "{index} => email={0}")
        @ValueSource(strings = {
                "test@example.com",
                "user@domain.co.kr",
                "user.name@domain.com",
                "user+tag@domain.org",
                "user123@domain123.net",
                "user_name@sub.domain.com",
                "a@b.co",
                "test.email+tag@sub.domain.example.org"
        })
        void createWithValidEmails(String validEmail) {
            Email email = Email.of(validEmail);

            assertThat(email).isNotNull();
            assertThat(email.getValue()).isEqualTo(validEmail.toLowerCase());
        }

        @DisplayName("경계값 테스트 - 최대 길이 이메일들")
        @ParameterizedTest(name = "{index} => email={0}, expectedLength={1}")
        @MethodSource("getMaxLengthEmailArguments")
        void createWithMaximumLengthEmails(String email, int expectedLength) {
            Email createdEmail = Email.of(email);

            assertThat(createdEmail).isNotNull();
            assertThat(createdEmail.getValue()).hasSize(expectedLength);
        }

        static Stream<Object[]> getMaxLengthEmailArguments() {
            return Stream.of(
                    new Object[]{"a".repeat(49) + "@"+ "b".repeat(46) + ".com", 100}, // 전체 100자
                    new Object[]{"a".repeat(63) + "@example.com", 75} // 로컬 64자
            );
        }

        @DisplayName("UserConstants의 기본 이메일로 생성")
        @Test
        void createWithDefaultEmail() {
            Email email = UserConstants.DEFAULT_EMAIL;

            assertThat(email).isNotNull();
            assertThat(email.getValue()).isEqualTo("test@naver.com");
        }

        @DisplayName("대소문자 섞인 이메일로 생성 - 소문자로 변환 확인")
        @Test
        void createWithMixedCaseEmail() {
            String mixedCaseEmail = "Test@Example.COM";

            Email email = Email.of(mixedCaseEmail);

            assertThat(email.getValue()).isEqualTo("test@example.com");
        }

        @DisplayName("공백이 포함된 이메일로 생성 - trim 처리 확인")
        @Test
        void createWithEmailHavingSpaces() {
            String emailWithSpaces = "  test@example.com  ";

            Email email = Email.of(emailWithSpaces);

            assertThat(email.getValue()).isEqualTo("test@example.com");
        }
    }

    @DisplayName("이메일 생성 - 실패 케이스")
    @Nested
    class Fail {

        @DisplayName("null/빈값으로 생성 시 예외 발생")
        @ParameterizedTest(name = "{index} => email={0}")
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        void createWithNullOrEmptyEmail(String invalidEmail) {
            assertThatThrownBy(() -> Email.of(invalidEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일은 필수 입력값입니다.");
        }

        @DisplayName("null 이메일로 생성 시 예외 발생")
        @Test
        void createWithNullEmail() {
            assertThatThrownBy(() -> Email.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일은 필수 입력값입니다.");
        }

        @DisplayName("잘못된 형식의 이메일들로 생성 시 예외 발생")
        @ParameterizedTest(name = "{index} => email={0}")
        @ValueSource(strings = {
                "testexample.com",           // @ 없음
                "test@",                     // 도메인 없음
                "@example.com",              // 로컬 부분 없음
                "test@@example.com",         // 다중 @
                "test@example",              // TLD 없음
                "user..name@domain.com",     // 연속 점
                "user@domain.",              // 도메인 끝 점
                ".user@domain.com",          // 로컬 시작 점
                "user@.domain.com",          // 도메인 시작 점
                "user name@domain.com",      // 공백 포함
                "user@domain .com"           // 도메인 공백
        })
        void createWithInvalidFormats(String invalidEmail) {
            assertThatThrownBy(() -> Email.of(invalidEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("올바른 이메일 형식이 아닙니다.");
        }

        @DisplayName("길이 제한 초과 이메일들로 생성 시 예외 발생")
        @ParameterizedTest(name = "{index} => email={0}, expectedMessage={1}")
        @MethodSource("getTooLongEmailArguments")
        void createWithTooLongEmails(String tooLongEmail, String expectedMessage) {
            assertThatThrownBy(() -> Email.of(tooLongEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(expectedMessage);
        }

        static Stream<Object[]> getTooLongEmailArguments() {
            return Stream.of(
                    new Object[]{
                            "a".repeat(95) + "@example.com", // 101자
                            "이메일은 100자를 초과할 수 없습니다."
                    },
                    new Object[]{
                            "a".repeat(65) + "@example.com", // 로컬 65자
                            "이메일의 로컬 부분은 64자를 초과할 수 없습니다."
                    }
            );
        }
    }
} 