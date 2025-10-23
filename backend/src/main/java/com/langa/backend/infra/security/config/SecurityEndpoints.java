package com.langa.backend.infra.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.security.endpoints")
public class SecurityEndpoints {

    @Value("${application.security.endpoints.unsecured}")
    private String unsecured;

    public String[] getUnsecured() { return unsecured.split(","); }

    public void setUnsecured(String unsecured) {
        this.unsecured = unsecured;
    }
}
