package com.langa.backend.infra.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "application.security.endpoints")
public class SecurityEndpoints {

    private List<String> unsecured;

    public String[] getUnsecured() {
        return unsecured.toArray(new String[0]);
    }

    public void setUnsecured(List<String> unsecured) {
        this.unsecured = unsecured;
    }
}
