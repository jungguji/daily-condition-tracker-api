package com.jgji.daily_condition_tracker.domain.user.domain;

import com.jgji.daily_condition_tracker.constants.UserConstants;
import com.jgji.daily_condition_tracker.fake.FakePasswordEncoder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
            .build();

    PasswordEncoder passwordEncoder = new FakePasswordEncoder("test");

    @DisplayName("유저 생성 - 성공 케이스")
    @Nested
    class Success {

        @DisplayName("유저 생성 - 일반 유저")
        @Test
        void createRegularUser() {
            // when
            User regularUser = User.createRegularUser(
                    UserConstants.DEFAULT_EMAIL,
                    UserConstants.DEFAULT_PASSWORD_HASH,
                    UserConstants.DEFAULT_NICKNAME
            );

            // then
            assertThat(regularUser.getEmail().getValue()).isEqualTo(UserConstants.DEFAULT_EMAIL.getValue());
        }
    }

    @DisplayName("유저 생성 - 실패 케이스")
    @Nested
    class Fail {

        @DisplayName("유저 생성 - 이메일이 null인 경우")
        @Test
        void createUserWithNullEmail() {
            assertThatThrownBy(() -> User.createRegularUser(
                    null
                    , UserConstants.DEFAULT_PASSWORD_HASH
                    , UserConstants.DEFAULT_NICKNAME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일은 필수값입니다.");
        }

        @DisplayName("유저 생성 - 비밀번호 해시가 null인 경우")
        @Test
        void createUserWithNullPasswordHash() {
            assertThatThrownBy(() -> User.createRegularUser(
                    UserConstants.DEFAULT_EMAIL
                    , null
                    , UserConstants.DEFAULT_NICKNAME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비밀번호 해시는 필수값입니다.");
        }

        @DisplayName("유저 생성 - 닉네임이 null인 경우")
        @Test
        void createUserWithNullNickname() {
            assertThatThrownBy(() -> User.createRegularUser(
                    UserConstants.DEFAULT_EMAIL
                    , UserConstants.DEFAULT_PASSWORD_HASH
                    , null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("닉네임은 필수값입니다.");
        }
    }

    @DisplayName("유저 상태 확인 메서드")
    @Nested
    class UserStatusCheck {
        
        @DisplayName("일반 유저 확인")
        @Test
        void isRegularUser() {
            // given
            User regularUser = User.createRegularUser(
                    UserConstants.DEFAULT_EMAIL,
                    UserConstants.DEFAULT_PASSWORD_HASH,
                    UserConstants.DEFAULT_NICKNAME
            );
            
            // when & then
            assertThat(regularUser.isRegularUser()).isTrue();
            assertThat(regularUser.isSocialUser()).isFalse();
        }
        
        @DisplayName("소셜 유저 확인")
        @Test
        void isSocialUser() {
            // given
            User user = fixtureMonkey.giveMeBuilder(User.class)
                    .setNotNull("socialProvider")
                    .setNotNull("socialId")
                    .sample();
            
            // when & then
            assertThat(user.isSocialUser()).isTrue();
            assertThat(user.isRegularUser()).isFalse();
        }
        
        @DisplayName("로그인 가능 여부 확인 - 활성화되고 인증된 유저")
        @Test
        void canLogin_ActiveAndVerified() {
            // given
            User user = fixtureMonkey.giveMeBuilder(User.class)
                    .set("isActive", true)
                    .set("isVerified", true)
                    .sample();
            
            // when & then
            assertThat(user.canLogin()).isTrue();
        }
        
        @DisplayName("로그인 불가능 - 비활성화된 유저")
        @Test
        void cannotLogin_InactiveUser() {
            // given
            User user = fixtureMonkey.giveMeBuilder(User.class)
                    .set("isActive", false)
                    .sample();
            
            // when & then
            assertThat(user.canLogin()).isFalse();
        }
        
        @DisplayName("관리자 권한 확인 - 슈퍼유저이면서 활성화된 유저")
        @Test
        void hasAdminPrivileges() {
            // given
            User admin = fixtureMonkey.giveMeBuilder(User.class)
                    .set("isActive", true)
                    .set("isSuperuser", true)
                    .sample();
            
            // when & then
            assertThat(admin.hasAdminPrivileges()).isTrue();
        }
    }
    
    @DisplayName("유저 상태 변경 메서드")
    @Nested
    class UserStateChange {
        
        private User createBaseUser() {
            return User.createRegularUser(
                    UserConstants.DEFAULT_EMAIL,
                    UserConstants.DEFAULT_PASSWORD_HASH,
                    UserConstants.DEFAULT_NICKNAME
            );
        }
        
        @DisplayName("닉네임 변경 - 성공")
        @Test
        void updateNickname_Success() {
            // given
            User user = createBaseUser();
            String newNickname = "새로운닉네임";
            
            // when
            User updatedUser = user.updateNickname(newNickname);
            
            // then
            assertThat(updatedUser.getNickname()).isEqualTo(newNickname);
            assertThat(updatedUser.getUserId()).isEqualTo(user.getUserId());
        }
        
        @DisplayName("닉네임 변경 - 실패 (빈 문자열)")
        @Test
        void updateNickname_Fail_EmptyNickname() {
            // given
            User user = createBaseUser();
            
            // when & then
            assertThatThrownBy(() -> user.updateNickname(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("닉네임은 필수값입니다.");
        }
        
        @DisplayName("비밀번호 변경 - 성공")
        @Test
        void updatePassword_Success() {
            // given
            User user = createBaseUser();
            HashedPassword newPassword = HashedPassword.of(RawPassword.of("newPasswordHashnewPasswordHashnewPasswordHash123!@#"), passwordEncoder);
            
            // when
            User updatedUser = user.updatePassword(newPassword);
            
            // then
            assertThat(updatedUser.getPasswordHash()).isEqualTo(newPassword);
            assertThat(updatedUser.getUserId()).isEqualTo(user.getUserId());
        }
        
        @DisplayName("유저 비활성화")
        @Test
        void deactivate() {
            // given
            User user = createBaseUser();
            
            // when
            User deactivatedUser = user.deactivate();
            
            // then
            assertThat(deactivatedUser.isActive()).isFalse();
            assertThat(deactivatedUser.getUserId()).isEqualTo(user.getUserId());
        }
        
        @DisplayName("유저 활성화")
        @Test
        void activate() {
            // given
            User inactiveUser = fixtureMonkey.giveMeBuilder(User.class)
                    .set("isActive", false)
                    .sample();
            
            // when
            User activatedUser = inactiveUser.activate();
            
            // then
            assertThat(activatedUser.isActive()).isTrue();
            assertThat(activatedUser.getUserId()).isEqualTo(inactiveUser.getUserId());
        }
        
        @DisplayName("유저 인증")
        @Test
        void verify() {
            // given
            User unverifiedUser = User.createRegularUser(
                    UserConstants.DEFAULT_EMAIL,
                    UserConstants.DEFAULT_PASSWORD_HASH,
                    UserConstants.DEFAULT_NICKNAME
            );
            
            // when
            User verifiedUser = unverifiedUser.verify();
            
            // then
            assertThat(verifiedUser.isVerified()).isTrue();
        }
        
        @DisplayName("슈퍼유저 권한 부여")
        @Test
        void grantSuperuserPrivileges() {
            // given
            User user = createBaseUser();
            
            // when
            User superuser = user.grantSuperuserPrivileges();
            
            // then
            assertThat(superuser.isSuperuser()).isTrue();
        }
        
        @DisplayName("슈퍼유저 권한 해제")
        @Test
        void revokeSuperuserPrivileges() {
            // given
            User superuser = fixtureMonkey.giveMeBuilder(User.class)
                    .set("isSuperuser", true)
                    .sample();
            
            // when
            User regularUser = superuser.revokeSuperuserPrivileges();
            
            // then
            assertThat(regularUser.isSuperuser()).isFalse();
        }
        
        @DisplayName("유저 소프트 삭제")
        @Test
        void softDelete() {
            // given
            User user = createBaseUser();
            
            // when
            User deletedUser = user.softDelete();
            
            // then
            assertThat(deletedUser.isDeleted()).isTrue();
            assertThat(deletedUser.isActive()).isFalse(); // 삭제 시 비활성화도 됨
        }
    }
}