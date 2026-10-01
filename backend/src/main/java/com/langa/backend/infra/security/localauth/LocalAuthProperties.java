package com.langa.backend.infra.security.localauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Local authentication mode: the backend issues its own access tokens, so Langa can run without an
 * identity provider (docker compose, demos). Anyone can get a token for any email: never enable it in production.
 */
@Component
@ConfigurationProperties(prefix = "application.security.local-auth")
@Getter
@Setter
public class LocalAuthProperties {

    /** Exposes POST /api/auth/local/token and validates the tokens it issues instead of the provider ones. */
    private boolean enabled = false;
    /** Lifetime of the issued access tokens. */
    private Duration tokenTtl = Duration.ofHours(12);
}
