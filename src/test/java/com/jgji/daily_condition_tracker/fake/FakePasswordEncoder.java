package com.jgji.daily_condition_tracker.fake;

import org.springframework.security.crypto.password.PasswordEncoder;

public class FakePasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        // Fake encoding logic for testing purposes
        return "fakeEncoded" + rawPassword;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Fake matching logic for testing purposes
        return encodedPassword.equals("fakeEncoded" + rawPassword);
    }
}
