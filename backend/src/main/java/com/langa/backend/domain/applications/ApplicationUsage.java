package com.langa.backend.domain.applications;

public record ApplicationUsage(
        String appKey,
        long totalLogBytes,
        long totalMetricBytes
) {
    public ApplicationUsage increaseLogBytes(long bytes) {
        return new ApplicationUsage(appKey, totalLogBytes + bytes, totalMetricBytes);
    }

    public ApplicationUsage increaseTotalMetricBytes(long bytes) {
        return new ApplicationUsage(appKey, totalLogBytes, totalMetricBytes + bytes);
    }
}
