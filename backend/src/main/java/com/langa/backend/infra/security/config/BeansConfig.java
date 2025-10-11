package com.langa.backend.infra.security.config;

import com.langa.backend.domain.users.repositories.RefreshTokenRepository;
import com.langa.backend.domain.users.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    public RefreshTokenService refreshTokenService(RefreshTokenRepository refreshTokenRepository,
                                                   @Value("${application.security.jwt.refresh-token.expiration}")long refreshTokenExpiration) {
        return new RefreshTokenService(refreshTokenRepository, refreshTokenExpiration);
    }
}
