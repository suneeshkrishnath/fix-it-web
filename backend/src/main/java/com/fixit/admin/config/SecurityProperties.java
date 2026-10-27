package com.fixit.admin.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(Jwt jwt, List<String> corsAllowedOrigins) {

    public record Jwt(String secret, Duration accessTtl, Duration refreshTtl) {
    }
}
