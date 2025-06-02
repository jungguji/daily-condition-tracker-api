package com.jgji.daily_condition_tracker.domain.user.domain;

import com.jgji.daily_condition_tracker.global.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RawPasswordTest {

    private static final String VALID_PASSWORD = "TestPassword1!";
    private static final String PASSWORD_WITHOUT_UPPERCASE = "testpassword1!";
    private static final String PASSWORD_WITHOUT_LOWERCASE = "TESTPASSWORD1!";
    private static final String PASSWORD_WITHOUT_DIGIT = "TestPassword!";
    private static final String PASSWORD_WITHOUT_SPECIAL_CHAR = "TestPassword1";
    private static final String SHORT_PASSWORD = "Test1!";
    private static final String LONG_PASSWORD = "A".repeat(90) + "testpassword1!";

    @DisplayName("비밀번호 생성 - 성공 케이스")
    @Nested
    class Success {

        @DisplayName("유효한 비밀번호로 RawPassword 생성")
        @Test
        void createWithValidPassword() {
            RawPassword rawPassword = RawPassword.of(VALID_PASSWORD);

            assertThat(rawPassword).isNotNull();
            assertThat(rawPassword.getValue()).isEqualTo(VALID_PASSWORD);
        }

        @DisplayName("최소 길이 비밀번호로 생성 - 8자")
        @Test
        void createWithMinimumLength() {
            String minimumPassword = "Test123!";

            RawPassword rawPassword = RawPassword.of(minimumPassword);

            assertThat(rawPassword).isNotNull();
            assertThat(rawPassword.getValue()).isEqualTo(minimumPassword);
        }

        @DisplayName("최대 길이 비밀번호로 생성 - 100자")
        @Test
        void createWithMaximumLength() {
            String maximumPassword = "A".repeat(89) + "testpass1!";

            RawPassword rawPassword = RawPassword.of(maximumPassword);

            assertThat(rawPassword).isNotNull();
            assertThat(rawPassword.getValue()).isEqualTo(maximumPassword);
        }

        @DisplayName("모든 특수문자가 포함된 비밀번호로 생성")
        @Test
        void createWithAllSpecialCharacters() {
            String passwordWithAllSpecials = "Test123!@#$%^&*()_+-=[]{}|;:,.<>?";

            RawPassword rawPassword = RawPassword.of(passwordWithAllSpecials);

            assertThat(rawPassword).isNotNull();
            assertThat(rawPassword.getValue()).isEqualTo(passwordWithAllSpecials);
        }
    }

    @DisplayName("비밀번호 생성 - 실패 케이스")
    @Nested
    class Fail {

        @DisplayName("null 비밀번호로 생성 시 예외 발생")
        @Test
        void createWithNullPassword() {
            assertThatThrownBy(() -> RawPassword.of(null))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("비밀번호는 비어있을 수 없습니다.");
        }

        @DisplayName("빈 문자열 비밀번호로 생성 시 예외 발생")
        @Test
        void createWithEmptyPassword() {
            assertThatThrownBy(() -> RawPassword.of(""))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("비밀번호는 비어있을 수 없습니다.");
        }

        @DisplayName("공백만 있는 비밀번호로 생성 시 예외 발생")
        @Test
        void createWithWhitespaceOnlyPassword() {
            assertThatThrownBy(() -> RawPassword.of("   "))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("비밀번호는 비어있을 수 없습니다.");
        }

        @DisplayName("8자 미만 비밀번호로 생성 시 예외 발생")
        @Test
        void createWithTooShortPassword() {
            assertThatThrownBy(() -> RawPassword.of(SHORT_PASSWORD))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("비밀번호는 8자 이상이어야 합니다.");
        }

        @DisplayName("100자 초과 비밀번호로 생성 시 예외 발생")
        @Test
        void createWithTooLongPassword() {
            assertThatThrownBy(() -> RawPassword.of(LONG_PASSWORD))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("비밀번호는 100자를 초과할 수 없습니다.");
        }

        @DisplayName("대문자가 없는 비밀번호로 생성 시 예외 발생")
        @Test
        void createWithoutUpperCase() {
            assertThatThrownBy(() -> RawPassword.of(PASSWORD_WITHOUT_UPPERCASE))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("비밀번호는 대문자를 최소 1개 포함해야 합니다.");
        }

        @DisplayName("소문자가 없는 비밀번호로 생성 시 예외 발생")
        @Test
        void createWithoutLowerCase() {
            assertThatThrownBy(() -> RawPassword.of(PASSWORD_WITHOUT_LOWERCASE))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("비밀번호는 소문자를 최소 1개 포함해야 합니다.");
        }

        @DisplayName("숫자가 없는 비밀번호로 생성 시 예외 발생")
        @Test
        void createWithoutDigit() {
            assertThatThrownBy(() -> RawPassword.of(PASSWORD_WITHOUT_DIGIT))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("비밀번호는 숫자를 최소 1개 포함해야 합니다.");
        }

        @DisplayName("특수문자가 없는 비밀번호로 생성 시 예외 발생")
        @Test
        void createWithoutSpecialCharacter() {
            assertThatThrownBy(() -> RawPassword.of(PASSWORD_WITHOUT_SPECIAL_CHAR))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("비밀번호는 특수문자를 최소 1개 포함해야 합니다.");
        }
    }
} 