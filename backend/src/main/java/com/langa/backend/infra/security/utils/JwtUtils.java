package com.langa.backend.infra.security.utils;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.repositories.TokenRepository;
import com.langa.backend.domain.users.services.TokenProvider;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.valueobjects.TokenType;
import com.langa.backend.infra.security.config.JwtConfig;
import com.langa.backend.infra.security.exceptions.SecurityException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class JwtUtils implements TokenProvider {

    private final JwtConfig jwtConfig;
    private final TokenRepository tokenRepository;

    public JwtUtils(JwtConfig jwtConfig, TokenRepository tokenRepository) {
        this.jwtConfig = jwtConfig;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public String generateToken(User user, TokenType tokenType) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationMs()))
                .setHeaderParam("kid", jwtConfig.getKid())
                .signWith(jwtConfig.getKey(), SignatureAlgorithm.HS256)
                .addClaims(customClaims(user, tokenType))
                .compact();
    }

    private Map<String, Object> customClaims(User user, TokenType tokenType) {
        return Map.of(
                "type", tokenType.toString(),
                "account_key", user.getAccountKey());
    }

    @Override
    public String generateToken(String user, TokenType tokenType) {
        return Jwts.builder()
                .setSubject(user)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationMs()))
                .setHeaderParam("kid", jwtConfig.getKid())
                .signWith(jwtConfig.getKey(), SignatureAlgorithm.HS256)
                .addClaims(Map.of("type", tokenType.toString()))
                .compact();
    }

    @Override
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtConfig.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtConfig.getKey()).build().parseClaimsJws(token);
            return !tokenRepository.isRevoked(token);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getClaim(String claim) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
            Map<String, Object> details = (Map<String, Object>) usernamePasswordAuthenticationToken.getDetails();
            log.debug("auth details {}",details);
            String token = (String) details.get("token");
            return getClaim(token, claim);
        }
        log.error("No JWT Authentication found in Security Context");
        throw new SecurityException("No JWT Authentication found in Security Context", null, Errors.INVALID_CREDENTIALS);
    }

    private String getClaim(String token, String claim) {
        try {
            return Jwts.parserBuilder().setSigningKey(jwtConfig.getKey())
                    .build().parseClaimsJws(token)
                    .getBody().get(claim).toString();
        } catch (Exception e) {
            return null;
        }

    }
}
