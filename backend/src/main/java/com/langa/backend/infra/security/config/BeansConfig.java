package com.langa.backend.infra.security.config;

import com.langa.backend.domain.users.repositories.TokenRepository;
import com.langa.backend.domain.users.services.TokenProvider;
import com.langa.backend.domain.users.services.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    public TokenService tokenService(TokenRepository tokenRepository,
                                     @Value("${application.security.jwt.refresh-token.expiration}")long refreshTokenExpiration,
                                     TokenProvider provider) {
        return new TokenService(tokenRepository, refreshTokenExpiration, provider);
    }
}
