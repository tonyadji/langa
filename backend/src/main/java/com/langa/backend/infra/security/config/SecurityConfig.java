package com.langa.backend.infra.security.config;

import com.langa.backend.infra.security.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {


    private final SecurityEndpoints securityEndpoints;
    private final SecurityCors securityCors;
    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(SecurityEndpoints securityEndpoints, SecurityCors securityCors, JwtAuthenticationFilter jwtFilter) {
        this.securityEndpoints = securityEndpoints;
        this.securityCors = securityCors;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorizer -> authorizer
                .requestMatchers(securityEndpoints.getUnsecured()).permitAll()
                .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
