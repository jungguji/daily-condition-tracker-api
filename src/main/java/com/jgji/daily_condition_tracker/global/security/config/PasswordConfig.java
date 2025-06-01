package com.jgji.daily_condition_tracker.global.security.config;

import com.jgji.daily_condition_tracker.global.security.password.Sha512PasswordEncoder;
import com.jgji.daily_condition_tracker.global.security.value.PasswordProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
@Configuration
public class PasswordConfig {

    private final PasswordProperties passwordProperties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Sha512PasswordEncoder(passwordProperties.getPepper());
    }
}
