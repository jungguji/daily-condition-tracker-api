package com.jgji.daily_condition_tracker.global.security.value;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "jwt")
class JwtPropertiesImpl implements JwtProperties {

    private final String secret;
    private final Expiration expiration;

    public JwtPropertiesImpl(String secret, Expiration expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    @Override
    public long getAccessTokenExpirationMs() {
        return Long.parseLong(expiration.accessTokenMs);
    }

    @Override
    public long getRefreshTokenExpirationMs() {
        return Long.parseLong(expiration.refreshTokenMs);
    }

    @Getter
    private static class Expiration {

        private final String accessTokenMs;
        private final String refreshTokenMs;

        @ConstructorBinding
        public Expiration(String accessTokenMs, String refreshTokenMs) {
            this.accessTokenMs = accessTokenMs;
            this.refreshTokenMs = refreshTokenMs;
        }
    }
}
