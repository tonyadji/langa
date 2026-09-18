package com.langa.backend.infra.adapters.persistence.users.mongo;

import com.langa.backend.domain.users.valueobjects.Token;
import com.langa.backend.domain.users.valueobjects.TokenType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "c_tokens")
@Data
public class TokenDocument {
    @Id
    private String id;
    private String token;
    private TokenType tokenType;
    private String userEmail;
    private Instant expiresAt;
    private boolean revoked;

    public TokenDocument() {}

    public TokenDocument(String token, String userEmail, TokenType type, Instant expiresAt, boolean revoked) {
        this.token = token;
        this.tokenType = type;
        this.userEmail = userEmail;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public static TokenDocument of(Token token) {
        return new TokenDocument(token.getValue(), token.getBearer(), token.getType(), token.getExpiresAt(), token.isRevoked());
    }

    public Token toToken() {
        return new Token(this.token, this.userEmail, this.expiresAt, this.revoked, this.tokenType);
    }
}
