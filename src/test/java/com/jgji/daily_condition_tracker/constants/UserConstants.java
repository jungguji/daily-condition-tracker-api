package com.jgji.daily_condition_tracker.constants;

import com.jgji.daily_condition_tracker.domain.user.domain.Email;
import com.jgji.daily_condition_tracker.domain.user.domain.HashedPassword;
import com.jgji.daily_condition_tracker.domain.user.domain.RawPassword;
import com.jgji.daily_condition_tracker.fake.FakePasswordEncoder;

public class UserConstants {

    public static final Email DEFAULT_EMAIL = Email.of("test@naver.com");
    public static final HashedPassword DEFAULT_PASSWORD_HASH = HashedPassword.of(RawPassword.of("testPasswordHashtestPasswordHashtestPasswordHashtestPasswordHash"), new FakePasswordEncoder());
    public static final String DEFAULT_NICKNAME = "testNickname";

}
