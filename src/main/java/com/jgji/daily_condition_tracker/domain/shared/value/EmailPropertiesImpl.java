package com.jgji.daily_condition_tracker.domain.shared.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@RequiredArgsConstructor
@ConfigurationProperties(prefix = "email")
class EmailPropertiesImpl implements EmailProperties {

    private final Smtp smtp;
    private final String from;
    private final String frontEndUrl;

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public String getFrontEndUrl() {
        return frontEndUrl;
    }

    @Getter
    @RequiredArgsConstructor
    private static class Smtp {
        private final String host;
        private final int port;
    }
}
