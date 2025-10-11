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
}
