package com.portfolio.erp.application.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(LoginRateLimit loginRateLimit) {

    public record LoginRateLimit(int maxAttempts, Duration window) {
    }
}
