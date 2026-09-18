package com.langa.backend.domain.applications.valueobjects;

import java.time.Instant;

public record ApplicationUsage(
        long totalLogBytes,
        long totalMetricBytes,
        Instant lastUpdatedDate
) {
    public static ApplicationUsage empty() {
        return new ApplicationUsage(0, 0, Instant.EPOCH);
    }

    public ApplicationUsage increaseLogBytes(long bytes) {
        return new ApplicationUsage(totalLogBytes + bytes, totalMetricBytes, Instant.now());
    }

    public ApplicationUsage increaseTotalMetricBytes(long bytes) {
        return new ApplicationUsage(totalLogBytes, totalMetricBytes + bytes, Instant.now());
    }
}
