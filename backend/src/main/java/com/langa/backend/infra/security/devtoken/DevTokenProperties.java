package com.langa.backend.infra.security.devtoken;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Settings of the development endpoint issuing access tokens without the frontend.
 * Disabled by default: only enable it on local/test environments.
 */
@Component
@ConfigurationProperties(prefix = "application.security.dev-token")
@Getter
@Setter
public class DevTokenProperties {

    /** Exposes POST /api/dev/token/start and /complete. */
    private boolean enabled = false;
    /** Public client allowed to use the provider's native authentication API (e.g. "langa-test-client"). */
    private String clientId;
    /** Base URL of the native authentication API, e.g. https://{subdomain}.ciamlogin.com/{tenant-id}/oauth2/v2.0 */
    private String nativeAuthUri;
    /** Scopes requested for the access token, space separated (the API scope). */
    private String scope;
}
