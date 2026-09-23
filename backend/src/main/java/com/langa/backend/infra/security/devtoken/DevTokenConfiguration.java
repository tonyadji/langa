package com.langa.backend.infra.security.devtoken;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wires the development token provider, only when {@code application.security.dev-token.enabled=true}.
 * Entra External ID is the only implementation today; another identity provider would add its own here.
 */
@Configuration
@ConditionalOnProperty(prefix = "application.security.dev-token", name = "enabled", havingValue = "true")
public class DevTokenConfiguration {

    @Bean
    public DevTokenProvider devTokenProvider(DevTokenProperties properties) {
        return new EntraNativeAuthDevTokenProvider(properties, RestClient.create());
    }
}
