package com.langa.backend.infra.security.localauth;

import com.langa.backend.infra.security.config.AuthProviderProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the local token issuer, only when {@code application.security.local-auth.enabled=true}.
 * The identity provider must then be named {@value LocalIdentity#PROVIDER}, so that local identities are never
 * mixed with the identities of a real provider.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "application.security.local-auth", name = "enabled", havingValue = "true")
public class LocalAuthConfiguration {

    @Bean
    public LocalTokenIssuer localTokenIssuer(AuthProviderProperties authProperties, LocalAuthProperties localAuthProperties) {
        if (!LocalIdentity.PROVIDER.equals(authProperties.getProvider())) {
            throw new IllegalStateException("Local authentication requires application.security.auth.provider="
                    + LocalIdentity.PROVIDER + " (got " + authProperties.getProvider() + ")");
        }
        log.warn("Local authentication is ENABLED: anyone can sign in with any email. Never enable it in production.");
        return new LocalTokenIssuer(authProperties, localAuthProperties.getTokenTtl(), LocalTokenIssuer.generateKeyPair());
    }
}
