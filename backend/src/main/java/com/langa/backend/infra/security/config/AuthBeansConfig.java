package com.langa.backend.infra.security.config;

import com.langa.backend.domain.users.RefreshTokenService;
import com.langa.backend.domain.users.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthBeansConfig {
    @Bean
    public RefreshTokenService refreshTokenService(RefreshTokenRepository refreshTokenRepository,
                                                   @Value("${application.security.jwt.refresh-token.expiration}")long refreshTokenExpiration) {
        return new RefreshTokenService(refreshTokenRepository, refreshTokenExpiration);
    }
}
