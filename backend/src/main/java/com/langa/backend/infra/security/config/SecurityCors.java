package com.langa.backend.infra.security.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "application.security.cors")
@Setter
public class SecurityCors {

    private String allowedOrigins;
    private String allowedMethods;
    private String allowedHeaders;
    private boolean allowCredentials;
    private String patternRegistry;

    public List<String> getAllowedOrigins() {
        return  Arrays.asList(allowedOrigins.split(","));
    }

    public List<String> getAllowedMethods() {
        return Arrays.asList(allowedMethods.split(","));
    }

    public List<String> getAllowedHeaders() {
        return Arrays.asList(allowedHeaders.split(","));
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public String getPatternRegistry() {
        return patternRegistry;
    }
}
