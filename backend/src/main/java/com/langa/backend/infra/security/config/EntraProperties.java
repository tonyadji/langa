package com.langa.backend.infra.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Microsoft Entra External ID settings used to validate the access tokens sent by the frontend.
 */
@Component
@ConfigurationProperties(prefix = "application.security.entra")
@Setter
public class EntraProperties {

    /** Expected {@code iss} claim, e.g. https://{tenant-id}.ciamlogin.com/{tenant-id}/v2.0 */
    @Getter
    private String issuerUri;
    /** Signing keys endpoint, e.g. https://{subdomain}.ciamlogin.com/{tenant-id}/discovery/v2.0/keys */
    @Getter
    private String jwkSetUri;
    /** Accepted {@code aud} values (comma separated): the API application (client) ID and/or its App ID URI. */
    private String audiences;
    /** Delegated scope the access token must contain in its {@code scp} claim. */
    @Getter
    private String requiredScope;

    public List<String> getAudiences() {
        return Arrays.stream(audiences.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
