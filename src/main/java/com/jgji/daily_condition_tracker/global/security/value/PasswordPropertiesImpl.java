package com.jgji.daily_condition_tracker.global.security.value;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.password")
class PasswordPropertiesImpl implements PasswordProperties {

    private final String pepper;

    public PasswordPropertiesImpl(String pepper) {
        this.pepper = pepper;
    }

    @Override
    public String getPepper() {
        return pepper;
    }
}
