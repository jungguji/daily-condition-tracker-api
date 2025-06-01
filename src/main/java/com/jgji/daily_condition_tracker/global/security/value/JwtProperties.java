package com.jgji.daily_condition_tracker.global.security.value;

public interface JwtProperties {

    String getSecret();

    long getAccessTokenExpirationMs();

    long getRefreshTokenExpirationMs();
}
