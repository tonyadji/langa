package com.langa.backend.infra.security.config;

import com.langa.backend.infra.security.auth.EntraJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Configuration
public class SecurityConfig {

    static final String AUDIENCE_CLAIM = "aud";
    static final String SCOPE_CLAIM = "scp";

    private final SecurityEndpoints securityEndpoints;
    private final SecurityCors securityCors;
    private final EntraProperties entraProperties;
    private final EntraJwtAuthenticationConverter jwtAuthenticationConverter;

    public SecurityConfig(SecurityEndpoints securityEndpoints,
                          SecurityCors securityCors,
                          EntraProperties entraProperties,
                          EntraJwtAuthenticationConverter jwtAuthenticationConverter) {
        this.securityEndpoints = securityEndpoints;
        this.securityCors = securityCors;
        this.entraProperties = entraProperties;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizer -> authorizer
                .requestMatchers(securityEndpoints.getUnsecured()).permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    /**
     * Validates Entra ID access tokens: RS256 signature (keys fetched lazily from the JWKS endpoint),
     * expiry, issuer (hence tenant), audience (this API) and delegated scope.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(entraProperties.getJwkSetUri())
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(jwtValidator(entraProperties));
        return decoder;
    }

    static OAuth2TokenValidator<Jwt> jwtValidator(EntraProperties properties) {
        final List<String> audiences = properties.getAudiences();
        final String requiredScope = properties.getRequiredScope();

        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<Collection<String>>(AUDIENCE_CLAIM,
                aud -> aud != null && aud.stream().anyMatch(audiences::contains));
        OAuth2TokenValidator<Jwt> scopeValidator = new JwtClaimValidator<String>(SCOPE_CLAIM,
                scp -> scp != null && Arrays.asList(scp.split(" ")).contains(requiredScope));

        return new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.getIssuerUri()),
                audienceValidator,
                scopeValidator);
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
