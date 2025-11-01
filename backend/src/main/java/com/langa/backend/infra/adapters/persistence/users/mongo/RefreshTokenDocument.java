package com.langa.backend.infra.adapters.persistence.users.mongo;

import com.langa.backend.domain.users.RefreshToken;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "refresh_tokens")
@Data
public class RefreshTokenDocument {
    @Id
    private String id;
    private String token;
    private String userEmail;
    private Instant expiresAt;
    private boolean revoked;

    public RefreshTokenDocument() {}

    public RefreshTokenDocument(String token, String userEmail, Instant expiresAt, boolean revoked) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public static RefreshTokenDocument of(RefreshToken token) {
        return new RefreshTokenDocument(token.getToken(), token.getUserEmail(), token.getExpiresAt(), token.isRevoked());
    }

    public RefreshToken toRefreshToken() {
        return new RefreshToken(this.token, this.userEmail, this.expiresAt, this.revoked);
    }
}
