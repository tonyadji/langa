package com.langa.backend.domain.applications.valueobjects;

import java.time.LocalDateTime;

public record ShareWith(
        String appId,
        String appName,
        String key,
        SharedWithProfile profile,
        LocalDateTime sharedDate,
        LocalDateTime expirationDate,
        LocalDateTime revokedDate
) {

    public boolean isCurrentlyActive() { return !isExpired() && !isRevoked(); }
    public boolean isExpired() { return expirationDate != null && expirationDate.isBefore(LocalDateTime.now()); }
    public boolean isRevoked() { return revokedDate != null; }
}
