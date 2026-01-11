package com.langa.backend.domain.applications.valueobjects;

import java.time.Instant;

public record ApplicationUsageTrend(
        String name,
        String key,
        long usage,
        IngestionType type,
        Instant createdDate
) {
}
