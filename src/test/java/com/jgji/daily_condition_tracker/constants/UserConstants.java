package com.jgji.daily_condition_tracker.constants;

import com.jgji.daily_condition_tracker.domain.user.domain.Email;
import com.jgji.daily_condition_tracker.domain.user.domain.HashedPassword;
import com.jgji.daily_condition_tracker.domain.user.domain.RawPassword;
import com.jgji.daily_condition_tracker.fake.FakePasswordEncoder;

public class UserConstants {

    public static final Email DEFAULT_EMAIL = Email.of("test@naver.com");
    public static final String DEFAULT_STRING_PASSWORD = "testPassword!@#123HashtestPasswordHashtestPasswordHashtest";
    public static final RawPassword DEFAULT_RAW_PASSWORD = RawPassword.of(DEFAULT_STRING_PASSWORD);
    public static final HashedPassword DEFAULT_PASSWORD_HASH = HashedPassword.of(DEFAULT_RAW_PASSWORD, new FakePasswordEncoder("test"));
    public static final String DEFAULT_NICKNAME = "testNickname";

}
