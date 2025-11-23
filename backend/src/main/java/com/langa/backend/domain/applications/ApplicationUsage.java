package com.langa.backend.domain.applications;

public record ApplicationUsage(
    String id,
    String appKey,
        long totalLogBytes,
        long totalMetricBytes
) {
    public ApplicationUsage increaseLogBytes(long bytes) {
        return new ApplicationUsage(id, appKey, totalLogBytes + bytes, totalMetricBytes);
    }

    public ApplicationUsage increaseTotalMetricBytes(long bytes) {
        return new ApplicationUsage(id, appKey, totalLogBytes, totalMetricBytes + bytes);
    }
}
