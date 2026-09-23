package com.langa.backend.infra.security.config;

import com.langa.backend.infra.security.auth.ExternalJwtAuthenticationConverter;
import com.langa.backend.infra.rest.teams.AcceptInvitationController;
import com.langa.backend.infra.security.devtoken.DevTokenController;
import com.langa.backend.infra.security.devtoken.DevTokenProperties;
import com.langa.backend.infra.security.identity.ClaimsExternalIdentityResolver;
import com.langa.backend.infra.security.identity.ExternalIdentityResolver;
import com.langa.backend.infra.security.identity.UserInfoExternalIdentityResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Configuration
public class SecurityConfig {

    static final String[] HEALTH_PATHS = {"/actuator/health", "/actuator/health/**", "/actuator/info"};

    private final SecurityEndpoints securityEndpoints;
    private final SecurityCors securityCors;
    private final AuthProviderProperties authProperties;
    private final DevTokenProperties devTokenProperties;

    public SecurityConfig(SecurityEndpoints securityEndpoints,
                          SecurityCors securityCors,
                          AuthProviderProperties authProperties,
                          DevTokenProperties devTokenProperties) {
        this.securityEndpoints = securityEndpoints;
        this.securityCors = securityCors;
        this.authProperties = authProperties;
        this.devTokenProperties = devTokenProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ExternalJwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizer -> {
                    // Always authenticated, even if listed in the unsecured endpoints: the guest is the signed-in user
                    authorizer.requestMatchers(HttpMethod.POST, AcceptInvitationController.ACCEPT_PATH).authenticated();
                    // Health probes for orchestrators and load balancers (details are hidden unless configured)
                    authorizer.requestMatchers(HEALTH_PATHS).permitAll();
                    authorizer.requestMatchers(securityEndpoints.getUnsecured()).permitAll();
                    if (devTokenProperties.isEnabled()) {
                        // Development endpoint issuing tokens: public only when explicitly enabled
                        authorizer.requestMatchers(DevTokenController.PATH + "/**").permitAll();
                    }
                    authorizer.anyRequest().authenticated();
                })
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    /**
     * Validates the access tokens of the configured identity provider: RS256 signature (keys fetched lazily
     * from the JWKS endpoint), expiry, issuer, audience, scope and additional required claims.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(authProperties.getJwkSetUri())
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(jwtValidator(authProperties));
        return decoder;
    }

    @Bean
    public ExternalIdentityResolver externalIdentityResolver() {
        if (authProperties.getProvider() == null || authProperties.getProvider().isBlank()) {
            throw new IllegalStateException("application.security.auth.provider must not be empty");
        }
        return switch (authProperties.getEmailSource()) {
            case CLAIM -> new ClaimsExternalIdentityResolver(authProperties);
            case USERINFO -> new UserInfoExternalIdentityResolver(authProperties, RestClient.create());
        };
    }

    static OAuth2TokenValidator<Jwt> jwtValidator(AuthProviderProperties properties) {
        final List<String> audiences = properties.getAudiences();
        if (audiences.isEmpty()) {
            throw new IllegalStateException("application.security.auth.audiences must not be empty");
        }

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(JwtValidators.createDefaultWithIssuer(properties.getIssuerUri()));
        validators.add(claimValidator(properties.getAudienceClaim(), values -> values.stream().anyMatch(audiences::contains)));

        final String requiredScope = properties.getRequiredScope();
        if (requiredScope != null && !requiredScope.isBlank()) {
            validators.add(claimValidator(properties.getScopeClaim(), values -> values.contains(requiredScope)));
        }
        for (Map.Entry<String, String> claim : properties.getRequiredClaims().entrySet()) {
            validators.add(claimValidator(claim.getKey(), values -> values.contains(claim.getValue())));
        }
        return new DelegatingOAuth2TokenValidator<>(validators);
    }

    /**
     * Validates a claim whose value may be a string (space separated values, e.g. scopes) or a list.
     */
    private static OAuth2TokenValidator<Jwt> claimValidator(String claim, Predicate<List<String>> test) {
        final OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,
                "The " + claim + " claim is not valid", null);
        return jwt -> test.test(claimValues(jwt.getClaim(claim)))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(error);
    }

    private static List<String> claimValues(Object claimValue) {
        if (claimValue instanceof Collection<?> values) {
            return values.stream().map(String::valueOf).toList();
        }
        if (claimValue instanceof String value) {
            return Arrays.asList(value.split(" "));
        }
        return List.of();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(securityCors.getAllowedOrigins());
        configuration.setAllowedMethods(securityCors.getAllowedMethods());
        configuration.setAllowedHeaders(securityCors.getAllowedHeaders());
        configuration.setAllowCredentials(securityCors.isAllowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(securityCors.getPatternRegistry(), configuration);
        return source;
    }
}
